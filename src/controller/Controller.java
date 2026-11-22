package controller;

import java.util.HashMap;
import java.util.List;

import model.ColumnColor;
import model.GameData;
import model.ObserverIF;
import model.PlayerColor;
import model.TreasureType;
import model.TributeType;

/**
 * Coordinates match setup, turn life cycle, counselor claims, upgrades, scoring,
 * save availability rules, and player actions between View and Model.
 */
public class Controller {

    private int currentPlayer = 0;

    private boolean finalTurn = false;
    private boolean endRoundToFinalTurn = false;
    private boolean ended = false;

    private boolean firstSackCity = true;
    private ColumnColor chosenColumn = null;
    private boolean turnActionStarted = false;

    // Two-step choices: the player first SELECTS (click) and then CONFIRMS (button).
    // While only selected, nothing in the Model changes.
    private ColumnColor pendingColumn = null;
    private String pendingMoveNodeId = null;

    // Which pawn of the player will play this turn (only Altani has 2); chosen before "Confirm Column"
    private int selectedPawn = 0;

    // Final score of each player, filled when the match ends
    private int[] finalScores = new int[0];

    private int totalVotes = 0;
    private int maxVotes = 10;

    private static Controller controller = null;
    private GameData data = null;

    private Controller() { }

    public static Controller getController() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    public void register(ObserverIF o) {
        GameData.getData().add(o);
    }

    public boolean setupMatch(List<String> chosenHeirs, boolean alternativeKarakorum) {
        startGame(alternativeKarakorum);
        updateMaxVotes(chosenHeirs.size());
        return data.setupMatch(chosenHeirs, alternativeKarakorum);
    }

    private boolean startGame(boolean alternativeKarakorum) {
        data = GameData.createData(alternativeKarakorum);
        // The screens of a previous match (if any) must stop observing the game
        data.removeAllObservers();
        this.currentPlayer = 0;
        this.finalTurn = false;
        this.endRoundToFinalTurn = false;
        this.ended = false;
        this.totalVotes = 0;
        this.firstSackCity = true;
        this.chosenColumn = null;
        this.turnActionStarted = false;
        this.pendingColumn = null;
        this.pendingMoveNodeId = null;
        this.finalScores = new int[0];
        return true;
    }

    private void updateMaxVotes(int playersQty) {
        if (playersQty == 2) this.maxVotes = 10;
        else if (playersQty == 3) this.maxVotes = 14;
        else if (playersQty == 4) this.maxVotes = 16;
        else this.maxVotes = 17;
    }
    
    /*
     * ============================================
     * =============== Turn methods ===============
     * ============================================
     */
    
    private boolean canEndTurn() { return this.chosenColumn != null && !data.isKhanPending(currentPlayer); }
    
    /**
     * Saving is only permitted immediately before the active player starts their turn
     * (i.e., before choosing a column or taking any turn action).
     */
    public boolean canSaveMatch() {
        return !this.ended && this.chosenColumn == null && !this.turnActionStarted;
    }

    public boolean startTurn(ColumnColor column, int pawnIndex) {
        if (this.chosenColumn != null) {
            return false;
        }
        // Set turn flags before notifying observers so the Save button disables immediately
        this.chosenColumn = column;
        this.turnActionStarted = true;
        if (!data.startTurn(this.currentPlayer, column, pawnIndex)) {
            this.chosenColumn = null;
            this.turnActionStarted = false;
            return false;
        }
        return true;
    }

    public boolean endTurn() {
        int total = getTotalPlayers();
        if (this.ended || total == 0) {
            return false;
        }
        
        if (!canEndTurn()) return false;

        if (endGameConditionsMet() && !this.endRoundToFinalTurn) {
            this.endRoundToFinalTurn = true;
        }

        data.endTurn(this.currentPlayer);
        this.chosenColumn = null;
        this.turnActionStarted = false;
        this.pendingColumn = null;
        this.pendingMoveNodeId = null;
        this.selectedPawn = 0;
        nextPlayer();

        if (this.endRoundToFinalTurn) {
            if (!this.finalTurn && this.currentPlayer == 0) {
                this.finalTurn = true;
            } else if (this.finalTurn && this.currentPlayer == 0) {
                this.ended = true;
                calculatePoints();
            }
        }

        return true;
    }

    /*
     * ============================================
     * ====== Select first, confirm after =========
     * ============================================
     */

