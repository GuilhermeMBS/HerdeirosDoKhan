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
 * Singleton that sits between the View and the Model (through the {@link GameData} facade).
 * Coordinates match setup, turn life cycle, counselor claims, upgrades, scoring,
 * save availability rules, and player actions between View and Model.
 * <p>
 * Every method that receives no player index works on the <b>current player</b>.
 * Methods that receive a {@code playerInd} work on that specific player.
 */
public class Controller {

    /*
     * ============================================
     * =============== Attributes =================
     * ============================================
     */

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

    /*
     * ============================================
     * ========= Constructor & Singleton ==========
     * ============================================
     */

    private Controller() { }

    /**
     * Get the only instance of the controller, creating it on the first call
     * @return {@code Controller} the singleton instance
     */
    public static Controller getController() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    /**
     * Register a view to be notified whenever the match state changes.
     * Observers registered before {@link #setupMatch(List, boolean)} are dropped when the match starts,
     * so views must register after the match has been set up
     * @param o {@code ObserverIF} the view that wants to observe the match
     */
    public void register(ObserverIF o) {
        GameData.getData().add(o);
    }

    /*
     * ============================================
     * ================ Match setup ===============
     * ============================================
     */

    /**
     * Set up a new match. Resets every controller flag, creates the game data and
     * defines the maximum amount of votes according to how many players there are
     * @param chosenHeirs {@code List<String>} containing all the Heir names chosen for the match
     * @param alternativeKarakorum {@code boolean} {@code true} if it's to use the alternative Karakorum
     * @return {@code true} if the match could be initialized, {@code false} otherwise
     */
    public boolean setupMatch(List<String> chosenHeirs, boolean alternativeKarakorum) {
        startGame(alternativeKarakorum);
        updateMaxVotes(chosenHeirs.size());
        return data.setupMatch(chosenHeirs, alternativeKarakorum);
    }

    /**
     * Create the game data and reset every flag of the controller to the state of a fresh match.
     * Also removes the observers of a previous match, if any
     * @param alternativeKarakorum {@code boolean} {@code true} if it's to use the alternative Karakorum
     * @return {@code true} always
     */
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

    /**
     * Define how many votes (trades made) the match can reach before the end of the game is triggered.
     * 2 players: 10, 3 players: 14, 4 players: 16, any other amount: 17
     * @param playersQty {@code int} amount of players in the match
     */
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

    /**
     * Verify if the current player is allowed to end the turn
     * @return {@code true} if a column was already chosen and Khan does not need to be moved,
     * {@code false} otherwise
     */
    private boolean canEndTurn() { return this.chosenColumn != null && !data.isKhanPending(currentPlayer); }
    
    /**
     * Verify if the match can be saved right now.
     * Saving is only permitted immediately before the active player starts their turn
     * (i.e., before choosing a column or taking any turn action).
     * @return {@code true} if the match has not ended and the turn has not started, {@code false} otherwise
     */
    public boolean canSaveMatch() {
        return !this.ended && this.chosenColumn == null && !this.turnActionStarted;
    }

    /**
     * Start the turn of the current player. The turn flags are set before the observers are notified,
     * so the Save button is disabled immediately. If the Model refuses, the flags are rolled back
     * @param column {@code ColumnColor} column chosen for the turn
     * @param pawnIndex {@code int} index of the pawn that will play the turn
     * @return {@code true} if the turn started, {@code false} if a column was already chosen in this turn
     * or the Model could not start it
     */
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

    /**
     * End the turn of the current player and pass it to the next one.
     * If the end game conditions were met, the current round becomes the last one
     * ({@link #getLastRound()}); when that last round reaches the first player again,
     * the match ends and the final scores are calculated
     * @return {@code true} if the turn ended, {@code false} if the match already ended,
     * there is no match, or the player is not allowed to end the turn yet (no column chosen or Khan pending)
     */
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
     * @param column {@code ColumnColor} the clicked column
     * @return {@code true} if the column became the selected one. {@code false} (and nothing changes) if a column
     * was already confirmed in this turn, the match ended, the column is {@code null} or the column is occupied
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
     * @param pawnIndex {@code int} index of the pawn
     * @return {@code true} if the pawn was selected. {@code false} (and nothing changes) if the turn already started,
     * the match ended or the pawn does not exist
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

