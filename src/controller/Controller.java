package controller;

import java.util.HashMap;
import java.util.List;

import model.ColumnColor;
import model.GameData;
import model.ObservadorIF;
import model.PlayerColor;
import model.TreasureType;
import model.TributeType;

/**
 * Coordinates match setup, turn life cycle, counselor claims, upgrades, scoring,
 * and player actions between View and Model.
 */
public class Controller {

    private int currentPlayer = 0;

    private boolean finalTurn = false;
    private boolean endRoundToFinalTurn = false;
    private boolean ended = false;

    private boolean firstSackCity = true;
    private ColumnColor chosenColumn = null;

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

    public void registra(ObservadorIF o) {
        GameData.getData().add(o);
    }

    public boolean setupMatch(List<String> chosenHeirs, boolean alternativeKarakorum) {
        startGame(alternativeKarakorum);
        updateMaxVotes(chosenHeirs.size());
        return data.setupMatch(chosenHeirs, alternativeKarakorum);
    }

    private boolean startGame(boolean alternativeKarakorum) {
        data = GameData.createData(alternativeKarakorum);
        this.currentPlayer = 0;
        this.finalTurn = false;
        this.endRoundToFinalTurn = false;
        this.ended = false;
        this.totalVotes = 0;
        this.firstSackCity = true;
        this.chosenColumn = null;
        return true;
    }

    private void updateMaxVotes(int playersQty) {
        if (playersQty == 2) this.maxVotes = 10;
        else if (playersQty == 3) this.maxVotes = 14;
        else if (playersQty == 4) this.maxVotes = 16;
        else this.maxVotes = 17;
    }

    public boolean setHeirPlayer(int indPlayer, PlayerColor color, String heirName) {
        if (getTotalPlayers() < 2) {
            return false;
        }
        return data.assignPlayer(indPlayer, color, heirName, (indPlayer >= 2) ? 2 : 1);
    }

    public boolean startTurn(ColumnColor column, int pawnIndex) {
        if (this.chosenColumn != null) {
            return false;
        }
        if (!data.startTurn(this.currentPlayer, column, pawnIndex)) {
            return false;
        }
        this.chosenColumn = column;
        return true;
    }

    public boolean endTurn() {
        int total = getTotalPlayers();
        if (this.ended || total == 0) {
            return false;
        }

        if (endGameConditionsMet() && !this.endRoundToFinalTurn) {
            this.endRoundToFinalTurn = true;
        }

        data.endTurn(this.currentPlayer);
        this.chosenColumn = null;
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
    public PlayerColor getCurrentPlayerColor() { return data.getPlayerColor(this.currentPlayer); }
    public ColumnColor getChosenColumn() { return this.chosenColumn; }
    public String getCurrentHeirName() { return data.getPlayerHeirName(this.currentPlayer); }
    public int getCurrentPlayerMovementsLeft() { return data.getMovementsLeft(this.currentPlayer); }
    public int getCurrentPlayerHandsLeft() { return data.getHandsLeft(this.currentPlayer); }
    public int getCurrentPlayerYurtsLeft() { return data.getPlayerYurtsLeft(this.currentPlayer); }
    public boolean isKhanPending() { return data.isKhanPending(this.currentPlayer); }
    public int[] getCurrentPlayerTributeAmount(TributeType trib) { return data.getPlayerTributeAmount(currentPlayer, trib); }
    public int getCurrentPlayerTreasure(TreasureType type) { return data.getPlayerTreasure(this.currentPlayer, type); }
    public int getCurrentPlayerVotes() { return data.getPlayerVotes(this.currentPlayer); }
    public String getPlayerNodeId(int playerInd) { return data.getPlayerNodeId(playerInd); }
    public void setPlayerNodeId(int playerInd, String nodeId) { data.setPlayerNodeId(playerInd, nodeId); }
    public boolean isColumnFree(ColumnColor column) { return data.isColumnFree(this.currentPlayer, column); }
    public String getCurrentPlayerPrivateCounselorName() { return data.getPlayerPrivateCounselorName(this.currentPlayer); }

    public boolean createMoveAction(String nodeId) { return data.MovePawn(this.currentPlayer, nodeId); }
    public boolean createYurtAction(String nodeId) { return data.placeYurtOnNode(this.currentPlayer, nodeId); }

    public boolean createSackAction(String cityOrNodeId, int indTreasure) {
        if (indTreasure < 0 || indTreasure > 3) {
            return false;
        }
        int swordCost = this.firstSackCity ? 1 : 2;
        boolean success = data.attackCityForTreasure(this.currentPlayer, cityOrNodeId, indTreasure, swordCost);
        if (success) {
            this.firstSackCity = false;
        }
        return success;
    }

    public boolean createKhanAction(String provinceId) { return data.moveKhan(this.currentPlayer, provinceId); }

    public boolean createTributeAction(String provinceId) {
        return data.harvestProvinceResource(this.currentPlayer, data.getPlayerNodeId(currentPlayer), provinceId);
    }

    public boolean createTradeAction(String nodeId) {
        boolean traded = data.executeTrade(this.currentPlayer, nodeId);
        if (traded) {
            this.totalVotes++;
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
        return data.buyUpgradeFromMarket(this.currentPlayer, slotIndex);
    }

    public boolean claimFreeUpgradeFromMarket(int slotIndex) {
        return data.claimFreeUpgradeFromMarket(this.currentPlayer, slotIndex);
    }

    public boolean claimFreeUpgradeFromDeck() {
        return data.claimFreeUpgradeFromDeck(this.currentPlayer);
    }

    /*
     * ============================================
     * ============== Points methods ==============
     * ============================================
     */

    public int getTotalVotes() { return this.totalVotes; }

    private void calculatePoints() {
        for (int i = 0; i < getTotalPlayers(); i++) {
            data.calculatePlayerTotalScore(i);
        }
    }

    public void restoreControllerTurnState(int activePlayerInd, ColumnColor column) {
        if (activePlayerInd >= 0 && activePlayerInd < getTotalPlayers()) {
            this.currentPlayer = activePlayerInd;
            this.chosenColumn = column;
            data.setCurrentPlayer(this.currentPlayer);
        }
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
    public List<String> getCitiesToSack() { return data.getCitiesToSack(); }
    public List<String> getCityTreasures(String cityName) { return data.getCityTreasures(cityName); }
    public String getCityNameForNode(String nodeId) { return data.getCityNameForNode(nodeId); }
    public List<String> getCitiesNames() { return data.getCityNames(); }

    public List<String> getOpenCounselorsIds() { return data.getOpenCounselorsId(); }
    public List<String> getGameOpenCounselorsIds() { return data.getGameOpenCounselorIds(); }

    public List<String> getPrivateCounselorCandidates() { return data.getPrivateCounselorCandidates(); }
    public boolean choosePrivateCounselor(String name) { return data.choosePrivateCounselor(this.currentPlayer, name); }

    public int getTotalPlayers() { return data != null ? data.getPlayerCount() : 0; }
    public String getOpenCounselorOwnerName(int cardInd) { return data.getOpenCounselorOwnerName(cardInd); }
    public boolean getGameHasEnded() { return this.ended; }
    public boolean getLastRound() { return this.finalTurn; }
}