    /**
     * Marks a column of the player board as selected, without starting the turn yet.
     * The turn only starts when the player presses "Confirm Column".
     * @param column the clicked column
     * @return false (and nothing changes) if a column was already confirmed in this
     *         turn, the match ended or the column is occupied
     */
    public boolean selectColumn(ColumnColor column) {
        if (this.ended || this.chosenColumn != null || column == null || !isColumnFree(column)) {
            return false;
        }
        this.pendingColumn = column;
        data.notifyStateRestored();
        return true;
    }

    /**
     * Chooses which pawn of the current player will play this turn.
     * Only allowed before the column is confirmed.
     * @return false (and nothing changes) if the turn already started or the pawn does not exist
     */
    public boolean selectPawn(int pawnIndex) {
        if (this.ended || this.chosenColumn != null
                || pawnIndex < 0 || pawnIndex >= data.getPawnCount(this.currentPlayer)) {
            return false;
        }
        this.selectedPawn = pawnIndex;
        data.notifyStateRestored();
        return true;
    }

    /** @return the pawn that plays (or will play) this turn */
    public int getSelectedPawn() { return this.selectedPawn; }

    /** @return how many pawns the player has */
    public int getPawnCount(int playerInd) { return data.getPawnCount(playerInd); }

    /** @return node of one specific pawn of the player */
    public String getPawnNodeId(int playerInd, int pawnIndex) { return data.getPawnNodeId(playerInd, pawnIndex); }

    /** @return the selected column waiting for confirmation, or null if there is none */
    public ColumnColor getPendingColumn() { return this.pendingColumn; }

    /**
     * Starts the turn with the selected column (the "Confirm Column" button).
     * @return true if the turn started
     */
    public boolean confirmColumn() {
        if (this.pendingColumn == null) {
            return false;
        }
        ColumnColor column = this.pendingColumn;
        // Cleared before startTurn because startTurn notifies the observers,
        // and they must already see that nothing is pending anymore
        this.pendingColumn = null;
        if (!startTurn(column, this.selectedPawn)) {
            this.pendingColumn = column;
            return false;
        }
        return true;
    }

    /**
     * Marks a node of the map as the destination of the next move, without moving yet.
     * Only nodes the pawn can really reach (enough horses, a free space, a valid path)
     * are accepted; any other click does nothing.
     * @param nodeId the clicked node
     * @return true if the node became the selected destination
     */
    public boolean selectMoveDestination(String nodeId) {
        if (this.ended || this.chosenColumn == null || nodeId == null) {
            return false;
        }
        if (!data.getReachableNodeIds(this.currentPlayer).contains(nodeId)) {
            return false;
        }
        this.pendingMoveNodeId = nodeId;
        data.notifyStateRestored();
        return true;
    }

    /** @return the selected destination waiting for confirmation, or null if there is none */
    public String getPendingMoveNodeId() { return this.pendingMoveNodeId; }

    /**
     * Moves the pawn to the selected destination (the "Confirm Move" button).
     * @return true if the pawn moved
     */
    public boolean confirmMove() {
        if (this.pendingMoveNodeId == null) {
            return false;
        }
        String target = this.pendingMoveNodeId;
        this.pendingMoveNodeId = null;
        boolean moved = createMoveAction(target);
        if (!moved) {
            // Nothing moved, but the screen must stop showing the old selection
            data.notifyStateRestored();
        }
        return moved;
    }

    public List<String> getReachableNodeIds(int playerInd) { return data.getReachableNodeIds(playerInd); }
    public List<String> getTurnPathNodeIds(int playerInd) { return data.getTurnPathNodeIds(playerInd); }

    private void nextPlayer() {
        int total = getTotalPlayers();
        if (total > 0) {
            this.currentPlayer = (this.currentPlayer + 1) % total;
        }
        this.firstSackCity = true;
        data.setCurrentPlayer(this.currentPlayer);
    }

    private boolean endGameConditionsMet() {
        return data.areAllCityConquered() || this.totalVotes >= this.maxVotes;
    }

    public int getCurrentPlayerIndex() { return this.currentPlayer; }
    
    /**
     * 
     * @param playerInd
     * @return
     */
    public PlayerColor getPlayerColor(int playerInd) { return data.getPlayerColor(playerInd); }
    