    /**
     * Get the pawn that plays (or will play) this turn
     * @return {@code int} index of the selected pawn
     */
    public int getSelectedPawn() { return this.selectedPawn; }

    /**
     * Get how many pawns that player have
     * @param playerInd {@code int} index of the player that you want to check the amount of pawns
     * @return {@code int} amount of pawns of that player
     */
    public int getPawnCount(int playerInd) { return data.getPawnCount(playerInd); }

    /**
     * Get the node id of a specific pawn from a player
     * @param playerInd {@code int} index of the player that you want to check
     * @param pawnIndex {@code int} index of the pawn to get the node id
     * @return {@code String} of the nodeId from that pawn
     */
    public String getPawnNodeId(int playerInd, int pawnIndex) { return data.getPawnNodeId(playerInd, pawnIndex); }

    /**
     * Get the column that was selected and is waiting for confirmation
     * @return {@code ColumnColor} of the selected column, {@code null} if there is none
     */
    public ColumnColor getPendingColumn() { return this.pendingColumn; }

    /**
     * Starts the turn with the selected column (the "Confirm Column" button) using the selected pawn.
     * @return {@code true} if the turn started, {@code false} if there is no selected column or the turn
     * could not start (the column stays selected in that case)
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
     * @param nodeId {@code String} id of the clicked node
     * @return {@code true} if the node became the selected destination, {@code false} otherwise
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

    /**
     * Get the destination that was selected and is waiting for confirmation
     * @return {@code String} id of the selected node, {@code null} if there is none
     */
    public String getPendingMoveNodeId() { return this.pendingMoveNodeId; }

    /**
     * Moves the pawn to the selected destination (the "Confirm Move" button).
     * The selection is always cleared, even if the move fails
     * @return {@code true} if the pawn moved, {@code false} if there is no selected destination or the move failed
     */
    public boolean confirmMove() {
        if (this.pendingMoveNodeId == null) return false;
        
        String target = this.pendingMoveNodeId;
        this.pendingMoveNodeId = null;
        boolean moved = createMoveAction(target);
        if (!moved) {
            // Nothing moved, but the screen must stop showing the old selection
            data.notifyStateRestored();
        }
        return moved;
    }

    /**
     * Get all nodes that a player can reach, considering his movement and if the node if full
     * @param playerInd {@code int} of the player index that want to get the path
     * @return {@code List<String>} containing all the nodes id that the player can reach/move to
     */
    public List<String> getReachableNodeIds(int playerInd) { return data.getReachableNodeIds(playerInd); }

    /**
     * Get all the nodes id that the player has passed in this turn
     * @param playerInd {@code int} of the player index that want to get the path
     * @return {@code List<String>} containing all nodes id that the player has passed through
     */
    public List<String> getTurnPathNodeIds(int playerInd) { return data.getTurnPathNodeIds(playerInd); }

    /**
     * Pass the turn to the next player (wrapping around after the last one),
     * allowing the first sack of the turn to be paid with the cheaper sword cost again
     */
    private void nextPlayer() {
        int total = getTotalPlayers();
        if (total > 0) {
            this.currentPlayer = (this.currentPlayer + 1) % total;
        }
        this.firstSackCity = true;
        data.setCurrentPlayer(this.currentPlayer);
    }

    /**
     * Verify if the match must enter its last round
     * @return {@code true} if all the cities have been conquered or the maximum amount of votes was reached,
     * {@code false} otherwise
     */
    private boolean endGameConditionsMet() {
        return data.areAllCityConquered() || this.totalVotes >= this.maxVotes;
    }

