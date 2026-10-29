package controller;

import java.util.List;

import model.ColumnColor;
import model.GameData;
import model.PlayerColor;

/**
 * Coordinates match setup, turn lifecycle, and player actions between View and Model.
 */
public class Controller {
    private int currentPlayer = 0;

    private boolean finalTurn = false;
    private boolean endRoundToFinalTurn = false;
    private boolean ended = false;

    private int totalVotes = 0;
    private int maxVotes = 10;

    private boolean firstSackCity = true;
    private ColumnColor chosenColumn = null;

    private static Controller controller = null;
    private GameData data = null;

    private Controller() {
    }

    public static Controller getController() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
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

    public boolean setupMatch(List<String> chosenHeirs, boolean alternativeKarakorum) {
        startGame(alternativeKarakorum);
        updateMaxVotes(chosenHeirs.size());
        return data.setupMatch(chosenHeirs, alternativeKarakorum);
    }

    private void updateMaxVotes(int playersQty) {
        if (playersQty == 2) this.maxVotes = 10;
        else if (playersQty == 3) this.maxVotes = 14;
        else if (playersQty == 4) this.maxVotes = 16;
        else this.maxVotes = 17;
    }

    public boolean setPlayerAmount(int playersQty) {
        if (playersQty < 2 || playersQty > 5) {
            return false;
        }
        updateMaxVotes(playersQty);
        return data.createArrayPlayers(playersQty);
    }

    public boolean setHeirPlayer(int indPlayer, PlayerColor color, String heirName) {
        if (getTotalPlayers() < 2) {
            return false;
        }
        return data.assignPlayer(indPlayer, color, heirName, (indPlayer >= 2) ? 2 : 1);
    }

    private void nextPlayer() {
        int total = getTotalPlayers();
        if (total > 0) {
            this.currentPlayer = (this.currentPlayer + 1) % total;
        }
        this.firstSackCity = true;
    }

    private boolean endGameConditionsMet() {
        return data.areAllCityConquered() || this.totalVotes >= this.maxVotes;
    }

    private void calculatePoints() {
        // End-of-match scoring hook for Iteration 3/4.
    }

    public boolean createMoveAction(String nodeId) {
        return data.MovePawn(this.currentPlayer, nodeId);
    }

    public boolean createYurtAction(String nodeId) {
        return data.placeYurtOnNode(this.currentPlayer, nodeId);
    }

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

    public boolean createKhanAction(String provinceId) {
        return data.moveKhan(this.currentPlayer, provinceId);
    }

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
        data.setMovementsLeft(this.currentPlayer, 2);

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

    public void setTesterMovements(int steps) {
        data.setMovementsLeft(this.currentPlayer, steps);
    }
    
    
    public List<String> getReachableNodeIds(int playerInd) { return data.getReachableNodeIds(playerInd); }
    
    
    public int getProvinceResourceCount(String provinceId) {
    	return data.getProvinceResourceCount(provinceId);
    }
    
    
    public boolean nodeHasYurt(String nodeId) {
    	return data.nodeHasYurt(nodeId);
    }
    
    
    public String getKhanProvinceId() {
    	return data.getKhanProvinceId();
    }
    
    
    public String getPlayerNodeId(int ind) {
    	return data.getPlayerNodeId(ind);
    }
    
    
    public void setPlayerNodeId(int playerInd, String nodeId) {
    	data.setPlayerNodeId(playerInd, nodeId);
    }

    public int getCurrentPlayerIndex() { return this.currentPlayer; }
    public String getCurrentPlayerName() { return data.getPlayerName(this.currentPlayer); }
    public int getTotalVotes() { return this.totalVotes; }
    public int getTotalPlayers() { return data.getPlayerCount(); }
    public PlayerColor getCurrentPlayerColor() { return data.getPlayerColor(this.currentPlayer); }
    public ColumnColor getChosenColumn() { return this.chosenColumn; }
    public boolean isColumnFree(ColumnColor column) { return data.isColumnFree(this.currentPlayer, column); }
    public String getCurrentHeirName() { return data.getPlayerHeirName(this.currentPlayer); }
    public int getMovementsLeft() { return data.getMovementsLeft(this.currentPlayer); }
    public boolean getGameHasEnded() { return this.ended; }
    public boolean getLastRound() { return this.finalTurn; }
    public boolean isKhanAtUpgradeArea() { return data.isKhanAtUpgradeArea(); }
}
