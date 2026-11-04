package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Singleton and Facade providing external access to the Model package.
 * It is also the observed object of the Observer pattern: every time the
 * state of the match changes, the screens registered as observers are notified.
 */
public class GameData implements ObservadoIF {
    /** Codes for {@link #get(int)}: index of the player whose turn it is. */
    public static final int CURRENT_PLAYER = 0;
    /** Codes for {@link #get(int)}: horses left to the player whose turn it is. */
    public static final int MOVEMENTS_LEFT = 1;
    /** Codes for {@link #get(int)}: number of players in the match. */
    public static final int PLAYER_COUNT = 2;

    private static boolean altKarakorum = false;
    private Player[] players = null;
    private static ArrayList<OpenCounselor> openCounselorCards = null;
    private Action action = null;
    private int currentPlayer = 0;

    /** Screens that are notified when the state of the match changes. */
    private final List<ObservadorIF> observers = new ArrayList<>();

    private static GameData data = null;

    private GameData(boolean alt) {
        altKarakorum = alt;
        Map.resetMap(altKarakorum);
    }

    /**
     * Ensures a single shared GameData instance across Controller and View.
     */
    public static GameData createData(boolean alt) {
        if (data == null) {
            data = new GameData(alt);
        } else {
            altKarakorum = alt;
            Map.resetMap(altKarakorum);
            data.players = null;
        }
        return data;
    }

    public static GameData getData() {
        if (data == null) {
            data = new GameData(false); // Creates a default Karakorum
        }
        return data;
    }

    /*
     *
     * Observer methods (ObservadoIF)
     *
     */

    @Override
    public void add(ObservadorIF o) {
        observers.add(o);
    }

    @Override
    public void remove(ObservadorIF o) {
        observers.remove(o);
    }

    @Override
    public int get(int i) {
        switch (i) {
            case CURRENT_PLAYER:
                return currentPlayer;
            case MOVEMENTS_LEFT:
                return getMovementsLeft(currentPlayer);
            case PLAYER_COUNT:
                return getPlayerCount();
            default:
                return -1;
        }
    }

    /** Notifies every observer that the state of the match changed. */
    private void atualiza() {
        ListIterator<ObservadorIF> li = observers.listIterator();
        while (li.hasNext()) {
            li.next().notify(this);
        }
    }

    /** The Controller tells whose turn it is now; the screens are notified. */
    public void setCurrentPlayer(int playerInd) {
        currentPlayer = playerInd;
        atualiza();
    }

    private Map getMap() {
        return Map.getMap(altKarakorum);
    }

    private Node nodeById(String id) {
        return getMap().getNodeById(id);
    }