    /*
     * ============================================
     * ============ Current player info ===========
     * ============================================
     */

    /**
     * Get the index of the player whose turn it is
     * @return {@code int} index of the current player
     */
    public int getCurrentPlayerIndex() { return this.currentPlayer; }
    
    /**
     * Get the color of the player from its index
     * @param playerInd {@code int} index of the player that you want to get the color
     * @return {@code PlayerColor} of that player, {@code PlayerColor.BLUE} as default
     */
    public PlayerColor getPlayerColor(int playerInd) { return data.getPlayerColor(playerInd); }
    
    /**
     * Get the column chosen by the current player in this turn
     * @return {@code ColumnColor} of the confirmed column, {@code null} if the turn has not started
     */
    public ColumnColor getChosenColumn() { return this.chosenColumn; }

    /**
     * Get the Heir name of the current player
     * @return {@code String} name of the Heir from the current player
     */
    public String getCurrentHeirName() { return data.getPlayerHeirName(this.currentPlayer); }

    /**
     * Get how many movements are left for the current player
     * @return {@code int} amount of movements left
     */
    public int getCurrentPlayerMovementsLeft() { return data.getMovementsLeft(this.currentPlayer); }

    /**
     * Get how many "hands" the current player has left
     * @return {@code int} amount of hands left in this turn
     */
    public int getCurrentPlayerHandsLeft() { return data.getHandsLeft(this.currentPlayer); }

    /**
     * Get how many yurts the current player can still place
     * @return {@code int} the amount that the player has left
     */
    public int getCurrentPlayerYurtsLeft() { return data.getPlayerYurtsLeft(this.currentPlayer); }

    /**
     * Verify if the current player still needs to move Khan to end the turn
     * @return {@code true} if the player still needs to move Khan, {@code false} otherwise
     */
    public boolean isKhanPending() { return data.isKhanPending(this.currentPlayer); }

    /**
     * Get the amount of a tribute from the current player
     * @param trib {@code TributeType} type of the tribute that want to get info about
     * @return {@code int[]} in {@code TributePosition.TRIBUTE.ordinal()} position with the amount of such tribute that the player has.
     * {@code TributePosition.VIRTUAL.ordinal()} position with the virtual amount of that tribute
     */
    public int[] getCurrentPlayerTributeAmount(TributeType trib) { return data.getPlayerTributeAmount(currentPlayer, trib); }

    /**
     * Get the amount of a treasure from the current player
     * @param type {@code TreasureType} type of the treasure that want to get info about
     * @return {@code int[]} in {@code TributePosition.TRIBUTE.ordinal()} position with the amount of such treasure that the player has.
     * {@code TributePosition.VIRTUAL.ordinal()} position with the virtual amount of that treasure
     */
    public int[] getCurrentPlayerTreasure(TreasureType type) { return data.getPlayerTreasure(this.currentPlayer, type); }
    
    /**
     * Get how many votes a player have at that time
     * @param playerInd {@code int} index of the player
     * @return {@code int} amount of votes that the player has at the moment
     */
    public int getPlayerVotes(int playerInd) { return data.getPlayerVotes(playerInd); }
        
    /**
     * Get current node id of the player
     * @param playerInd {@code int} index of the player that you want to check
     * @return {@code String} of the nodeId of that player
     */
    public String getPlayerNodeId(int playerInd) { return data.getPlayerNodeId(playerInd); }

    /**
     * Verify if the column is free to activate by the current player
     * @param column {@code ColumnColor} column that wants to be verified
     * @return {@code true} if the column haven't been activated before, therefore valid.
     * {@code false} otherwise
     */
    public boolean isColumnFree(ColumnColor column) { return data.isColumnFree(this.currentPlayer, column); }

    /**
     * Get the private counselor name of the current player
     * @return {@code String} of the name of private counselor. "None" if no private counselor have been chosen
     */
    public String getCurrentPlayerPrivateCounselorName() { return data.getPlayerPrivateCounselorName(this.currentPlayer); }

