package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Singleton and Facade providing external access to the Model package.
 * Also acts as the observed subject in the Observer pattern, notifying
 * registered views whenever the match state changes.
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

    private final List<ObservadorIF> observers = new ArrayList<>();

    private static GameData data = null;

    private GameData(boolean alt) {
        altKarakorum = alt;
        Map.resetMap(altKarakorum);
    }

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
            data = new GameData(false);
        }
        return data;
    }

    @Override
    public void add(ObservadorIF o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
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

    private void atualiza() {
        List<ObservadorIF> snapshot = new ArrayList<>(observers);
        ListIterator<ObservadorIF> li = snapshot.listIterator();
        while (li.hasNext()) {
            li.next().notify(this);
        }
    }

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
            Node current = players[playerInd].getCurrentNode();
            if (current != null) {
                current.leaveNode();
            }
            target.enterNode();
            players[playerInd].moveTo(target);
            atualiza();
        }
    }

    /**
     * Returns the ordered list of node IDs visited by the specified player during the current turn.
     */
    public List<String> getTurnPathNodeIds(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return Collections.emptyList();
        }
        List<String> pathIds = new ArrayList<>();
        for (Node n : players[playerInd].getTurnPath()) {
            String id = getMap().getNodeId(n);
            if (id != null) {
                pathIds.add(id);
            }
        }
        return pathIds;
    }

    /**
     * Restores the turn path of the specified player from a list of node IDs.
     */
    public void setTurnPathNodeIds(int playerInd, List<String> nodeIds) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        if (nodeIds != null) {
            for (String id : nodeIds) {
                Node n = nodeById(id);
                if (n != null) {
                    nodes.add(n);
                }
            }
        }
        players[playerInd].setTurnPath(nodes);
        atualiza();
    }

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
        List<String> ret = new ArrayList<>();
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

    /**
     * Restores the Khan piece state from a saved match without triggering cascading tributes.
     */
    public void restoreKhanState(boolean atUpgrades, String provinceId) {
        Province target = atUpgrades ? null : provinceById(provinceId);
        getMap().getKhan().restoreLocation(atUpgrades, target);
        atualiza();
    }

    public HashMap<String, int[]> getNodeCoordinates() { return getMap().getNodeCoordinates(); }
    public HashMap<String, int[]> getProvinceCoordinates() { return getMap().getProvinceCoordinates(); }
    public HashMap<String, int[]> getKhanCoordinates() { return getMap().getKhanCoordinates(); }
    public HashMap<String, int[]> getCitiesCoordinates() { return getMap().getCitiesCoordinates(); }

    public List<String> getCityNames() {
        HashMap<String, int[]> map = this.getCitiesCoordinates();
        List<String> ret = new ArrayList<>();
        for (java.util.Map.Entry<String, int[]> entry : map.entrySet()) {
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
            atualiza();
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
        Player p = players[playerInd];
        Node n = nodeById(nodeId);
        if (n == null || !p.hasVisited(n)) {
            return false;
        }
        newYurtAction(playerInd, n);
        return doAction();
    }

    /**
     * Harvests a tribute from the given province if the specified node OR any node
     * visited in the player's current turn path borders that province.
     */
    public boolean harvestProvinceResource(int playerInd, String nodeId, String provinceId) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Player p = players[playerInd];
        Province province = provinceById(provinceId);
        if (province == null) {
            return false;
        }

        Node validNode = null;
        Node candidate = nodeById(nodeId);
        if (candidate != null && p.hasVisited(candidate) && candidate.getProvinces().contains(province)) {
            validNode = candidate;
        } else {
            for (Node visited : p.getTurnPath()) {
                if (visited != null && visited.getProvinces().contains(province)) {
                    validNode = visited;
                    break;
                }
            }
        }

        if (validNode == null) {
            return false;
        }
        newTributeAction(p, validNode, province);
        return doAction();
    }

    public boolean attackCityForTreasure(int playerInd, String nodeOrCityName, int treasureInd, int qtySword) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Player p = players[playerInd];
        int cityInd = getMap().getCityInd(nodeOrCityName);
        Node actionNode = p.getCurrentNode();

        if (cityInd < 0) {
            Node clicked = nodeById(nodeOrCityName);
            if (clicked instanceof SingleNode && p.hasVisited(clicked)) {
                cityInd = getMap().getCityInd(((SingleNode) clicked).getCityName());
                actionNode = clicked;
            }
        }

        if (cityInd < 0) {
            for (Node visited : p.getTurnPath()) {
                if (visited instanceof SingleNode) {
                    int candidateInd = getMap().getCityInd(((SingleNode) visited).getCityName());
                    if (candidateInd >= 0 && getMap().getCity(candidateInd).canBeSacked()) {
                        cityInd = candidateInd;
                        actionNode = visited;
                        break;
                    }
                }
            }
        }

        if (cityInd < 0) {
            return false;
        }
        newSackAction(playerInd, actionNode, cityInd, qtySword, treasureInd);
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
        Player p = players[playerInd];
        Node n = nodeById(nodeId);
        if (n == null || n instanceof SingleNode || !p.hasVisited(n)) {
            n = null;
            for (Node visited : p.getTurnPath()) {
                if (visited instanceof CentralNode || visited instanceof DoubleNode) {
                    n = visited;
                    break;
                }
            }
        }
        if (n == null) {
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

    public void setMovementsLeft(int playerInd, int steps) {
        if (players != null && playerInd >= 0 && playerInd < players.length && players[playerInd] != null) {
            players[playerInd].setMovementsLeft(steps);
            atualiza();
        }
    }

    public int getHandsLeft(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 0;
        }
        return players[playerInd].getHandsLeft();
    }

    public void setHandsLeft(int playerInd, int hands) {
        if (players != null && playerInd >= 0 && playerInd < players.length && players[playerInd] != null) {
            players[playerInd].setHandsLeft(hands);
            atualiza();
        }
    }

    public boolean isKhanPending(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        return players[playerInd].mustUseKhan();
    }

    public void setKhanPending(int playerInd, boolean pending) {
        if (players != null && playerInd >= 0 && playerInd < players.length && players[playerInd] != null) {
            players[playerInd].setKhanPending(pending);
            atualiza();
        }
    }

    public boolean usedAllActions(int playerInd) {
        return (getMovementsLeft(playerInd) == 0)
            && !(players[playerInd].mustUseKhan())
            && (players[playerInd].getHandsLeft() == 0);
    }

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

    private boolean doAction() {
        if (action == null || action.doAction() != DebugReturn.SUCCESS) {
            return false;
        }
        atualiza();
        return true;
    }
}
