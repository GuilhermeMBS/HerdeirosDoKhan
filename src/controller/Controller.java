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
 * Coordinates match setup, turn life cycle, counselor claims, scoring,
 * and player actions between View and Model.
 */
public class Controller {

    /*
     * ============================================
     * ============== Turn variables ==============
     * ============================================
     */
    
	private int currentPlayer = 0;

	private boolean finalTurn = false;
    private boolean endRoundToFinalTurn = false;
    private boolean ended = false;

    private boolean firstSackCity = true;
    private ColumnColor chosenColumn = null;

    /*
     * ============================================
     * ============== Game variables ==============
     * ============================================
     */
    
    private int totalVotes = 0;
    private int maxVotes = 10;

    /*
     * ============================================
     * ================ Singletons ================
     * ============================================
     */
    
    private static Controller controller = null;
    private GameData data = null;

    private Controller() { }

    /**
     * Creates/gets the instance of the controller (Singleton)
     * @return {@code Controller} instance
     */
    public static Controller getController() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    /*
     * ============================================
     * ============= Observer methods =============
     * ============================================
     */
    
    /**
     * Register the observer to the array in the observed. So it can notify it
     * @param o instance of {@code ObservadorIF}
     */
    public void registra(ObservadorIF o) {
        GameData.getData().add(o);
    }

    /*
     * ============================================
     * ================ Start Game ================
     * ============================================
     */
    
    /**
     * Initializes the game, creating all player's heirs and giving them the amount of coins 
     * @param chosenHeirs {@code List<String>} of the names of the Heirs
     * @param alternativeKarakorum {@code boolean}. {@code true} if the game is using the alternative central node
     * @return {@code true} if it could create the game. {@code false} otherwise.
     */
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

    /**
     * 
     * @param indPlayer
     * @param color
     * @param heirName
     * @return
     */
    public boolean setHeirPlayer(int indPlayer, PlayerColor color, String heirName) {
        if (getTotalPlayers() < 2) {
            return false;
        }
        return data.assignPlayer(indPlayer, color, heirName, (indPlayer >= 2) ? 2 : 1);
    }
    
    /*
     * ============================================
     * =============== Turn methods ===============
     * ============================================
     */
    
    /**
     * 
     * @param column
     * @param pawnIndex
     * @return
     */
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
    
    /**
     * 
     * @return
     */
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
    
    /**
     * 
     * @param playerInd
     * @return
     */
    public List<String> getReachableNodeIds(int playerInd) { return data.getReachableNodeIds(playerInd); }
    
    /**
     * 
     * @param playerInd
     * @return
     */
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
    
    /*
     * ============================================
     * ============== Player methods ==============
     * ============================================
     */
    
    /**
     * 
     * @return
     */
    public int getCurrentPlayerIndex() { return this.currentPlayer; }
    
    /**
     * 
     * @return
     */
    public PlayerColor getCurrentPlayerColor() { return data.getPlayerColor(this.currentPlayer); }
    
    /**
     * 
     * @return
     */
    public ColumnColor getChosenColumn() { return this.chosenColumn; }
    
    /**
     * 
     * @return
     */
    public String getCurrentHeirName() { return data.getPlayerHeirName(this.currentPlayer); }
    
    /**
     * 
     * @return
     */
    public int getCurrentPlayerMovementsLeft() { return data.getMovementsLeft(this.currentPlayer); }
    
    /**
     * 
     * @return
     */
    public int getCurrentPlayerHandsLeft() { return data.getHandsLeft(this.currentPlayer); }
    
    /**
     * 
     * @return
     */
    public int getCurrentPlayerYurtsLeft() { return data.getPlayerYurtsLeft(this.currentPlayer); }

    /**
     * 
     * @return
     */
    public boolean isKhanPending() { return data.isKhanPending(this.currentPlayer); }
    
    /**
     * 
     * @param trib
     * @return
     */
    public int[] getCurrentPlayerTributeAmount(TributeType trib) { return data.getPlayerTributeAmount(currentPlayer, trib); }

    /**
     * 
     * @param type
     * @return
     */
    public int getCurrentPlayerTreasure(TreasureType type) { return data.getPlayerTreasure(this.currentPlayer, type); }
    
    /**
     * 
     * @return
     */
    public int getCurrentPlayerVotes() { return data.getPlayerVotes(this.currentPlayer); }
        
    /**
     * 
     * @param playerInd
     * @return
     */
    public String getPlayerNodeId(int playerInd) { return data.getPlayerNodeId(playerInd); }
    
    /**
     * 
     * @param playerInd
     * @param nodeId
     */
    public void setPlayerNodeId(int playerInd, String nodeId) { data.setPlayerNodeId(playerInd, nodeId); }
    
    /**
     * 
     * @param column
     * @return
     */
    public boolean isColumnFree(ColumnColor column) { return data.isColumnFree(this.currentPlayer, column); }
    
    /**
     * 
     * @return
     */
    public String getCurrentPlayerPrivateCounselorName() { return data.getPlayerPrivateCounselorName(this.currentPlayer); }
    
    
    /*
     * ============================================
     * ============== Action methods ==============
     * ============================================
     */
    