    /*
     * ============================================
     * ================= Actions ==================
     * ============================================
     */

    /**
     * Action of moving the current player's pawn along the board.
     * Marks the turn as started when it succeeds
     * @param nodeId {@code String} id of the node that the pawn will move to
     * @return {@code true} if the movement was a success, {@code false} otherwise
     */
    public boolean createMoveAction(String nodeId) {
        boolean moved = data.MovePawn(this.currentPlayer, nodeId);
        if (moved) {
            this.turnActionStarted = true;
        }
        return moved;
    }

    /**
     * Action of creating a yurt on a node for the current player.
     * Marks the turn as started when it succeeds
     * @param nodeId {@code String} id of the node that the yurt will be built on
     * @return {@code true} if the yurt could be built, {@code false} otherwise
     */
    public boolean createYurtAction(String nodeId) {
        boolean built = data.placeYurtOnNode(this.currentPlayer, nodeId);
        if (built) {
            this.turnActionStarted = true;
        }
        return built;
    }

    /**
     * Action of sacking a city for the current player.
     * The first city sacked in the turn costs 1 sword, every other one costs 2 swords.
     * Marks the turn as started when it succeeds
     * @param cityOrNodeId {@code String} nodeId or a name of a city
     * @param indTreasure {@code int} index of the treasure to be sacked (from 0 to 3)
     * @return {@code true} if the player had the amount of swords, the treasure was available and the city was enabled for sack.
     * {@code false} otherwise, including when the treasure index is out of range
     */
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

    /**
     * Action of the current player to move Khan.
     * Marks the turn as started when it succeeds
     * @param provinceId {@code String} id of the province that the Khan will move to. "UPGRADES" to the upgrades area
     * @return {@code true} if the movement was a success, {@code false} otherwise
     */
    public boolean createKhanAction(String provinceId) {
        boolean moved = data.moveKhan(this.currentPlayer, provinceId);
        if (moved) {
            this.turnActionStarted = true;
        }
        return moved;
    }

    /**
     * Action of harvesting a tribute from a province, from the node where the current player is.
     * Marks the turn as started when it succeeds
     * @param provinceId {@code String} id of the province that will collect the resource from
     * @return {@code true} if it could harvest a tribute, {@code false} otherwise
     */
    public boolean createTributeAction(String provinceId) {
        boolean harvested = data.harvestProvinceResource(this.currentPlayer, data.getPlayerNodeId(currentPlayer), provinceId);
        if (harvested) {
            this.turnActionStarted = true;
        }
        return harvested;
    }

    /**
     * Action of the current player to execute a trade.
     * Every successful trade counts as one vote towards the end of the match
     * ({@link #getTotalVotes()}). Marks the turn as started when it succeeds
     * @param nodeId {@code String} nodeId of a DoubleNode or CentralNode
     * @return {@code true} if the player can do the trade, giving him its bonus.
     * {@code false} otherwise
     */
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

    /**
     * Get all the upgrades that are available to buy right now
     * @return {@code List<String>} containing all the names of the upgrades available
     */
    public List<String> getAvailableUpgradeCodes() { return data.getAvailableUpgradeCodes(); }

    /**
     * Get how many upgrades are still available to buy
     * @return {@code int} indicating how many upgrades available there are
     */
    public int getUpgradeDeckSize() { return data.getUpgradeDeckSize(); }

    /**
     * Get all the upgrades on a column of the current player's board
     * @param column {@code ColumnColor} of the column to verify
     * @return {@code List<String>} containing all the upgrades names from that column
     */
    public List<String> getCurrentPlayerColumnUpgrades(ColumnColor column) {
        return data.getPlayerColumnUpgrades(this.currentPlayer, column);
    }

