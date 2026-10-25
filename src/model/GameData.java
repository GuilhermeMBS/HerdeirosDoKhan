package model;


import java.util.*;


public class GameData {
	private static boolean altKarakorum = false;
	private Player[] players = null;
	private static ArrayList<OpenCounselor> openCounselorCards = null;
	private Action action = null;
	
	
	private static GameData data = null;
	
	
	/*
	 * 
	 * Create the singleton
	 * 
	 */
	
	private GameData(boolean alt) {
		altKarakorum = alt;
		Map.getMap(altKarakorum);
	}
	
	
	public static GameData createData(boolean alt) {
		if (data == null)
			data = new GameData(alt);
		return data;
	}
	
	
	/*
	 * 
	 * Map methods
	 * 
	 */
	
	private Map getMap() { return Map.getMap(altKarakorum); }
	
	public boolean areAllCityConquered() { return Map.getMap(altKarakorum).allCityConquered(); }
	
	public int getAmountProvinces() { return getMap().getAmountProvinces(); }
	
	public int getAmountCity() { return getMap().getAmountCity(); }
	
	
	/*
	 * 
	 * Open Counselors methods
	 * 
	 */
	
	public static boolean createOpenCounselors() {
		if (openCounselorCards == null)
			openCounselorCards = OpenCounselor.giveCardsInGame();
		return true;
	}
	
	
	private static boolean verifyOpenCounselorCondition(OpenCounselor o, Player p) {
		int target = o.getMissionTarget();
		
		switch (o.getMissionType()) {
		case YURT:
			if (p.getAmountYurts() < target) return false;
			break;
			
		case YURTREGION:
			EnumMap<Region, Integer> qty = Map.getMap(altKarakorum).getYurtsPerRegion(p);
			for (Region region : Region.values()) {
				if (qty.get(region) < target) return false;
			}
			break;
			
		case SWORD:
			if (p.getTributeAmount(TributeType.SWORD) < target) return false;
			break;
			
		case EACHRESOURCE:
			for (TreasureType type : TreasureType.values()) {
				if (p.getTreasureAmount(type) < 1) return false;
			}
			break;
			
		case TREASURE:
			if (p.getTotalTreasures() < target) return false;
			break;
			
		case CITIESDOMINATED:
			if (p.getConqueredCities() < target) return false;
			break;
			
		case POWERUPS_LINE:
			if (p.getBoard().completeRows() < target) return false;
			break;
			
		case POWERUPS_COL:
			for (ColumnColor color : ColumnColor.values()) {
				if (p.getBoard().getColumn(color).getUpgradeCount() < target) return false;
			}
			break;
			
		case POWERUPS_SQUARE:
			if (!p.getBoard().hasCompletedSquare()) return false;
			break;
			
		case POINTS:
			if (p.getVotes() < target) return false;
			break;
			
		default:
			// Invalid mission Type
			return false;
		}
		
		return true;
	}
	
	
	public static boolean setOpenCounselorDone(int ind, Player p) {
		OpenCounselor card = openCounselorCards.get(ind);
		
		if (!verifyOpenCounselorCondition(card, p)) return false;
		
		return card.playerHasCompleted(p);
	}
	
	
	/*
	 * 
	 * Players methods
	 * 
	 */
	
	public boolean createArrayPlayers(int qtyPlayers) {
		if (players != null) return false;
		
		players = new Player[qtyPlayers];
		for (int i = 0; i < qtyPlayers; i++)
			players[i] = null;
		return true;
	}
	
	
	public boolean assignPlayer(int ind, PlayerColor color, String heirName, int amountCoins) {
		if (players[ind] != null) return false;
		
		players[ind] = new Player(color ,Heir.fromName(heirName), Map.getMap(altKarakorum).getCentralNode(), amountCoins);
		return true;
	}
	
	
	public PlayerColor getPlayerColor(int ind) { return players[ind].getColor(); }
	
	
	/*
	 * 
	 * Actions methods
	 * 
	 */
	
	public boolean newMoveAction(int playerInd, Node nodeInd, int qty) {
		action = new Action(players[playerInd], nodeInd, new MovementAction(qty));
		return true;
	}
	
	
	public boolean newYurtAction(int playerInd, Node nodeInd) {
		action = new Action(players[playerInd], nodeInd, new YurtAction());
		return true;
	}
	
	
	public boolean newKhanAction(int playerInd, Node nodeInd, int provinceInd) {
		action = new Action(players[playerInd], nodeInd, new KhanAction(getMap().getProvince(provinceInd), getMap().getKhan()));
		return true;
	}
	
	
	public boolean newSackAction(int playerInd, Node nodeInd, int cityInd, int qtySword, int indResource) {
		action = new Action(players[playerInd], nodeInd, new SackAction(getMap().getCity(cityInd), qtySword, indResource));
		return true;
	}
	
	
	public boolean newTributeAction(int playerInd, Node nodeInd, int provinceInd) {
		action = new Action(players[playerInd], nodeInd, new TributeAction(getMap().getProvince(provinceInd)));
		return true;
	}
	
	
	public boolean newTradeAction(int playerInd, boolean isCentralNode, Region region, boolean isFisrtPost) {
		TradingPost post;
		if (isCentralNode) post = ((CentralNode) getMap().getCentralNode()).getTradingPost();
		else post = getMap().getTradingPost(region, isFisrtPost);
		
		action = new Action(players[playerInd], players[playerInd].getCurrentNode(), new TradeAction(post));
		return true;
	}
	
	
	public boolean doAction() {
		DebugReturn ret = action.doAction();
		
		if (ret != DebugReturn.SUCCESS) return false;
		return true;
	}
}