    /**
     * 
     * @param nodeId
     * @return
     */
    public boolean createMoveAction(String nodeId) { return data.MovePawn(this.currentPlayer, nodeId); }
    
    /**
     * 
     * @param nodeId
     * @return
     */
    public boolean createYurtAction(String nodeId) { return data.placeYurtOnNode(this.currentPlayer, nodeId); }

    /**
     * 
     * @param cityOrNodeId
     * @param indTreasure
     * @return
     */
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

    /**
     * 
     * @param provinceId
     * @return
     */
    public boolean createKhanAction(String provinceId) { return data.moveKhan(this.currentPlayer, provinceId); }
    
    /**
     * 
     * @param provinceId
     * @return
     */
    public boolean createTributeAction(String provinceId) {
        return data.harvestProvinceResource(this.currentPlayer, data.getPlayerNodeId(currentPlayer), provinceId);
    }
    
    /**
     * 
     * @param nodeId
     * @return
     */
    public boolean createTradeAction(String nodeId) {
        boolean traded = data.executeTrade(this.currentPlayer, nodeId);
        if (traded) {
            this.totalVotes++;
        }
        return traded;
    }

//    public boolean claimOpenCounselor(int cardInd) {
//        boolean claimed = data.setOpenCounselorDone(cardInd, this.currentPlayer);
//        if (claimed) {
//            this.totalVotes++;
//        }
//        return claimed;
//    }

    /*
     * ============================================
     * ============== Points methods ==============
     * ============================================
     */
    
    /**
     * 
     * @return
     */
    public int getTotalVotes() { return this.totalVotes; }
    
    private void calculatePoints() {
        for (int i = 0; i < getTotalPlayers(); i++) {
            data.calculatePlayerTotalScore(i);
        }
    }

    /*
     * ============================================
     * ================= Load Game ================
     * ============================================
     */

    public void restoreControllerTurnState(int activePlayerInd, ColumnColor column) {
        if (activePlayerInd >= 0 && activePlayerInd < getTotalPlayers()) {
            this.currentPlayer = activePlayerInd;
            this.chosenColumn = column;
            data.setCurrentPlayer(this.currentPlayer);
        }
    }
    
    /*
     * =============================================
     * ================ Map methods ================
     * =============================================
     */

    /**
     * 
     * @param provinceId
     * @return
     */
    public int getProvinceResourceCount(String provinceId) { return data.getProvinceResourceCount(provinceId); }
    
    /**
     * 
     * @param nodeId
     * @return
     */
    public PlayerColor getYurtColor(String nodeId) { return data.getyurtColor(nodeId); }
    
    /**
     * 
     * @return
     */
    public String getKhanProvinceId() { return data.getKhanProvinceId(); }
    
    /**
     * 
     * @return
     */
    public List<String> getKhanProvincesIds() { return data.getKhanProvincesIds(); }
    
    /**
     * 
     * @return
     */
    public boolean isKhanAtUpgradeArea() { return data.isKhanAtUpgradeArea(); }
    
    /**
     * 
     * @return
     */
    public HashMap<String, int[]> getNodeCoordinates() { return data.getNodeCoordinates(); }
    
    /**
     * 
     * @return
     */
    public HashMap<String, int[]> getProvinceCoordinates() { return data.getProvinceCoordinates(); }
    
    /**
     * 
     * @return
     */
    public HashMap<String, int[]> getKhanCoordinates() { return data.getKhanCoordinates(); }
    
    /**
     * 
     * @return
     */
    public HashMap<String, int[]> getCitiesCoordinates() { return data.getCitiesCoordinates(); }
    
    /**
     * 
     * @return
     */
    public List<String> getCitiesToSack() { return data.getCitiesToSack(); }
    
    /**
     * 
     * @param cityName
     * @return
     */
    public List<String> getCityTreasures(String cityName) { return data.getCityTreasures(cityName); }
    
    /**
     * 
     * @param nodeId
     * @return
     */
    public String getCityNameForNode(String nodeId) { return data.getCityNameForNode(nodeId); }
    
    /**
     * 
     * @return
     */
    public List<String> getCitiesNames() { return data.getCityNames(); }
    
    /*
     * ============================================
     * ========== Open Counselor methods ==========
     * ============================================
     */
    
    /**
     * 
     * @return
     */
    public List<String> getOpenCounselorsIds() { return data.getOpenCounselorsId(); }
    
    /**
     * 
     * @return
     */
    public List<String> getGameOpenCounselorsIds() { return data.getGameOpenCounselorIds(); }
    
    /*
     * =============================================
     * ============ Game status methods ============
     * =============================================
     */
    
    /**
     * 
     * @return
     */
    public int getTotalPlayers() { return data != null ? data.getPlayerCount() : 0; }
    
    /**
     * 
     * @param cardInd
     * @return
     */
    public String getOpenCounselorOwnerName(int cardInd) { return data.getOpenCounselorOwnerName(cardInd); }

    /**
     * 
     * @return
     */
    public boolean getGameHasEnded() { return this.ended; }
    
    /**
     * 
     * @return
     */
    public boolean getLastRound() { return this.finalTurn; }
}