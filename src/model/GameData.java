package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;

/**
 * Singleton and Facade providing external access to the Model package.
 * Also acts as the observed subject in the Observer pattern, notifying
 * registered views whenever the match state changes.
 */
public class GameData implements ObservableIF {

    private static boolean altKarakorum = false;
    private Player[] players = null;
    private static ArrayList<OpenCounselor> openCounselorCards = null;
    private Action action = null;
    private int currentPlayer = 0;

    private final List<String> remainingCityDeck = new ArrayList<>();
    private final List<String> activeSackCities = new ArrayList<>();
    private final UpgradeMarket upgradeMarket = new UpgradeMarket();

    private final List<ObserverIF> observers = new ArrayList<>();
    private List<PrivateCounselor> offered = new ArrayList<>();

    private static GameData data = null;

    private GameData(boolean alt) {
        altKarakorum = alt;
        Map.resetMap(altKarakorum);
    }

    public static GameData createData(boolean alt) {
        if (data == null) {
            data = new GameData(alt);
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
    public void add(ObserverIF o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void remove(ObserverIF o) {
        observers.remove(o);
    }

    /**
     * Removes every observer. Used when a new match starts, so the screens of the
     * previous match (already closed) stop being notified.
     */
    public void removeAllObservers() {
        observers.clear();
    }

    @Override
    public int get(ObserverEnum ind) {
        switch (ind) {
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

    private void notifyObservers() {
        List<ObserverIF> snapshot = new ArrayList<>(observers);
        ListIterator<ObserverIF> li = snapshot.listIterator();
        while (li.hasNext()) {
            li.next().notify(this);
        }
    }

    public void notifyStateRestored() {
        notifyObservers();
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

    private int findCityIndexFlexible(String rawName) {
        if (rawName == null) {
            return -1;
        }
        int exact = getMap().getCityInd(rawName);
        if (exact >= 0) {
            return exact;
        }
        String asciiTarget = toAsciiName(rawName);
        for (int i = 0; i < getMap().getAmountCity(); i++) {
            City c = getMap().getCity(i);
            if (c != null && toAsciiName(c.getName()).equalsIgnoreCase(asciiTarget)) {
                return i;
            }
        }
        return -1;
    }

    private static String toAsciiName(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("\u00E3", "a")
            .replace("\u00E1", "a")
            .replace("\u00E2", "a")
            .replace("\u00E9", "e")
            .replace("\u00EA", "e")
            .replace("\u00ED", "i")
            .replace("\u00F3", "o")
            .replace("\u00F4", "o")
            .replace("\u00FA", "u")
            .replace("\u00E7", "c")
            .replace("\u00C3", "A")
            .replace("\u00C1", "A")
            .replace("\u00C9", "E")
            .replace("\u00CD", "I")
            .replace("\u00D3", "O")
            .replace("\u00DA", "U")
            .replace("\u00C7", "C");
    }

    /*
     * =============== Node & Yurt methods ===============
     */

    public List<String> getAllNodeIds() { return getMap().getAllNodeIds(); }
    public HashMap<String, int[]> getNodeCoordinates() { return getMap().getNodeCoordinates(); }

    public String getPlayerNodeId(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return "CENTRAL";
        }
        return getMap().getNodeId(players[playerInd].getCurrentNode());
    }

    /** How many pawns the player has (Altani has 2, the others have 1). */
    public int getPawnCount(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 1;
        }
        return players[playerInd].getPawnCount();
    }

    /** Node id of one specific pawn of the player. */
    public String getPawnNodeId(int playerInd, int pawnIndex) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null
                || pawnIndex < 0 || pawnIndex >= players[playerInd].getPawnCount()) {
            return "CENTRAL";
        }
        return getMap().getNodeId(players[playerInd].getPawn(pawnIndex).getCurrentNode());
    }

    /** Puts one specific pawn (not only the active one) on the given node. Used when loading a save. */
    public void setPawnNodeId(int playerInd, int pawnIndex, String nodeId) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null
                || pawnIndex < 0 || pawnIndex >= players[playerInd].getPawnCount()) {
            return;
        }
        Node target = nodeById(nodeId);
        if (target != null) {
            Pawn pawn = players[playerInd].getPawn(pawnIndex);
            if (pawn.getCurrentNode() != null) {
                pawn.getCurrentNode().leaveNode();
            }
            target.enterNode();
            pawn.moveTo(target);
            notifyObservers();
        }
    }