    /**
     * Verify if the current player can buy a free upgrade (completed a row)
     * @return {@code true} if the player can buy one for free, {@code false} otherwise
     */
    public boolean hasCurrentPlayerFreeUpgradePending() { return data.hasFreeUpgradePending(this.currentPlayer); }

    /**
     * Get how many free upgrades the current player can get
     * @return {@code int} amount of free upgrades that the player can buy
     */
    public int getCurrentPlayerFreeUpgradesPendingCount() { return data.getFreeUpgradesPendingCount(this.currentPlayer); }

    /**
     * Action of buying an upgrade from the market for the current player.
     * Marks the turn as started and refreshes the views when it succeeds
     * @param slotIndex {@code int} index of the upgrade slot
     * @return {@code true} if the purchase was successful, {@code false} otherwise
     */
    public boolean buyUpgradeFromMarket(int slotIndex) {
        boolean bought = data.buyUpgradeFromMarket(this.currentPlayer, slotIndex);
        if (bought) {
            this.turnActionStarted = true;
            data.notifyStateRestored();
        }
        return bought;
    }

    /**
     * Action of the current player to get a free upgrade from the market.
     * Marks the turn as started and refreshes the views when it succeeds
     * @param slotIndex {@code int} the index of the free upgrade
     * @return {@code true} if the player could get a free upgrade from the market, {@code false} otherwise
     */
    public boolean claimFreeUpgradeFromMarket(int slotIndex) {
        boolean claimed = data.claimFreeUpgradeFromMarket(this.currentPlayer, slotIndex);
        if (claimed) {
            this.turnActionStarted = true;
            data.notifyStateRestored();
        }
        return claimed;
    }

    /**
     * Action of the current player to get a free upgrade from the stock (deck).
     * Marks the turn as started and refreshes the views when it succeeds
     * @return {@code true} if the player could get a free upgrade from the deck, {@code false} otherwise
     */
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
     * ============== Points & Bonus ==============
     * ============================================
     */

    /**
     * Get how many trades have been made in the match. Reaching the maximum
     * for the amount of players triggers the last round
     * @return {@code int} amount of votes counted so far
     */
    public int getTotalVotes() { return this.totalVotes; }

    /**
     * Verify if the end of the match has already been triggered
     * @return {@code true} if the current round is the one that leads to the last round, {@code false} otherwise
     */
    public boolean isEndRoundToFinalTurn() { return this.endRoundToFinalTurn; }

    /**
     * Calculate and store the final score of every player. Called when the match ends
     */
    private void calculatePoints() {
        this.finalScores = new int[getTotalPlayers()];
        for (int i = 0; i < getTotalPlayers(); i++) {
            this.finalScores[i] = data.calculatePlayerTotalScore(i);
        }
    }
    
    /**
     * Get bonus card of a trading post
     * @param nodeId {@code String} of the DoubleNode or CentralNode
     * @return {@code String} of the name of the bonus card
     */
    public String getBonusCardName(String nodeId) { return data.getBonusCardOnPost(nodeId); }

    /**
     * Get all the paths for the images of the bonus cards
     * @return {@code HashMap<String, String>} mapping the bonus card name to its image path
     */
    public HashMap<String, String> getBonusCardImgPath() { return data.getBonusCardImgPath(); }

    /**
     * Get the bonus cards coordinate to show on the map
     * @return {@code HashMap<String, int[]>} mapping each bonus card name to its coordinate.
     * The first element of the array is the x coordinate, the second is the y.
     */
    public HashMap<String, int[]> getBonusCardCoordinates() { return data.getBonusCardCoordinates(); }

    /**
     * Get all the bonus cards that the current player can still use
     * @return {@code List<String>} containing all the names of the cards that the player have not used
     */
    public List<String> getCurrentPlayerBonusCards() { return data.getPlayerUnusedBonusCardNames(currentPlayer); }