    private Province provinceById(String id) {
        if (id == null) {
            return null;
        }
        try {
            return getMap().getProvince(ProvinceId.valueOf(id).ordinal());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isAlternativeKarakorum() { return altKarakorum; }
    public boolean areAllCityConquered() { return getMap().allCityConquered(); }
    public int getAmountProvinces() { return getMap().getAmountProvinces(); }
    public int getAmountCity() { return getMap().getAmountCity(); }
    public List<String> getAllNodeIds() { return getMap().getAllNodeIds(); }
    public boolean isKhanAtUpgradeArea() { return getMap().getKhan().isAtUpgradesArea(); }
    public int getPlayerCount() { return players == null ? 0 : players.length; }

    public String getPlayerNodeId(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return "CENTRAL";
        }
        return getMap().getNodeId(players[playerInd].getCurrentNode());
    }

    public void setPlayerNodeId(int playerInd, String nodeId) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return;
        }
        Node target = nodeById(nodeId);
        if (target != null) {
            players[playerInd].getCurrentNode().leaveNode();
            target.enterNode();
            players[playerInd].moveTo(target);
            atualiza();
        }
    }

    /**
     * Returns the list of node IDs that the specified player can currently reach.
     */
    public List<String> getReachableNodeIds(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return Collections.emptyList();
        }
        Player p = players[playerInd];
        List<String> reachable = new ArrayList<>();
        for (String nodeId : getAllNodeIds()) {
            Node target = nodeById(nodeId);
            if (target != null && p.findPath(target) != null) {
                reachable.add(nodeId);
            }
        }
        return reachable;
    }

    public List<String> getAllProvinceIds() {
        List<String> ids = new ArrayList<>();
        for (ProvinceId id : ProvinceId.values()) {
            ids.add(id.name());
        }
        return ids;
    }
    
    public List<String> getKhanProvincesIds() {
    	List<String> ret = new ArrayList<String>();
    	ret.add(ProvinceId.RUSSIA_YURT1.toString());
    	ret.add(ProvinceId.RUSSIA_COIN1.toString());
    	ret.add(ProvinceId.CHINA_YURT1.toString());
    	ret.add(ProvinceId.PERSIA_COIN1.toString());
    	
    	return ret;
    }

    public boolean nodeHasYurt(String nodeId) {
        Node node = nodeById(nodeId);
        return node != null && node.hasYurt();
    }

    public int getProvinceResourceCount(String provinceId) {
        Province p = provinceById(provinceId);
        return p != null ? p.resourceCount : 0;
    }

    public String getKhanProvinceId() {
        Province current = getMap().getKhan().currentProvince;
        if (current == null) {
            return null;
        }
        for (ProvinceId id : ProvinceId.values()) {
            if (getMap().getProvince(id.ordinal()) == current) {
                return id.name();
            }
        }
        return null;
    }
    
    public HashMap<String, int[]> getNodeCoordinates() { return getMap().getNodeCoordinates(); }
    public HashMap<String, int[]> getProvinceCoordinates() { return getMap().getProvinceCoordinates(); }
    public HashMap<String, int[]> getCitiesCoordinates() { return getMap().getCitiesCoordinates(); }
    
    public List<String> getCityNames() {
    	HashMap<String, int[]> map = this.getCitiesCoordinates();
    	List<String> ret = new ArrayList<>();
    	for (EnumMap.Entry<String, int[]> entry : map.entrySet()) {
    		ret.add(entry.getKey());
    	}
    	return ret;
    }

    public void setProvinceResourceCount(String provinceId, int qty) {
        Province p = provinceById(provinceId);
        if (p != null) {
            p.resourceCount = Math.max(0, Math.min(3, qty));
            atualiza();
        }
    }

    public static boolean createOpenCounselors() {
        if (openCounselorCards == null) {
            openCounselorCards = OpenCounselor.giveCardsInGame();
        }
        return true;
    }
    
    public List<String> getOpenCounselorsId() { return OpenCounselor.getAllOpenCounselorsIds(); }
    public List<String> getGameOpenCounselorIds() { return OpenCounselor.getOpenCounselorsIdGame(); }

    private static boolean verifyOpenCounselorCondition(OpenCounselor o, Player p) {
        int target = o.getMissionTarget();
        switch (o.getMissionType()) {
            case YURT:
                return p.getAmountYurts() >= target;
            case YURTREGION:
                EnumMap<Region, Integer> qty = Map.getMap(altKarakorum).getYurtsPerRegion(p);
                for (Region region : Region.values()) {
                    if (qty.get(region) < target) return false;
                }
                return true;
            case SWORD:
                return p.getTributeAmount(TributeType.SWORD) >= target;
            case EACHRESOURCE:
                for (TreasureType type : TreasureType.values()) {
                    if (p.getTreasureAmount(type) < 1) return false;
                }
                return true;
            case TREASURE:
                return p.getTotalTreasures() >= target;
            case CITIESDOMINATED:
                return p.getConqueredCities() >= target;
            case POWERUPS_LINE:
                return p.getBoard().completeRows() >= target;
            case POWERUPS_COL:
                for (ColumnColor color : ColumnColor.values()) {
                    if (p.getBoard().getColumn(color).getUpgradeCount() < target) return false;
                }
                return true;
            case POWERUPS_SQUARE:
                return p.getBoard().hasCompletedSquare();
            case POINTS:
                return p.getVotes() >= target;
            default:
                return false;
        }
    }

    public boolean setOpenCounselorDone(int ind, int playerInd) {
        if (openCounselorCards == null || ind < 0 || ind >= openCounselorCards.size()) {
            return false;
        }
        if (players == null || playerInd < 0 || playerInd >= players.length) {
            return false;
        }
        OpenCounselor card = openCounselorCards.get(ind);
        Player p = players[playerInd];
        if (!verifyOpenCounselorCondition(card, p)) {
            return false;
        }
        if (card.playerHasCompleted(p)) {
            p.addVotes(1);
            return true;
        }
        return false;
    }

    public boolean createArrayPlayers(int qtyPlayers) {
        if (qtyPlayers < 2 || qtyPlayers > 5) {
            return false;
        }
        players = new Player[qtyPlayers];
        return true;
    }

    public boolean assignPlayer(int ind, PlayerColor color, String heirName, int amountCoins) {
        if (players == null || ind < 0 || ind >= players.length) {
            return false;
        }
        Player p = new Player(color, Heir.fromName(heirName), getMap().getCentralNode(), amountCoins);
        players[ind] = p;
        return true;
    }

    public boolean setupMatch(List<String> heirNames, boolean alt) {
        altKarakorum = alt;
        Resource.resetResources();
        Map.resetMap(altKarakorum);
        players = null;

        if (!createArrayPlayers(heirNames.size())) {
            return false;
        }
        for (int i = 0; i < heirNames.size(); i++) {
            assignPlayer(i, PlayerColor.values()[i], heirNames.get(i), (i >= 2) ? 2 : 1);
        }
        for (int i = 0; i < 3 && i < getMap().getAmountCity(); i++) {
            getMap().getCity(i).activateSacking();
        }
        currentPlayer = 0;
        atualiza();
        return true;
    }

    public PlayerColor getPlayerColor(int ind) {
        if (players == null || ind < 0 || ind >= players.length || players[ind] == null) {
            return PlayerColor.BLUE;
        }
        return players[ind].getColor();
    }

    public String getPlayerHeirName(int ind) {
        if (players == null || ind < 0 || ind >= players.length || players[ind] == null) {
            return Heir.TOLUI.getDisplayName();
        }
        return players[ind].getHeir().getDisplayName();
    }

    public String getPlayerName(int ind) {
        if (players == null || ind < 0 || ind >= players.length || players[ind] == null) {
            return "Tolui";
        }
        return players[ind].getName();
    }

    public int getPlayerInd(String name) {
        if (players == null || name == null) {
            return -1;
        }
        for (int i = 0; i < players.length; i++) {
            if (players[i] != null && players[i].getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    public boolean MovePawn(int playerInd, String nodeId) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Node target = nodeById(nodeId);
        if (target == null) {
            return false;
        }
        newMoveAction(players[playerInd], target, 1);
        return doAction();
    }

    public boolean placeYurtOnNode(int playerInd, String nodeId) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Node n = nodeById(nodeId);
        if (n == null) {
            return false;
        }
        newYurtAction(playerInd, n);
        return doAction();
    }

    public boolean harvestProvinceResource(int playerInd, String nodeId, String provinceId) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Node n = nodeById(nodeId);
        Province province = provinceById(provinceId);
        if (n == null || province == null || !n.getProvinces().contains(province)) {
            return false;
        }
        newTributeAction(players[playerInd], n, province);
        return doAction();
    }

    public boolean attackCityForTreasure(int playerInd, String nodeOrCityName, int treasureInd, int qtySword) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        int cityInd = getMap().getCityInd(nodeOrCityName);
        if (cityInd < 0) {
            Node n = nodeById(nodeOrCityName);
            if (n instanceof SingleNode) {
                cityInd = getMap().getCityInd(((SingleNode) n).getCityName());
            }
        }
        if (cityInd < 0) {
            return false;
        }
        newSackAction(playerInd, players[playerInd].getCurrentNode(), cityInd, qtySword, treasureInd);
        return doAction();
    }

    public boolean moveKhan(int playerInd, String provinceId) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Province p = "UPGRADES".equalsIgnoreCase(provinceId) ? null : provinceById(provinceId);
        if (!"UPGRADES".equalsIgnoreCase(provinceId) && p == null) {
            return false;
        }
        Node currentNode = players[playerInd].getCurrentNode() != null
            ? players[playerInd].getCurrentNode()
            : getMap().getCentralNode();
        newKhanAction(playerInd, currentNode, p);
        return doAction();
    }

    public boolean executeTrade(int playerInd, String nodeId) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Node n = nodeById(nodeId);
        if (n == null || n instanceof SingleNode) {
            return false;
        }
        TradingPost post = (n instanceof CentralNode)
            ? ((CentralNode) n).getTradingPost()
            : ((DoubleNode) n).getTradingPost();
        newTradeAction(playerInd, post);
        return doAction();
    }

    public boolean startTurn(int playerInd, ColumnColor column, int pawnIndex) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        if (!players[playerInd].startTurn(column, pawnIndex)) {
            return false;
        }
        atualiza();
        return true;
    }

    public void endTurn(int playerInd) {
        if (players != null && playerInd >= 0 && playerInd < players.length && players[playerInd] != null) {
            players[playerInd].endTurn();
        }
    }

    public boolean isColumnFree(int playerInd, ColumnColor column) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return true;
        }
        return players[playerInd].canActivate(column);
    }

    public int getMovementsLeft(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 0;
        }
        return players[playerInd].getMovementsLeft();
    }
    
    public boolean usedAllActions(int playerInd) {
    	return (getMovementsLeft(playerInd) == 0) && !(players[playerInd].mustUseKhan()) && (players[playerInd].getHandsLeft() == 0);
    }

    // public void setMovementsLeft(int playerInd, int steps) {
    //     if (players != null && playerInd >= 0 && playerInd < players.length && players[playerInd] != null) {
    //         players[playerInd].setMovementsLeft(steps);
    //         atualiza();
    //     }

    private void newMoveAction(Player p, Node node, int qty) {
        action = new Action(p, node, new MovementAction(qty));
    }

    private void newYurtAction(int playerInd, Node node) {
        action = new Action(players[playerInd], node, new YurtAction());
    }

    private void newKhanAction(int playerInd, Node node, Province province) {
        action = new Action(players[playerInd], node, new KhanAction(province, getMap().getKhan()));
    }

    private void newSackAction(int playerInd, Node node, int cityInd, int qtySword, int indResource) {
        action = new Action(players[playerInd], node, new SackAction(getMap().getCity(cityInd), qtySword, indResource));
    }

    private void newTributeAction(Player p, Node node, Province province) {
        action = new Action(p, node, new TributeAction(province));
    }

    private void newTradeAction(int playerInd, TradingPost post) {
        Node refNode = players[playerInd].getCurrentNode() != null
            ? players[playerInd].getCurrentNode()
            : getMap().getCentralNode();
        action = new Action(players[playerInd], refNode, new TradeAction(post));
    }

    /**
     * Runs the prepared action. Only a successful action changes the match,
     * so only then the screens are notified (an invalid move changes nothing).
     */
    private boolean doAction() {
        if (action == null || action.doAction() != DebugReturn.SUCCESS) {
            return false;
        }
        atualiza();
        return true;
    }
}