    public List<Integer> getNodeYurtOwnerIndices(String nodeId) {
        List<Integer> owners = new ArrayList<>();
        Node node = nodeById(nodeId);
        if (node == null || players == null) {
            return owners;
        }
        if (node instanceof CommonNode) {
            for (Player owner : ((CommonNode) node).getYurtOwners()) {
                for (int i = 0; i < players.length; i++) {
                    if (players[i] == owner) {
                        owners.add(i);
                        break;
                    }
                }
            }
        } else if (node instanceof CentralNode) {
            for (int i = 0; i < players.length; i++) {
                if (players[i] != null && node.hasYurtOf(players[i])) {
                    owners.add(i);
                    break;
                }
            }
        }
        return owners;
    }

    public void restoreNodeYurt(String nodeId, int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return;
        }
        Node node = nodeById(nodeId);
        if (node != null) {
            node.buildYurt(players[playerInd]);
        }
    }

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
        notifyObservers();
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
    
    private TradingPost tradingPostAt(String nodeId) {
    	Node n = nodeById(nodeId);
    	if (n instanceof CentralNode) return ((CentralNode) n).getTradingPost();
    	if (n instanceof DoubleNode) return ((DoubleNode) n).getTradingPost();
    	
    	return null;
    }
    
    public String getPostBonusCardName(String nodeId) {
    	TradingPost post = tradingPostAt(nodeId);
    	return post == null ? null : post.getBonusCardName();
    }
    
    public void restorePostBonusCard(String nodeId, String cardName) {
    	TradingPost post = tradingPostAt(nodeId);
    	if (post == null || cardName == null) return;
    	
    	try {
    		post.setCard(BonusCard.valueOf(cardName));
    	} catch (IllegalArgumentException ignores) { }
    }

    public boolean getPostBonusState(String nodeId) { return tradingPostAt(nodeId).isBonusAvailable(); }
    public void restorePostBonusAvailable(String nodeId, boolean state) { 
    	TradingPost post = tradingPostAt(nodeId);
    	if (post == null) return;
    	
    	post.setBonusAvailable(state); 
    }
    
    /*
     * ============= Province methods =============
     */
    
    public List<String> getAllProvinceIds() {
        List<String> ids = new ArrayList<>();
        for (ProvinceId id : ProvinceId.values()) {
            ids.add(id.name());
        }
        return ids;
    }

    public HashMap<String, int[]> getProvinceCoordinates() { return getMap().getProvinceCoordinates(); }

    public int getProvinceResourceCount(String provinceId) {
        Province p = provinceById(provinceId);
        return p != null ? p.resourceCount : 0;
    }

    public void setProvinceResourceCount(String provinceId, int qty) {
        Province p = provinceById(provinceId);
        if (p != null) {
            p.resourceCount = Math.max(0, Math.min(3, qty));
            notifyObservers();
        }
    }

    /*
     * =============== Khan methods ===============
     */

    public boolean isKhanAtUpgradeArea() { return getMap().getKhan().isAtUpgradesArea(); }

    public List<String> getKhanProvincesIds() {
        List<String> ret = new ArrayList<>();
        ret.add(ProvinceId.RUSSIA_YURT1.toString());
        ret.add(ProvinceId.RUSSIA_COIN1.toString());
        ret.add(ProvinceId.CHINA_YURT1.toString());
        ret.add(ProvinceId.PERSIA_COIN1.toString());
        return ret;
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

    public void restoreKhanState(boolean atUpgrades, String provinceId) {
        Province target = atUpgrades ? null : provinceById(provinceId);
        getMap().getKhan().restoreLocation(atUpgrades, target);
        notifyObservers();
    }

    public HashMap<String, int[]> getKhanCoordinates() { return getMap().getKhanCoordinates(); }

    /*
     * =============== City methods ===============
     */

    public boolean areAllCityConquered() { return getMap().allCityConquered(); }
    public HashMap<String, int[]> getCitiesCoordinates() { return getMap().getCitiesCoordinates(); }
    public HashMap<String, String> getCitiesImgPath() { return getMap().getCityImgPath(); }

    public List<String> getCityNames() {
        HashMap<String, int[]> map = this.getCitiesCoordinates();
        List<String> ret = new ArrayList<>();
        for (java.util.Map.Entry<String, int[]> entry : map.entrySet()) {
            ret.add(entry.getKey());
        }
        return ret;
    }

    public List<String> getCitiesToSack() { return Collections.unmodifiableList(this.activeSackCities); }

    public List<String> getRemainingCityDeck() {
        return Collections.unmodifiableList(this.remainingCityDeck);
    }

    public List<String> getCityTreasures(String cityName) {
        int cityInd = findCityIndexFlexible(cityName);
        if (cityInd < 0) {
            return Collections.emptyList();
        }
        City city = getMap().getCity(cityInd);
        if (city == null || !city.canBeSacked()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (TreasureType t : city.getResources()) {
            list.add(t.name());
        }
        return list;
    }

    public void restoreCitiesState(
        List<String> activeNames,
        List<String> deckNames,
        java.util.Map<String, List<String>> activeCityTreasures
    ) {
        this.activeSackCities.clear();
        this.remainingCityDeck.clear();

        if (activeNames != null) {
            for (String rawName : activeNames) {
                int ind = findCityIndexFlexible(rawName);
                if (ind >= 0) {
                    City city = getMap().getCity(ind);
                    if (city != null) {
                        city.activateSacking();
                        List<String> savedTreasures = activeCityTreasures != null
                            ? activeCityTreasures.get(toAsciiName(city.getName()))
                            : null;
                        if (savedTreasures != null) {
                            city.getResourcesInternal().clear();
                            for (String tName : savedTreasures) {
                                try {
                                    city.getResourcesInternal().add(TreasureType.valueOf(tName.trim().toUpperCase()));
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                        }
                        this.activeSackCities.add(city.getName());
                    }
                }
            }
        }

        if (deckNames != null) {
            for (String rawName : deckNames) {
                int ind = findCityIndexFlexible(rawName);
                if (ind >= 0) {
                    City city = getMap().getCity(ind);
                    if (city != null) {
                        this.remainingCityDeck.add(city.getName());
                    }
                }
            }
        }
    }

    private void revealNextCityIfAvailable() {
        while (this.activeSackCities.size() < 4 && !this.remainingCityDeck.isEmpty()) {
            String nextCityName = this.remainingCityDeck.remove(0);
            int ind = getMap().getCityInd(nextCityName);
            if (ind >= 0) {
                City city = getMap().getCity(ind);
                if (city != null && city.activateSacking()) {
                    this.activeSackCities.add(nextCityName);
                }
            }
        }
    }

    public boolean isAlternativeKarakorum() { return altKarakorum; }

    public PlayerColor getyurtColor(String nodeId) {
        Node node = nodeById(nodeId);
        if (node == null || players == null) {
            return null;
        }
        for (Player p : players) {
            if (p != null && node.hasYurtOf(p)) {
                return p.getColor();
            }
        }
        return null;
    }

    /*
     * ============================================
     * ============== Upgrade methods =============
     * ============================================
     */

    public List<String> getAvailableUpgradeCodes() {
        return this.upgradeMarket.getAvailableUpgradeCodes();
    }

    public int getUpgradeDeckSize() {
        return this.upgradeMarket.getDeckSize();
    }

    public List<String> getPlayerColumnUpgrades(int playerInd, ColumnColor column) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null || column == null) {
            return Collections.emptyList();
        }
        return players[playerInd].getBoard().getColumnUpgradeCodes(column);
    }

    public boolean hasFreeUpgradePending(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        return players[playerInd].hasFreeUpgradePending();
    }

    public int getFreeUpgradesPendingCount(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 0;
        }
        return players[playerInd].getBoard().getFreeUpgradesPendingCount();
    }

    public int getPlayerRewardedRows(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 0;
        }
        return players[playerInd].getBoard().getRewardedRows();
    }

    public boolean buyUpgradeFromMarket(int playerInd, int slotIndex) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Player p = players[playerInd];
        Upgrade candidate = this.upgradeMarket.peekAvailable(slotIndex);
        if (candidate == null) {
            return false;
        }
        if (!p.buyUpgrade(candidate.getColor(), candidate)) {
            return false;
        }
        this.upgradeMarket.takeAvailable(slotIndex);
        checkAutoOpenCounselors(playerInd);
        notifyObservers();
        return true;
    }

    public boolean claimFreeUpgradeFromMarket(int playerInd, int slotIndex) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Player p = players[playerInd];
        if (!p.hasFreeUpgradePending()) {
            return false;
        }
        Upgrade candidate = this.upgradeMarket.peekAvailable(slotIndex);
        if (candidate == null) {
            return false;
        }
        if (!p.receiveFreeUpgrade(candidate)) {
            return false;
        }
        this.upgradeMarket.takeAvailable(slotIndex);
        checkAutoOpenCounselors(playerInd);
        notifyObservers();
        return true;
    }

    public boolean claimFreeUpgradeFromDeck(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Player p = players[playerInd];
        if (!p.hasFreeUpgradePending()) {
            return false;
        }
        if (p.getBoard().getColumn(ColumnColor.WHITE).getUpgradeCount() >= Column.MAX_UPGRADES) {
            return false;
        }
        Upgrade drawn = this.upgradeMarket.drawFromDeck();
        if (drawn == null) {
            return false;
        }
        if (!p.receiveFreeUpgrade(drawn)) {
            return false;
        }
        checkAutoOpenCounselors(playerInd);
        notifyObservers();
        return true;
    }

    public void restoreAvailableUpgrades(List<String> codes) {
        this.upgradeMarket.restoreAvailableByCodes(codes);
    }

    public void restorePlayerColumnUpgrades(int playerInd, ColumnColor column, List<String> codes) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null || column == null) {
            return;
        }
        players[playerInd].getBoard().restoreColumnUpgrades(column, codes);
    }

    public void restorePlayerRewardedRows(int playerInd, int rewardedRows) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return;
        }
        players[playerInd].getBoard().setRewardedRows(rewardedRows);
    }

    /*
     * ============================================
     * ============== Player methods ==============
     * ============================================
     */

    public void setCurrentPlayer(int playerInd) {
        currentPlayer = playerInd;
        notifyObservers();
    }

    public int getPlayerCount() { return players == null ? 0 : players.length; }

    public int calculatePlayerTotalScore(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 0;
        }
        Player p = players[playerInd];
        evaluatePrivateCounselorProgress(p);
        int privatePts = p.getSecretCard() != null ? p.getSecretCard().gainPoints() : 0;
        return p.getVotes() + p.getConqueredCities() + privatePts;
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

    public int[] getPlayerTributeAmount(int playerInd, TributeType type) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null || type == null) {
            return null;
        }
        int[] ret = new int[2];
        ret[TributePosition.TRIBUTE.ordinal()] = players[playerInd].getTributeAmount(type);
        ret[TributePosition.VIRTUAL.ordinal()] = players[playerInd].getVirtualTributeAmount(type);
        return ret;
    }

    public int getPlayerTributeRaw(int playerInd, TributeType type) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null || type == null) {
            return 0;
        }
        return players[playerInd].getTributeAmount(type);
    }

    public void setPlayerTributeRaw(int playerInd, TributeType type, int qty) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null || type == null) {
            return;
        }
        players[playerInd].setTributeAmount(type, qty);
    }

    public int getPlayerTreasure(int playerInd, TreasureType type) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null || type == null) {
            return 0;
        }
        return players[playerInd].getEffectiveTreasureAmount(type);
    }

    public int getPlayerTreasureRaw(int playerInd, TreasureType type) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null || type == null) {
            return 0;
        }
        return players[playerInd].getTreasureAmount(type);
    }

    public void setPlayerTreasureRaw(int playerInd, TreasureType type, int qty) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null || type == null) {
            return;
        }
        players[playerInd].setTreasureAmount(type, qty);
    }

    public int getPlayerYurtsLeft(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 0;
        }
        return players[playerInd].getYurtsLeft();
    }

    public int getPlayerAmountYurts(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 0;
        }
        return players[playerInd].getAmountYurts();
    }

    public void setPlayerYurtsState(int playerInd, int left, int placed) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return;
        }
        players[playerInd].setYurtsState(left, placed);
    }

    public int getPlayerVotes(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 0;
        }
        return players[playerInd].getVotes();
    }

    public void setPlayerVotes(int playerInd, int votes) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return;
        }
        players[playerInd].setVotes(votes);
    }

    public int getPlayerConqueredCities(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return 0;
        }
        return players[playerInd].getConqueredCities();
    }

    public void setPlayerConqueredCities(int playerInd, int count) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return;
        }
        players[playerInd].setConqueredCities(count);
    }
    
    public List<String> getPlayerBonusCardNames(int playerInd) {
    	if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) return null;
    	
    	List<String> names = new ArrayList<>();
    	for (BonusCard card : players[playerInd].getBonuses()) names.add(card.getDisplayName());
    	return names;
    }
    
    public List<String> getPlayerUsedBonusCardNames(int playerInd) {
    	if (playerInd < 0 || playerInd >= players.length || players == null || players[playerInd] == null) return null;
    	
    	List<String> names = new ArrayList<String>();
    	for (BonusCard card : players[playerInd].getBonuses()) {
    		if (card.hasBeenUsed()) names.add(card.getDisplayName());
    	}
    	return names;
    }

    public void restorePlayerBonusCard(int playerInd, List<String> names, List<String> usedNames) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return;
        }
        
        ArrayList<BonusCard> cards = new ArrayList<>();
        for (String name : names) {
        	try {
        		BonusCard card = BonusCard.valueOf(name);
        		card.assignPlayer(players[playerInd]);
        		if (usedNames.contains(name)) card.useBonus();
        		cards.add(card);
        	} catch (IllegalArgumentException ignored) { }
        }
        players[playerInd].setBonusCard(cards);
    }

    public boolean startTurn(int playerInd, ColumnColor column, int pawnIndex) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        if (!players[playerInd].startTurn(column, pawnIndex)) {
            return false;
        }
        notifyObservers();
        return true;
    }

    public void endTurn(int playerInd) {
        if (players != null && playerInd >= 0 && playerInd < players.length && players[playerInd] != null) {
            checkAutoOpenCounselors(playerInd);
            players[playerInd].endTurn();
            offered.clear();
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
            notifyObservers();
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
            notifyObservers();
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
            notifyObservers();
        }
    }

    public void setColumnOccupied(int playerInd, ColumnColor column, boolean occupied) {
        if (players != null && playerInd >= 0 && playerInd < players.length && players[playerInd] != null) {
            players[playerInd].setColumnOccupied(column, occupied);
        }
    }

    public boolean needsPrivateCounselorChoice(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        return players[playerInd].getSecretCard() == null;
    }

    public boolean choosePrivateCounselor(int playerInd, String name) {
        if (!needsPrivateCounselorChoice(playerInd) || name == null) {
            return false;
        }
        Player p = players[playerInd];
        for (PrivateCounselor card : offered) {
            if (name.equalsIgnoreCase(card.getName())) {
                p.setSecretCard(card);
                offered.clear();
                evaluatePrivateCounselorProgress(p);
                notifyObservers();
                return true;
            }
        }
        return false;
    }

    /**
     * Restores a player's Secret Counselor card directly when loading a saved match.
     */
    public void restorePrivateCounselor(int playerInd, String cardName) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return;
        }
        offered.clear();
        if (cardName == null || cardName.trim().isEmpty() || "NONE".equalsIgnoreCase(cardName.trim())) {
            players[playerInd].setSecretCard(null);
            return;
        }
        PrivateCounselor card = PrivateCounselor.takeCardByName(cardName);
        if (card != null) {
            players[playerInd].setSecretCard(card);
            evaluatePrivateCounselorProgress(players[playerInd]);
        }
    }

    /*
     * ============================================
     * ========== Open Counselor methods ==========
     * ============================================
     */

    public static boolean createOpenCounselors() {
        if (openCounselorCards == null) {
            openCounselorCards = OpenCounselor.giveCardsInGame();
        }
        return true;
    }

    public List<String> getGameOpenCounselorIds() {
        createOpenCounselors();
        return OpenCounselor.getOpenCounselorsIdGame();
    }

    public String getOpenCounselorOwnerName(int cardInd) {
        createOpenCounselors();
        if (openCounselorCards == null || cardInd < 0 || cardInd >= openCounselorCards.size()) {
            return null;
        }
        Player owner = openCounselorCards.get(cardInd).getCompletedBy();
        return owner != null ? owner.getName() : null;
    }

    private void checkAutoOpenCounselors(int playerInd) {
        createOpenCounselors();
        if (openCounselorCards == null || players == null || playerInd < 0 || playerInd >= players.length) {
            return;
        }
        Player p = players[playerInd];
        if (p == null) {
            return;
        }
        for (int i = 0; i < openCounselorCards.size(); i++) {
            OpenCounselor card = openCounselorCards.get(i);
            if (!card.hasBeenCompleted() && verifyOpenCounselorCondition(card, p)) {
                if (card.playerHasCompleted(p)) {
                    p.addVotes(1);
                }
            }
        }
        evaluatePrivateCounselorProgress(p);
    }

    private static boolean verifyOpenCounselorCondition(OpenCounselor o, Player p) {
        int target = o.getMissionTarget();
        switch (o.getMissionType()) {
            case YURT:
                return (Player.INITIAL_YURTS - p.getYurtsLeft()) >= target;
            case YURTREGION:
                EnumMap<Region, Integer> qty = Map.getMap(altKarakorum).getYurtsPerRegion(p);
                for (Region region : Region.values()) {
                    if (qty.get(region) < target) return false;
                }
                return true;
            case SWORD:
                return (p.getTributeAmount(TributeType.SWORD) + p.getVirtualTributeAmount(TributeType.SWORD)) >= target;
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

    /*
     * =============================================
     * ========= Private Counselor methods =========
     * =============================================
     */

    public List<String> getPrivateCounselorCandidates() {
        if (offered.isEmpty()) {
            offered = PrivateCounselor.giveTwoCards();
        }
        List<String> names = new ArrayList<>();
        for (PrivateCounselor p : offered) {
            names.add(p.getName());
        }
        return names;
    }

    public String getPlayerPrivateCounselorName(int playerInd) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return "None";
        }
        PrivateCounselor card = players[playerInd].getSecretCard();
        return card != null ? card.getName() : "None";
    }

    private int computePrivateMissionMetric(Player p, PrivateMissionType type) {
        if (p == null || type == null) {
            return 0;
        }
        List<Upgrade> upgrades = p.getBoard().getAllUpgrades();
        switch (type) {
            case POWERUPS_QTD:
                return p.getBoard().getTotalUpgradeCount();
            case POWERUPS_SAME: {
                EnumMap<Icon, Integer> counts = new EnumMap<>(Icon.class);
                int maxSame = 0;
                for (Upgrade u : upgrades) {
                    int next = counts.getOrDefault(u.getIcon(), 0) + 1;
                    counts.put(u.getIcon(), next);
                    maxSame = Math.max(maxSame, next);
                }
                return maxSame;
            }
            case POWERUPS_DIFF: {
                Set<String> distinct = new HashSet<>();
                for (Upgrade u : upgrades) {
                    distinct.add(u.getIcon().name() + "_" + u.getColor().name());
                }
                return distinct.size();
            }
            case POWERUPS_HORSE_HAND: {
                int count = 0;
                for (Upgrade u : upgrades) {
                    if (u.getIcon() == Icon.HORSE || u.getIcon() == Icon.HAND) {
                        count++;
                    }
                }
                return count;
            }
            case POWERUPS_YURT: {
                int count = 0;
                for (Upgrade u : upgrades) {
                    if (u.getIcon() == Icon.VIRTUAL_YURT) {
                        count++;
                    }
                }
                return count;
            }
            case CITY_YURT: {
                Set<String> citiesWithYurt = new HashSet<>();
                for (String nodeId : getAllNodeIds()) {
                    Node n = nodeById(nodeId);
                    if (n instanceof SingleNode && n.hasYurtOf(p)) {
                        citiesWithYurt.add(((SingleNode) n).getCityName());
                    }
                }
                return citiesWithYurt.size();
            }
            case CITY_2YURT: {
                HashMap<String, Integer> cityYurtCount = new HashMap<>();
                for (String nodeId : getAllNodeIds()) {
                    Node n = nodeById(nodeId);
                    if (n instanceof SingleNode && n.hasYurtOf(p)) {
                        String cName = ((SingleNode) n).getCityName();
                        cityYurtCount.put(cName, cityYurtCount.getOrDefault(cName, 0) + 1);
                    }
                }
                int citiesWithAtLeastTwo = 0;
                for (int c : cityYurtCount.values()) {
                    if (c >= 2) {
                        citiesWithAtLeastTwo++;
                    }
                }
                return citiesWithAtLeastTwo;
            }
            case YURT_TRADINGPOST: {
                int postYurts = 0;
                for (String nodeId : getAllNodeIds()) {
                    Node n = nodeById(nodeId);
                    if ((n instanceof DoubleNode || n instanceof CentralNode) && n.hasYurtOf(p)) {
                        postYurts++;
                    }
                }
                return postYurts;
            }
            case YURT_KHAN: {
                Set<Province> khanProvinces = new HashSet<>();
                for (String provId : getKhanProvincesIds()) {
                    Province kp = provinceById(provId);
                    if (kp != null) {
                        khanProvinces.add(kp);
                    }
                }
                Set<Province> coveredKhanProvs = new HashSet<>();
                for (String nodeId : getAllNodeIds()) {
                    Node n = nodeById(nodeId);
                    if (n != null && n.hasYurtOf(p)) {
                        for (Province np : n.getProvinces()) {
                            if (khanProvinces.contains(np)) {
                                coveredKhanProvs.add(np);
                            }
                        }
                    }
                }
                return coveredKhanProvs.size();
            }
            case YURTINSEQUENCE: {
                Set<Node> ownedNodes = new HashSet<>();
                for (String nodeId : getAllNodeIds()) {
                    Node n = nodeById(nodeId);
                    if (n != null && n.hasYurtOf(p)) {
                        ownedNodes.add(n);
                    }
                }
                int maxChain = 0;
                Set<Node> visited = new HashSet<>();
                for (Node start : ownedNodes) {
                    if (visited.contains(start)) {
                        continue;
                    }
                    int compSize = 0;
                    Queue<Node> q = new ArrayDeque<>();
                    q.add(start);
                    visited.add(start);
                    while (!q.isEmpty()) {
                        Node cur = q.poll();
                        compSize++;
                        for (Node nb : cur.getNeighbors()) {
                            if (ownedNodes.contains(nb) && !visited.contains(nb)) {
                                visited.add(nb);
                                q.add(nb);
                            }
                        }
                    }
                    maxChain = Math.max(maxChain, compSize);
                }
                return maxChain;
            }
            default:
                return 0;
        }
    }

    private void evaluatePrivateCounselorProgress(Player p) {
        if (p == null || p.getSecretCard() == null) {
            return;
        }
        PrivateCounselor secret = p.getSecretCard();
        int progress = computePrivateMissionMetric(p, secret.getMissionType());
        secret.updateProgress(progress);
    }

    /*
     * ============================================
     * ============ Game Start methods ============
     * ============================================
     */

    public boolean setupMatch(List<String> heirNames, boolean alt) {
        altKarakorum = alt;
        Resource.resetResources();
        OpenCounselor.resetDeck();
        PrivateCounselor.resetDeck();
        openCounselorCards = OpenCounselor.giveCardsInGame();
        Map.resetMap(altKarakorum);
        this.upgradeMarket.resetMarket();
        this.offered.clear();
        players = null;

        if (!createArrayPlayers(heirNames.size())) {
            return false;
        }
        for (int i = 0; i < heirNames.size(); i++) {
            assignPlayer(i, PlayerColor.values()[i], heirNames.get(i), (i >= 2) ? 2 : 1);
        }

        this.remainingCityDeck.clear();
        this.activeSackCities.clear();
        this.remainingCityDeck.addAll(getCityNames());
        Collections.shuffle(this.remainingCityDeck);
        revealNextCityIfAvailable();

        currentPlayer = 0;
        notifyObservers();
        return true;
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

    /*
     * ===========================================
     * ============= Actions methods =============
     * ===========================================
     */

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
        boolean success = doAction();
        if (success) {
            checkAutoOpenCounselors(playerInd);
            notifyObservers();
        }
        return success;
    }

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
        boolean success = doAction();
        if (success) {
            checkAutoOpenCounselors(playerInd);
            notifyObservers();
        }
        return success;
    }

    public boolean attackCityForTreasure(int playerInd, String nodeOrCityName, int treasureInd, int qtySword) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        Player p = players[playerInd];
        int cityInd = findCityIndexFlexible(nodeOrCityName);
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

        City targetCity = getMap().getCity(cityInd);
        newSackAction(playerInd, actionNode, cityInd, qtySword, treasureInd);
        boolean success = doAction();
        if (success) {
            if (targetCity != null && !targetCity.canBeSacked()) {
                this.activeSackCities.remove(targetCity.getName());
                revealNextCityIfAvailable();
            }
            checkAutoOpenCounselors(playerInd);
            notifyObservers();
        }
        return success;
    }

    public boolean moveKhan(int playerInd, String provinceId) {
        if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) {
            return false;
        }
        boolean toUpgrades = "UPGRADES".equalsIgnoreCase(provinceId);
        Province p = toUpgrades ? null : provinceById(provinceId);
        if (!toUpgrades && p == null) {
            return false;
        }
        Node currentNode = players[playerInd].getCurrentNode() != null
            ? players[playerInd].getCurrentNode()
            : getMap().getCentralNode();
        newKhanAction(playerInd, currentNode, p);
        boolean success = doAction();
        if (success && toUpgrades) {
            int emptySlots = this.upgradeMarket.refreshMarket();
            if (emptySlots >= 2) {
                players[playerInd].addTribute(TributeType.COIN, 1);
            }
            if (emptySlots >= 4) {
                players[playerInd].addTribute(TributeType.SWORD, 1);
            }
            checkAutoOpenCounselors(playerInd);
            notifyObservers();
        }
        return success;
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
        boolean success = doAction();
        if (success) {
            checkAutoOpenCounselors(playerInd);
            notifyObservers();
        }
        return success;
    }

    private void newMoveAction(Player p, Node node, int qty) { action = new Action(p, node, new MovementAction(qty)); }
    private void newYurtAction(int playerInd, Node node) { action = new Action(players[playerInd], node, new YurtAction()); }
    private void newKhanAction(int playerInd, Node node, Province province) { action = new Action(players[playerInd], node, new KhanAction(province, getMap().getKhan())); }
    private void newSackAction(int playerInd, Node node, int cityInd, int qtySword, int indResource) { action = new Action(players[playerInd], node, new SackAction(getMap().getCity(cityInd), qtySword, indResource)); }
    private void newTributeAction(Player p, Node node, Province province) { action = new Action(p, node, new TributeAction(province)); }

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
        notifyObservers();
        return true;
    }
    
    /*
     * ===========================================
     * =============== Bonus Cards ===============
     * ===========================================
     */
    
    public HashMap<String, String> getBonusCardImgPath() {
    	HashMap<String, String> tmp = BonusCard.getImagePath();
    	return tmp;
    }
    
    public boolean useBonusCard(String name, int playerInd) {
    	if (players == null || playerInd < 0 || playerInd >= players.length || players[playerInd] == null) return false;
    	
    	BonusCard card = null;
    	for (BonusCard c : players[playerInd].getBonuses()) {
    		if (c.name().equalsIgnoreCase(name)) { card = c; break; }
    	}
    	if (card == null || card.hasBeenUsed()) return false;
    	
    	switch (card) {
    	case VOTE: 	players[playerInd].addVotes(1); break;
    	case SWORDS: players[playerInd].addTribute(TributeType.SWORD, 2); break;
    	case COINS:	players[playerInd].addTribute(TributeType.COIN, 2); break;
    	case YURTS:	players[playerInd].addTribute(TributeType.YURT, 2); break;
    	case MOVEMENT:
    		players[playerInd].setMovementsLeft(players[playerInd].getMovementsLeft() + 3);
    		players[playerInd].setHandsLeft(players[playerInd].getHandsLeft() + 1);
    		break;
    	case HANDS:
    		players[playerInd].setHandsLeft(players[playerInd].getHandsLeft() + 2);
    		break;
    	default: // 
    	}
    	
    	card.useBonus();
    	notifyObservers();    	
    	return true;
    }
    
    public String getBonusCardOnPost(String nodeId) {
    	Node n = this.nodeById(nodeId);
    	if (!(n instanceof CentralNode || n instanceof DoubleNode)) return null;
    	
    	TradingPost p = (n instanceof CentralNode) ? ((CentralNode) n).getTradingPost() : ((DoubleNode) n).getTradingPost();
    	
    	return (p.isBonusAvailable()) ? null : p.getBonusCardName();
    }
    
    public HashMap<String, int[]> getBonusCardCoordinates() { return getMap().getBonusCardCoordinates(); }
    
    
    public List<String> getPlayerUnusedBonusCardNames(int playerInd) {
    	if (playerInd < 0 || playerInd >= players.length || players == null || players[playerInd] == null) return null;
    	
    	List<String> names = new ArrayList<>();
    	for (BonusCard card : players[playerInd].getBonuses()) {
    		if (!card.hasBeenUsed()) names.add(card.getDisplayName());
    	}
    	return names;
    }    
}