    /**
     * Action of the current player to use a bonus card
     * @param name {@code String} name of the bonus card
     * @return {@code true} if the card hasn't been used and could use it.
     * {@code false} otherwise
     */
    public boolean useBonusCard(String name) { return data.useBonusCard(name, currentPlayer); }

    /*
     * ============================================
     * ================ End of match ==============
     * ============================================
     */

    /**
     * Get the final score of a player
     * @param playerInd {@code int} index of the player
     * @return {@code int} the final score of the player, 0 if the match has not ended or the index is invalid
     */
    public int getFinalScore(int playerInd) {
        if (playerInd < 0 || playerInd >= this.finalScores.length) {
            return 0;
        }
        return this.finalScores[playerInd];
    }

    /**
     * Get the highest final score of the match
     * @return {@code int} the highest final score, 0 if the match has not ended
     */
    public int getWinningScore() {
        int best = 0;
        for (int score : this.finalScores) {
            best = Math.max(best, score);
        }
        return best;
    }

    /**
     * Verify if a player won the match. When there is a tie, every player with the highest score wins
     * @param playerInd {@code int} index of the player
     * @return {@code true} if the match ended and this player has the highest score, {@code false} otherwise
     */
    public boolean isWinner(int playerInd) {
        return this.ended && getFinalScore(playerInd) == getWinningScore();
    }

    /**
     * Get the Heir name from a player
     * @param playerInd {@code int} index of the player that you want to get the Heir name
     * @return {@code String} name of the Heir from that player
     */
    public String getPlayerHeirName(int playerInd) { return data.getPlayerHeirName(playerInd); }

    /*
     * ============================================
     * ============== Save & Restore ==============
     * ============================================
     */

    /**
     * Restore the turn state of the controller when loading a saved match.
     * Nothing changes if the player index is invalid
     * @param activePlayerInd {@code int} index of the player whose turn it was when the match was saved
     * @param column {@code ColumnColor} column that player had chosen, {@code null} if the turn had not started
     */
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

    /**
     * Restore the life cycle of the match (votes and last round flags) when loading a saved match.
     * The match is always restored as not ended
     * @param savedTotalVotes {@code int} amount of votes saved, negative values become 0
     * @param savedEndRound {@code boolean} {@code true} if the end of the match had already been triggered
     * @param savedFinalTurn {@code boolean} {@code true} if the match was already in its last round
     */
    public void restoreMatchLifecycle(int savedTotalVotes, boolean savedEndRound, boolean savedFinalTurn) {
        this.totalVotes = Math.max(0, savedTotalVotes);
        this.endRoundToFinalTurn = savedEndRound;
        this.finalTurn = savedFinalTurn;
        this.ended = false;
    }

    /*
     * ============================================
     * ============ Map & Display data ============
     * ============================================
     */

    /**
     * Get the amount of resources available on that province
     * @param provinceId {@code String} of the identifier for the province
     * @return {@code int} the amount of resource if the id is a valid province, 0 otherwise
     */
    public int getProvinceResourceCount(String provinceId) { return data.getProvinceResourceCount(provinceId); }

    /**
     * Get the yurt color from the node
     * @param nodeId {@code String} of the node id that have the yurt
     * @return {@code PlayerColor} of the yurt in that node (if it has one), {@code null} otherwise
     */
    public PlayerColor getYurtColor(String nodeId) { return data.getyurtColor(nodeId); }

    /**
     * Get the province id of where Khan is at the moment
     * @return {@code String} with the province id of where Khan is at the moment
     */
    public String getKhanProvinceId() { return data.getKhanProvinceId(); }

    /**
     * Get all the the province ids that can have a Khan on it
     * @return {@code List<String>} containing all the province ids that may have a Khan on it
     */
    public List<String> getKhanProvincesIds() { return data.getKhanProvincesIds(); }

    /**
     * Verify if Khan is at the upgrade area
     * @return {@code true} if Khan is at the upgrade area, {@code false} otherwise
     */
    public boolean isKhanAtUpgradeArea() { return data.isKhanAtUpgradeArea(); }