    /**
     * 
     * @return
     */
    public ColumnColor getChosenColumn() { return this.chosenColumn; }
    public String getCurrentHeirName() { return data.getPlayerHeirName(this.currentPlayer); }
    public int getCurrentPlayerMovementsLeft() { return data.getMovementsLeft(this.currentPlayer); }
    public int getCurrentPlayerHandsLeft() { return data.getHandsLeft(this.currentPlayer); }
    public int getCurrentPlayerYurtsLeft() { return data.getPlayerYurtsLeft(this.currentPlayer); }
    public boolean isKhanPending() { return data.isKhanPending(this.currentPlayer); }
    public int[] getCurrentPlayerTributeAmount(TributeType trib) { return data.getPlayerTributeAmount(currentPlayer, trib); }
    public int getCurrentPlayerTreasure(TreasureType type) { return data.getPlayerTreasure(this.currentPlayer, type); }
    
    
    /**
     * 
     * @param playerInd
     * @return
     */
    public int getPlayerVotes(int playerInd) { return data.getPlayerVotes(playerInd); }
        
    /**
     * 
     * @param playerInd
     * @return
     */
    public String getPlayerNodeId(int playerInd) { return data.getPlayerNodeId(playerInd); }
    public boolean isColumnFree(ColumnColor column) { return data.isColumnFree(this.currentPlayer, column); }
    public String getCurrentPlayerPrivateCounselorName() { return data.getPlayerPrivateCounselorName(this.currentPlayer); }

    public boolean createMoveAction(String nodeId) {
        boolean moved = data.MovePawn(this.currentPlayer, nodeId);
        if (moved) {
            this.turnActionStarted = true;
        }
        return moved;
    }

    public boolean createYurtAction(String nodeId) {
        boolean built = data.placeYurtOnNode(this.currentPlayer, nodeId);
        if (built) {
            this.turnActionStarted = true;
        }
        return built;
    }

    public boolean createSackAction(String cityOrNodeId, int indTreasure) {
        if (indTreasure < 0 || indTreasure > 3) {
            return false;
        }
        int swordCost = this.firstSackCity ? 1 : 2;
        boolean success = data.attackCityForTreasure(this.currentPlayer, cityOrNodeId, indTreasure, swordCost);
        if (success) {
            this.firstSackCity = false;
            this.turnActionStarted = true;
        }
        return success;
    }

    public boolean createKhanAction(String provinceId) {
        boolean moved = data.moveKhan(this.currentPlayer, provinceId);
        if (moved) {
            this.turnActionStarted = true;
        }
        return moved;
    }

    public boolean createTributeAction(String provinceId) {
        boolean harvested = data.harvestProvinceResource(this.currentPlayer, data.getPlayerNodeId(currentPlayer), provinceId);
        if (harvested) {
            this.turnActionStarted = true;
        }
        return harvested;
    }

    public boolean createTradeAction(String nodeId) {
        boolean traded = data.executeTrade(this.currentPlayer, nodeId);
        if (traded) {
            this.totalVotes++;
            this.turnActionStarted = true;
        }
        return traded;
    }

    /*
     * ============================================
     * ============== Upgrade methods =============
     * ============================================
     */

    public List<String> getAvailableUpgradeCodes() { return data.getAvailableUpgradeCodes(); }
    public int getUpgradeDeckSize() { return data.getUpgradeDeckSize(); }
    public List<String> getCurrentPlayerColumnUpgrades(ColumnColor column) {
        return data.getPlayerColumnUpgrades(this.currentPlayer, column);
    }
    public boolean hasCurrentPlayerFreeUpgradePending() { return data.hasFreeUpgradePending(this.currentPlayer); }
    public int getCurrentPlayerFreeUpgradesPendingCount() { return data.getFreeUpgradesPendingCount(this.currentPlayer); }

    public boolean buyUpgradeFromMarket(int slotIndex) {
        boolean bought = data.buyUpgradeFromMarket(this.currentPlayer, slotIndex);
        if (bought) {
            this.turnActionStarted = true;
            data.notifyStateRestored();
        }
        return bought;
    }

    public boolean claimFreeUpgradeFromMarket(int slotIndex) {
        boolean claimed = data.claimFreeUpgradeFromMarket(this.currentPlayer, slotIndex);
        if (claimed) {
            this.turnActionStarted = true;
            data.notifyStateRestored();
        }
        return claimed;
    }

    public boolean claimFreeUpgradeFromDeck() {
        boolean claimed = data.claimFreeUpgradeFromDeck(this.currentPlayer);
        if (claimed) {
            this.turnActionStarted = true;
            data.notifyStateRestored();
        }
        return claimed;
    }

    /*
     * ============================================
     * ============== Points & Load ===============
     * ============================================
     */

