package controller;

import java.util.List;
import model.ColumnColor;
import model.GameData;
import model.Heir;
import model.PlayerColor;

/**
 * Coordinates match setup, turn lifecycle, and player actions between View and Model.
 */
public class Controller {
    private int qtyPlayers = 0;
    private int currentPlayer = 0;

    private boolean finalTurn = false;
    private boolean endRoundToFinalTurn = false;
    private boolean ended = false;

    private int totalVotes = 0;
    private int maxVotes = 10;

    private boolean firstSackCity = true;
    private ColumnColor chosenColumn = null;
    private GameData data;

    private static Controller controller = null;

    private Controller() {
        this.data = GameData.getData();
    }

    public static Controller getController() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    public boolean startGame(boolean alternativeKarakorum) {
        this.data = GameData.createData(alternativeKarakorum);
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
        if (!setPlayerAmount(chosenHeirs.size())) {
            return false;
        }
        return this.data.setupMatch(chosenHeirs, alternativeKarakorum);
    }

    public boolean setPlayerAmount(int playersQty) {
        if (playersQty < 2 || playersQty > 5) {
            return false;
        }
        this.qtyPlayers = playersQty;
        this.data.createArrayPlayers(this.qtyPlayers);

        if (this.qtyPlayers == 2) this.maxVotes = 10;
        else if (this.qtyPlayers == 3) this.maxVotes = 14;
        else if (this.qtyPlayers == 4) this.maxVotes = 16;
        else this.maxVotes = 17;

        return true;
    }

    public boolean setHeirPlayer(int indPlayer, PlayerColor color, String heirName) {
        if (this.qtyPlayers < 2 || this.data == null) {
            return false;
        }
        return this.data.assignPlayer(indPlayer, color, heirName, (indPlayer >= 2) ? 2 : 1);
    }

    private void nextPlayer() {
        if (this.qtyPlayers > 0) {
            this.currentPlayer = (this.currentPlayer + 1) % this.qtyPlayers;
        }
        this.firstSackCity = true;
    }

    private boolean endGameConditionsMet() {
        return this.data.areAllCityConquered() || this.totalVotes >= this.maxVotes;
    }

    private void calculatePoints() {
        // End-of-match scoring hook for Iteration 3/4.
    }

    public boolean createMoveAction(String nodeId) {
        return this.data.MovePawn(this.currentPlayer, nodeId);
    }

    public boolean createYurtAction(String nodeId) {
        return this.data.placeYurtOnNode(this.currentPlayer, nodeId);
    }

    public boolean createSackAction(String cityOrNodeId, int indTreasure) {
        if (indTreasure < 0 || indTreasure > 3) {
            return false;
        }
        int swordCost = this.firstSackCity ? 1 : 2;
        boolean success = this.data.attackCityForTreasure(this.currentPlayer, cityOrNodeId, indTreasure, swordCost);
        if (success) {
            this.firstSackCity = false;
        }
        return success;
    }

    public boolean createKhanAction(String provinceId) {
        return this.data.moveKhan(this.currentPlayer, provinceId);
    }

    public boolean createTributeAction(String nodeId, String provinceId) {
        return this.data.harvestProvinceResource(this.currentPlayer, nodeId, provinceId);
    }

    public boolean createTradeAction(String nodeId) {
        boolean traded = this.data.executeTrade(this.currentPlayer, nodeId);
        if (traded) {
            this.totalVotes++;
        }
        return traded;
    }

    public boolean endTurn() {
        if (this.ended || this.qtyPlayers == 0) {
            return false;
        }

        if (endGameConditionsMet() && !this.endRoundToFinalTurn) {
            this.endRoundToFinalTurn = true;
        }

        this.data.endTurn(this.currentPlayer);
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

    public boolean startTurn(ColumnColor column, int pawnIndex) {
        if (this.chosenColumn != null) {
            return false;
        }
        if (!this.data.startTurn(this.currentPlayer, column, pawnIndex)) {
            return false;
        }
        this.chosenColumn = column;
        return true;
    }

    public void setTesterMovements(int steps) {
        this.data.setMovementsLeft(this.currentPlayer, steps);
    }

    public int getCurrentPlayerIndex() { return this.currentPlayer; }
    public String getCurrentPlayerName() { return this.data.getPlayerName(this.currentPlayer); }
    public int getTotalVotes() { return this.totalVotes; }
    public int getTotalPlayers() { return this.qtyPlayers; }
    public PlayerColor getCurrentPlayerColor() { return this.data.getPlayerColor(this.currentPlayer); }
    public ColumnColor getChosenColumn() { return this.chosenColumn; }
    public boolean isColumnFree(ColumnColor column) { return this.data.isColumnFree(this.currentPlayer, column); }
    public Heir getCurrentHeir() { return this.data.getPlayerHeir(this.currentPlayer); }
    public int getMovementsLeft() { return this.data.getMovementsLeft(this.currentPlayer); }
    public boolean getGameHasEnded() { return this.ended; }
    public boolean getLastRound() { return this.finalTurn; }
    public GameData getGameData() { return this.data; }
}