    /**
     * Get all the coordinates for each node so it shows without any error on the view
     * @return {@code HashMap<String, int[]>} mapping each nodeId to its coordinate to show on the view.
     * The first position is the x coordinate, the second is the y.
     */
    public HashMap<String, int[]> getNodeCoordinates() { return data.getNodeCoordinates(); }

    /**
     * Get all the coordinates of the provinces to show in the view
     * @return {@code HashMap<String, int[]>} mapping each province id to a coordinate.
     * The first element in the array is the x coordinate and the second is the y coordinate
     */
    public HashMap<String, int[]> getProvinceCoordinates() { return data.getProvinceCoordinates(); }

    /**
     * Get all the coordinates that Khan can show up
     * @return {@code HashMap<String, int[]>} mapping each province id to the khan display position.
     * The first element of the array is the x coordinates, the second is the y
     */
    public HashMap<String, int[]> getKhanCoordinates() { return data.getKhanCoordinates(); }

    /**
     * Get all the coordinates of the cities to show them on the display
     * @return {@code HashMap<String, int[]>} mapping each city name to its coordinate.
     * The first element of the array is the x coordinate, the second is the y
     */
    public HashMap<String, int[]> getCitiesCoordinates() { return data.getCitiesCoordinates(); }

    /**
     * Get all the city images path
     * @return {@code HashMap<String, String>} mapping each city name to its image path
     */
    public HashMap<String, String> getCitiesImgPath() { return data.getCitiesImgPath(); }

    /**
     * Get all the cities that are available to be sacked
     * @return {@code List<String>} containing all the names of the cities that can be sacked
     */
    public List<String> getCitiesToSack() { return data.getCitiesToSack(); }

    /**
     * Get all of the city's treasure that can be sacked
     * @param cityName {@code String} name of the city to check its treasures
     * @return {@code List<String>} containing all the names of the treasures left to be sacked
     */
    public List<String> getCityTreasures(String cityName) { return data.getCityTreasures(cityName); }

    /*
     * ============================================
     * ================ Counselors ================
     * ============================================
     */

    /**
     * Get the openCounselor Ids from the game
     * @return {@code List<String>} containing all the ids for the open counselors in match
     */
    public List<String> getGameOpenCounselorsIds() { return data.getGameOpenCounselorIds(); }

    /**
     * Get the names of the candidates for private counselor for the current player
     * @return {@code List<String>} containing the 2 names for the private counselor
     */
    public List<String> getPrivateCounselorCandidates() { return data.getPrivateCounselorCandidates(); }

    /**
     * Action of the current player choosing his private counselor
     * @param name {@code String} name of the private counselor card chosen
     * @return {@code true} if the card could be assigned to the player, {@code false} otherwise
     */
    public boolean choosePrivateCounselor(String name) { return data.choosePrivateCounselor(this.currentPlayer, name); }

    /**
     * Get the name of the Heir who have completed that openCounselor
     * @param cardInd {@code int} index of the open counselor
     * @return {@code String} of the name of the Heir who has completed, {@code null} if no one completed
     */
    public String getOpenCounselorOwnerName(int cardInd) { return data.getOpenCounselorOwnerName(cardInd); }

    /*
     * ============================================
     * ============== Match status ================
     * ============================================
     */

    /**
     * Get how many players there are in the game
     * @return {@code int} how many players there are, 0 if there is no game started
     */
    public int getTotalPlayers() { return data != null ? data.getPlayerCount() : 0; }

    /**
     * Verify if the match has ended
     * @return {@code true} if the last round is over and the final scores were calculated, {@code false} otherwise
     */
    public boolean getGameHasEnded() { return this.ended; }

    /**
     * Verify if the match is in its last round
     * @return {@code true} if the match is in the last round (the round after the end of the game was triggered),
     * {@code false} otherwise
     */
    public boolean getLastRound() { return this.finalTurn; }
}