    public int getTotalVotes() { return this.totalVotes; }
    public boolean isEndRoundToFinalTurn() { return this.endRoundToFinalTurn; }

    private void calculatePoints() {
        this.finalScores = new int[getTotalPlayers()];
        for (int i = 0; i < getTotalPlayers(); i++) {
            this.finalScores[i] = data.calculatePlayerTotalScore(i);
        }
    }
    
    public String getBonusCardName(String nodeId) { return data.getBonusCardOnPost(nodeId); }
    public HashMap<String, String> getBonusCardImgPath() { return data.getBonusCardImgPath(); }
    public HashMap<String, int[]> getBonusCardCoordinates() { return data.getBonusCardCoordinates(); }
    public List<String> getCurrentPlayerBonusCards() { return data.getPlayerUnusedBonusCardNames(currentPlayer); }
    public boolean useBonusCard(String name) { return data.useBonusCard(name, currentPlayer); }

    /*
     * ============================================
     * ================ End of match ==============
     * ============================================
     */

    /**
     * @param playerInd index of the player
     * @return the final score of the player, or 0 if the match has not ended
     */
    public int getFinalScore(int playerInd) {
        if (playerInd < 0 || playerInd >= this.finalScores.length) {
            return 0;
        }
        return this.finalScores[playerInd];
    }

    /** @return the highest final score of the match */
    public int getWinningScore() {
        int best = 0;
        for (int score : this.finalScores) {
            best = Math.max(best, score);
        }
        return best;
    }

    /**
     * @param playerInd index of the player
     * @return true if the match ended and this player has the highest score
     *         (when there is a tie, every player with the highest score wins)
     */
    public boolean isWinner(int playerInd) {
        return this.ended && getFinalScore(playerInd) == getWinningScore();
    }

    public String getPlayerHeirName(int playerInd) { return data.getPlayerHeirName(playerInd); }

    public void restoreControllerTurnState(int activePlayerInd, ColumnColor column) {
        if (activePlayerInd >= 0 && activePlayerInd < getTotalPlayers()) {
            this.currentPlayer = activePlayerInd;
            this.chosenColumn = column;
            this.turnActionStarted = (column != null);
            this.firstSackCity = true;
            this.pendingColumn = null;
            this.pendingMoveNodeId = null;
            data.setCurrentPlayer(this.currentPlayer);
        }
    }

    public void restoreMatchLifecycle(int savedTotalVotes, boolean savedEndRound, boolean savedFinalTurn) {
        this.totalVotes = Math.max(0, savedTotalVotes);
        this.endRoundToFinalTurn = savedEndRound;
        this.finalTurn = savedFinalTurn;
        this.ended = false;
    }

    public int getProvinceResourceCount(String provinceId) { return data.getProvinceResourceCount(provinceId); }
    public PlayerColor getYurtColor(String nodeId) { return data.getyurtColor(nodeId); }
    public String getKhanProvinceId() { return data.getKhanProvinceId(); }
    public List<String> getKhanProvincesIds() { return data.getKhanProvincesIds(); }
    public boolean isKhanAtUpgradeArea() { return data.isKhanAtUpgradeArea(); }
    public HashMap<String, int[]> getNodeCoordinates() { return data.getNodeCoordinates(); }
    public HashMap<String, int[]> getProvinceCoordinates() { return data.getProvinceCoordinates(); }
    public HashMap<String, int[]> getKhanCoordinates() { return data.getKhanCoordinates(); }
    public HashMap<String, int[]> getCitiesCoordinates() { return data.getCitiesCoordinates(); }
    public HashMap<String, String> getCitiesImgPath() { return data.getCitiesImgPath(); }
    public List<String> getCitiesToSack() { return data.getCitiesToSack(); }
    public List<String> getCityTreasures(String cityName) { return data.getCityTreasures(cityName); }
    public List<String> getGameOpenCounselorsIds() { return data.getGameOpenCounselorIds(); }

    public List<String> getPrivateCounselorCandidates() { return data.getPrivateCounselorCandidates(); }
    public boolean choosePrivateCounselor(String name) { return data.choosePrivateCounselor(this.currentPlayer, name); }

    public int getTotalPlayers() { return data != null ? data.getPlayerCount() : 0; }
    public String getOpenCounselorOwnerName(int cardInd) { return data.getOpenCounselorOwnerName(cardInd); }
    public boolean getGameHasEnded() { return this.ended; }
    public boolean getLastRound() { return this.finalTurn; }
}
