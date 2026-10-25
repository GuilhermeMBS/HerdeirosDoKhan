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
	
	
	public static GameData getData() { return data; }
	
	
	/*
	 * 
	 * Map methods
	 * 
	 */
	
	// Helper functions (private)
	
	private Map getMap() { return Map.getMap(altKarakorum); }
	
	private Node nodeById(String id) { return getMap().getNodeById(id); }
	
	
	private Province provinceById(String id) {
		try {
			return getMap().getProvince(ProvinceId.valueOf(id).ordinal());
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}

	
	// Methods to get information on the map
	
	public boolean isAlternativeKarakorum() { return altKarakorum; }
	
	public boolean areAllCityConquered() { return Map.getMap(altKarakorum).allCityConquered(); }
	
	public int getAmountProvinces() { return getMap().getAmountProvinces(); }
	
	public int getAmountCity() { return getMap().getAmountCity(); }
	
	public List<String> getAllNodeIds() { return getMap().getAllNodeIds(); }
	
	public boolean isKhanAtUpgradeArea() { return getMap().getKhan().isAtUpgradesArea(); }
	
	public int getPlayerCount() { return players == null ? 0 : players.length; }
	
	public String getPlayerNodeId(int playerInd) { return getMap().getNodeId(players[playerInd].getCurrentNode()); }
	
	
	public List<String> getAllProvinceIds() {
		List<String> ids = new ArrayList<>();
		for (ProvinceId id : ProvinceId.values()) ids.add(id.name());
		return ids;
	}
	
	
	public boolean nodeHasYurt(String nodeId) {
		Node node = getMap().getNodeById(nodeId);
		
		if (node == null) return false;
		return node.hasYurt();
	}
	
	
	public int getProvinceResourceCount(String ProvinceId) {
		Province p = provinceById(ProvinceId);
		return p != null ? p.resourceCount : 0;
	}
	
	
	public String getKhanProvinceId() {
		Province current = getMap().getKhan().currentProvince;
		if (current == null) return null;
		
		for (ProvinceId id : ProvinceId.values()) 
			if (getMap().getProvince(id.ordinal()) == current) return id.name();
		return null;
	}
	
	
	// Methods to set some values
	
	public void setProvinceResourceCount(String provinceId, int qty) {
		Province p = provinceById(provinceId);
		if (p != null) p.resourceCount = Math.max(0, Math.min(3, qty));
	}
	
	
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
	
	
	public boolean setupMatch(List<String> heirNames, boolean alt) {
		if (players != null) return false;
		
		if (!createArrayPlayers(heirNames.size())) return false;
		
		for (int i = 0; i < heirNames.size(); i++)
			assignPlayer(i, PlayerColor.values()[i], heirNames.get(i), (i >= 2) ? 2 : 1);
		return true;
	}
	
	
	public PlayerColor getPlayerColor(int ind) { return players[ind].getColor(); }

	public Heir getPlayerHeir(int ind) { return players[ind].getHeir(); }
	
	public int getPlayerInd(String name) {
		for (int i = 0; i < players.length; i++) {
			if (players[i].getName().equalsIgnoreCase(name)) return i;
		}
		return -1;
	}
	
	
	public boolean MovePawn(int playerInd, String nodeId) {
		Node target = nodeById(nodeId);
		Player p = players[playerInd];
		if (target == null || !p.getCurrentNode().getNeighbors().contains(target)) return false;
		
		newMoveAction(p, target, 1);
		return doAction();
	}
	
	
	public boolean placeYurtOnNode(int playerInd, String nodeId) {
		Node n = nodeById(nodeId);
		newYurtAction(playerInd, n);
		return doAction();
	}
	
	
	public boolean harvestProvinceResource(int playerInd, String nodeId, String provinceId) {
		Node n = nodeById(nodeId);
		Province province = provinceById(provinceId);
		
		if (n == null || province == null || !n.getProvinces().contains(province)) return false;
		
		newTributeAction(players[playerInd], n, province);
		return doAction();
	}
	
	
	public boolean attackCityForTreasure(int playerInd, String cityName, int treasureInd, int qtySword) {
		int cityInd = getMap().getCityInd(cityName);
		if (cityInd < 0) return false;
		
		newSackAction(playerInd, players[playerInd].getCurrentNode(), cityInd, qtySword, treasureInd);
		return doAction();
	}
	
	
	public boolean moveKhan(int playerInd, String provinceId) {
		Province p;
		if ("UPGRADES".equalsIgnoreCase(provinceId)) p = null;
		else p = provinceById(provinceId);
		
		newKhanAction(playerInd, players[playerInd].getCurrentNode(), p);
		return doAction();
	}
	
	
	public boolean executeTrade(int playerInd, String nodeInd) {
		Node n = nodeById(nodeInd);
		if (n instanceof SingleNode) return false;
		
		TradingPost p;
		if (n instanceof CentralNode) p = ((CentralNode) n).getTradingPost();
		else p = ((DoubleNode) n).getTradingPost();
		
		newTradeAction(playerInd, p);
		return doAction();
	}


	/*
	 *
	 * Turn methods (used by the player board screen)
	 *
	 */

	public boolean startTurn(int playerInd, ColumnColor column, int pawnIndex) {
		return players[playerInd].startTurn(column, pawnIndex);
	}


	public void endTurn(int playerInd) { players[playerInd].endTurn(); }


	public boolean isColumnFree(int playerInd, ColumnColor column) { return players[playerInd].canActivate(column); }


	public int getMovementsLeft(int playerInd) { return players[playerInd].getMovementsLeft(); }

	
	/*
	 * 
	 * Actions methods
	 * 
	 */
	
	private boolean newMoveAction(Player p, Node node, int qty) {
		action = new Action(p, node, new MovementAction(qty));
		return true;
	}
	
	
	private boolean newYurtAction(int playerInd, Node nodeInd) {
		action = new Action(players[playerInd], nodeInd, new YurtAction());
		return true;
	}
	
	
	private boolean newKhanAction(int playerInd, Node nodeInd, Province province) {
		action = new Action(players[playerInd], nodeInd, new KhanAction(province, getMap().getKhan()));
		return true;
	}
	
	
	private boolean newSackAction(int playerInd, Node nodeInd, int cityInd, int qtySword, int indResource) {
		action = new Action(players[playerInd], nodeInd, new SackAction(getMap().getCity(cityInd), qtySword, indResource));
		return true;
	}
	
	
	private boolean newTributeAction(Player p, Node nodeInd, Province province) {
		action = new Action(p, nodeInd, new TributeAction(province));
		return true;
	}
	
	
	private boolean newTradeAction(int playerInd, TradingPost post) {		
		action = new Action(players[playerInd], players[playerInd].getCurrentNode(), new TradeAction(post));
		return true;
	}
	
	
	private boolean doAction() {
		DebugReturn ret = action.doAction();
		
		if (ret != DebugReturn.SUCCESS) return false;
		return true;
	}
}
