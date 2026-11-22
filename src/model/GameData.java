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
	
	/*
	 * ============================================
	 * =============== Attributes =================
	 * ============================================
	 */

    private static boolean altKarakorum = false; // To know if it's the alternate version for central node
    
    // Players attributes
    private Player[] players = null;
    private int currentPlayer = 0;
    
    // Counselors attributes
    private static ArrayList<OpenCounselor> openCounselorCards = null;
    private List<PrivateCounselor> offered = new ArrayList<>();
    
    // Action interface
    private Action action = null;

    // Cities attributes
    private final List<String> activeSackCities = new ArrayList<>();
    private final List<String> remainingCityDeck = new ArrayList<>();

    // Upgrades
    private final UpgradeMarket upgradeMarket = new UpgradeMarket();

    private final List<ObserverIF> observers = new ArrayList<>();

    private static GameData data = null;

    /*
     * ============================================
     * ============= Constructor ==================
     * ============================================
     */
    
    private GameData(boolean alt) {
        altKarakorum = alt;
        Map.resetMap(altKarakorum);
    }
    
    /*
     * ============================================
     * ============== Singleton ===================
     * ============================================
     */

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
    
    /*
     * ============================================
     * ================= Observer =================
     * ============================================
     */

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
    
    /*
     * ============================================
     * ============= Helper Methods ===============
     * ============================================
     */

    private Map getMap() { return Map.getMap(altKarakorum); }

    private Node nodeById(String id) { return getMap().getNodeById(id); }

    private Province provinceById(String id) { 
        if (id == null) return null;
        
        try {
            return getMap().getProvince(ProvinceId.valueOf(id).ordinal());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int findCityIndexFlexible(String rawName) {
        if (rawName == null) return -1;
        
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
        if (text == null) return "";
        
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
    
    private TradingPost tradingPostAt(String nodeId) {
    	Node n = nodeById(nodeId);
    	if (n instanceof CentralNode) return ((CentralNode) n).getTradingPost();
    	if (n instanceof DoubleNode) return ((DoubleNode) n).getTradingPost();
    	
    	return null;
    }
    
    private boolean playerIndIsInValid(int playerInd) { 
    	return (playerInd < 0 || players == null || playerInd >= players.length || players[playerInd] == null);
    }
    
    /*
     * ============================================
     * ============== Node & Yurt =================
     * ============================================
     */

    /**
     * Returns a list containing all the ids ({@code String}) for the nodes on the map
     * @return {@code List<String>} with all nodes id from the map
     */
    public List<String> getAllNodeIds() { return getMap().getAllNodeIds(); }
    
    /**
     * Get all the coordinates for each node so it shows without any error on the view
     * @return {@code HashMap<String, int[]} mapping each nodeId to its coordinate to show on the view. 
     * The first position is the x coordinate, the second is the y.
     */
    public HashMap<String, int[]> getNodeCoordinates() { return getMap().getNodeCoordinates(); }
    
    /**
     * Gets all the indices of the players who have a yurt on that node
     * @param nodeId {@code String} for the id of a node
     * @return {@code List<Integer>} containing all indices ({@code Integer}) for the players that have a yurt on that node
     */
    public List<Integer> getNodeYurtOwnerIndices(String nodeId) {
        List<Integer> owners = new ArrayList<>();
        
        Node node = nodeById(nodeId);
        if (node == null || players == null) return owners;
        
        for (int i = 0; i < players.length; i++) {
        	if (players[i] != null && node.hasYurtOf(players[i])) owners.add(i);
        }
        
        return owners;
    }
    
    /**
     * Get all the nodes id that the player has passed in this turn
     * @param playerInd {@code int} of the player index that want to get the path
     * @return {@code List<String>} containing all nodes id that the player has passed through
     */
    public List<String> getTurnPathNodeIds(int playerInd) {
        if (playerIndIsInValid(playerInd)) return Collections.emptyList();
        
        List<String> pathIds = new ArrayList<>();
        for (Node n : players[playerInd].getTurnPath()) {
            String id = getMap().getNodeId(n);
            if (id != null) pathIds.add(id);
        }
        return pathIds;
    }
    
    /**
     * Get all nodes that a player can reach, considering his movement and if the node if full
     * @param playerInd {@code int} of the player index that want to get the path
     * @return {@code List<String>} containing all the nodes id that the player can reach/move to
     */
    public List<String> getReachableNodeIds(int playerInd) {
        if (playerIndIsInValid(playerInd)) return Collections.emptyList();
        
        List<String> reachable = new ArrayList<>();
        for (String nodeId : getAllNodeIds()) {
            Node target = nodeById(nodeId);
            if (target != null && players[playerInd].findPath(target) != null) reachable.add(nodeId);
        }
        return reachable;
    }
    
    /**
     * Verify if the central node (Karakorum) is in the alternate state
     * @return {@code true} if Karakorum is using the alternative state, {@code false} otherwise
     */
    public boolean isAlternativeKarakorum() { return altKarakorum; }

    /**
     * Get the yurt color from the node
     * @param nodeId {@code String} of the node id that have the yurt
     * @return {@code PlayerColor} of the yurt in that node (if it has one), {@code null} otherwise
     */
    public PlayerColor getyurtColor(String nodeId) {
        Node node = nodeById(nodeId);
        if (node == null || players == null) return null;
        
        for (Player p : players) {
            if (p != null && node.hasYurtOf(p)) return p.getColor();
        }
        return null;
    }
    
    /**
     * Get the node id of a specific pawn from a player
     * @param playerInd {@code int} index of the player that you want to check
     * @param pawnIndex {@code int} index of the pawn to get the node id
     * @return {@code String} of the nodeId from that pawn
     */
    public String getPawnNodeId(int playerInd, int pawnIndex) {
        if (playerIndIsInValid(playerInd) || pawnIndex < 0 || pawnIndex >= players[playerInd].getPawnCount()) return "CENTRAL";
        
        return getMap().getNodeId(players[playerInd].getPawn(pawnIndex).getCurrentNode());
    }
    
    /*
     * ============================================
     * ============== Province ====================
     * ============================================
     */
    
    /**
     * Get all the provinces ids available on the map
     * @return {@code List<String>} containing all the provinces ids from the map
     */
    public List<String> getAllProvinceIds() { return Province.getAllProvinceIds(); }

    /**
     * Get all the coordinates of the provinces to show in the view
     * @return {@code HashMap<String, int[]>} mapping each province id to a coordinate.
     * The first element in the array is the x coordinate and the second is the y coordinate
     */
    public HashMap<String, int[]> getProvinceCoordinates() { return getMap().getProvinceCoordinates(); }

    /**
     * Get the amount of resources available on that province
     * @param provinceId {@code String} of the identifier for the string
     * @return the amount of resource if the id is a valid province, 0 otherwise
     */
    public int getProvinceResourceCount(String provinceId) {
        Province p = provinceById(provinceId);
        return p != null ? p.resourceCount : 0;
    }
    
    /*
     * ============================================
     * ================== Khan ====================
     * ============================================
     */
    
    /**
     * Verify if Khan is at the upgrade area
     * @return {@code true} if Khan is at the upgrade area, {@code false} otherwise
     */
    public boolean isKhanAtUpgradeArea() { return getMap().getKhan().isAtUpgradesArea(); }

    /**
     * Get all the the province ids that can have a Khan on it
     * @return {@code List<String>} containing all the province ids that may have a Khan on it
     */
    public List<String> getKhanProvincesIds() { return Province.getKhanProvinceIds(); }

    /**
     * Get the province id of where Khan is at the moment
     * @return {@code String} with the province id of where Khan is at the moment
     */
    public String getKhanProvinceId() {
        Province current = getMap().getKhan().currentProvince;
        if (current == null) return null; // No one has moved him yet
        
        for (ProvinceId id : ProvinceId.values()) {
            if (getMap().getProvince(id.ordinal()) == current) {
                return id.name();
            }
        }
        return null;
    }
    
    /**
     * Get all the coordinates that Khan can show up
     * @return {@code HashMap<String, int[]>} mapping each province id to the khan display position.
     * The first element of the array is the x coordinates, the second is the y
     */
    public HashMap<String, int[]> getKhanCoordinates() { return getMap().getKhanCoordinates(); }
    
    /*
     * ============================================
     * ================= City =====================
     * ============================================
     */

    /**
     * Verify if all the cities have been conquered (end game condition)
     * @return {@code true} if all cities have been conquered, {@code false} otherwise
     */
    public boolean areAllCityConquered() { return getMap().allCityConquered(); }
    
    /**
     * Get all the coordinates of the cities to show them on the display
     * @return {@code HashMap<String, int[]>} mapping each city name to its coordinate.
     * The first element of the array if the x coordinate, the second is the y
     */
    public HashMap<String, int[]> getCitiesCoordinates() { return getMap().getCitiesCoordinates(); }
    
    /**
     * Get all the city images path
     * @return {@code HashMap<String, String>} mapping each city name to its image path
     */
    public HashMap<String, String> getCitiesImgPath() { return City.getCityImgPath(); }

    /**
     * Get all the cities names from the game
     * @return {@code List<String>} containing each city name
     */
    public List<String> getCityNames() { return City.getCitiesNames(); }

    /**
     * Get all the cities that are available to be sacked
     * @return {@code List<String>} containing all the names of the cities that can be sacked
     */
    public List<String> getCitiesToSack() { return Collections.unmodifiableList(this.activeSackCities); }

    /**
     * Get all the cities that have not yet been activated to be sacked
     * @return {@code List<String>} containing all the names of the cities that have not yet been activated for sacking
     */
    public List<String> getRemainingCityDeck() { return Collections.unmodifiableList(this.remainingCityDeck); }

    /**
     * Get all of the city's treasure that can be sacked
     * @param cityName {@code String} name of the city to check its treasures
     * @return {@code List<String>} containing all the names of the treasures left to be sacked
     */
    public List<String> getCityTreasures(String cityName) {
        int cityInd = findCityIndexFlexible(cityName);
        if (cityInd < 0) return Collections.emptyList();
        
        City city = getMap().getCity(cityInd);
        if (city == null || !city.canBeSacked()) return Collections.emptyList();
        
        List<String> list = new ArrayList<>();
        for (TreasureType t : city.getResources()) list.add(t.name());
        
        return list;
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
    
    /** 
     * Get the index of the player that has conquered that city
     * @param cityName {@code String} of the name of the city to verify 
     * @return index of the player that conquered the city, or -1 if nobody did 
     */
    public int getCityConquerorIndex(String cityName) {
        int cityInd = findCityIndexFlexible(cityName);
        if (cityInd < 0 || players == null) return -1;
        
        Player owner = getMap().getCity(cityInd).dominatedBy();
        for (int i = 0; i < players.length; i++) {
            if (players[i] == owner && owner != null) return i;
        }
        return -1;
    }
    
    /*
     * ============================================
     * ============== TradingPosts ================
     * ============================================
     */
    
    /**
     * Get the name from the bonus card of a trading post
     * @param nodeId {@code String} id of a DoubleNode or CentralNode
     * @return {@code String} name of the bonus card, if there is one still available
     */
    public String getPostBonusCardName(String nodeId) {
    	TradingPost post = tradingPostAt(nodeId);
    	return post == null ? null : post.getBonusCardName();
    }
    
    /*
     * ============================================
     * ================= Upgrade ==================
     * ============================================
     */

    /**
     * Get all the upgrades that are available to buy right now
     * @return {@code List<String>} containing all the names of the upgrades available
     */
    public List<String> getAvailableUpgradeCodes() { return this.upgradeMarket.getAvailableUpgradeCodes(); }

    /**
     * Get how many upgrades are still available to buy
     * @return {@code int} indicating how many upgrades available there are
     */
    public int getUpgradeDeckSize() { return this.upgradeMarket.getDeckSize(); }

    /**
     * Get all the upgrades on a player's board column
     * @param playerInd {@code int} index of the player that you want to check for upgrades
     * @param column {@code ColumnColor} of the column to verify
     * @return {@code List<String>} containing all the upgrades names from that column
     */
    public List<String> getPlayerColumnUpgrades(int playerInd, ColumnColor column) {
        if (playerIndIsInValid(playerInd) || column == null) return Collections.emptyList();
        
        return players[playerInd].getBoard().getColumnUpgradeCodes(column);
    }

    /**
     * Verify if the player can buy a free upgrade (completed a row)
     * @param playerInd {@code int} index of the player that you want to check
     * @return {@code true} if the player can buy one for free, {@code false} otherwise
     */
    public boolean hasFreeUpgradePending(int playerInd) {
        if (playerIndIsInValid(playerInd)) return false;

        return players[playerInd].hasFreeUpgradePending();
    }

    /**
     * Get how many free upgrades the player can get
     * @param playerInd {@code int} index of the player that you want to check
     * @return {@code int} amount of free upgrades that the player can buy
     */
    public int getFreeUpgradesPendingCount(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;
        
        return players[playerInd].getBoard().getFreeUpgradesPendingCount();
    }

    /**
     * Get how many free upgrades the player has already got
     * @param playerInd {@code int} index of the player that you want to check
     * @return {@code int} the amount of free upgrades that the player has already got
     */
    public int getPlayerRewardedRows(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;
        
        return players[playerInd].getBoard().getRewardedRows();
    }

    /**
     * Action of buying an upgrade from the market
     * @param playerInd {@code int} index of the player that is buying
     * @param slotIndex {@code int} index of the upgrade slot
     * @return {@code true} if the purchase was successful, {@code false} otherwise
     */
    public boolean buyUpgradeFromMarket(int playerInd, int slotIndex) {
        if (playerIndIsInValid(playerInd)) return false;
        
        Player p = players[playerInd];
        Upgrade candidate = this.upgradeMarket.peekAvailable(slotIndex);
        if (candidate == null) return false;
        
        if (!p.buyUpgrade(candidate.getColor(), candidate)) return false;
        
        this.upgradeMarket.takeAvailable(slotIndex);
        checkAutoOpenCounselors(playerInd);
        notifyObservers();
        return true;
    }

    /**
     * Action of the player to buy the free upgrade
     * @param playerInd {@code int} index of the player that you want to check
     * @param slotIndex {@code int} the index of the free upgrade
     * @return {@code true} if the player could get a free upgrade from the market, {@code false} otherwise
     */
    public boolean claimFreeUpgradeFromMarket(int playerInd, int slotIndex) {
        if (playerIndIsInValid(playerInd)) return false;
        
        Player p = players[playerInd];
        if (!p.hasFreeUpgradePending()) return false;
        
        Upgrade candidate = this.upgradeMarket.peekAvailable(slotIndex);
        if (candidate == null) return false;
        
        if (!p.receiveFreeUpgrade(candidate)) return false;
        
        this.upgradeMarket.takeAvailable(slotIndex);
        checkAutoOpenCounselors(playerInd);
        notifyObservers();
        return true;
    }

    /**
     * Action of the player to buy the free upgrade from the stock
     * @param playerInd {@code int} index of the player that you want to check
     * @return {@code true} if the player could get a free upgrade from the deck, {@code false} otherwise
     */
    public boolean claimFreeUpgradeFromDeck(int playerInd) {
        if (playerIndIsInValid(playerInd)) return false;
        
        Player p = players[playerInd];
        if (!p.hasFreeUpgradePending()) return false;
        
        if (p.getBoard().getColumn(ColumnColor.WHITE).getUpgradeCount() >= Column.MAX_UPGRADES) {
            return false;
        }
        Upgrade drawn = this.upgradeMarket.drawFromDeck();
        if (drawn == null) return false;
        
        if (!p.receiveFreeUpgrade(drawn)) return false;
        checkAutoOpenCounselors(playerInd);
        notifyObservers();
        return true;
    }
    
    /*
     * ============================================
     * ================== Player ==================
     * ============================================
     */

    /**
     * Move the current player to another
     * @param playerInd {@code int} index of the next player
     */
    public void setCurrentPlayer(int playerInd) {
        currentPlayer = playerInd;
        notifyObservers();
    }

    /**
     * Get how many players there are in the game
     * @return {@code int} how many players there are, 0 if there is no game started
     */
    public int getPlayerCount() { return players == null ? 0 : players.length; } 
    
    /**
     * Get current node id of the player
     * @param playerInd {@code int} index of the player that you want to check
     * @return {@code String} of the nodeId of that player
     */
    public String getPlayerNodeId(int playerInd) {
        if (playerIndIsInValid(playerInd)) return "CENTRAL";
        
        return getMap().getNodeId(players[playerInd].getCurrentNode());
    }

    /**
     * Get how many pawns that player have
     * @param playerInd {@code int} index of the player that you want to check the amount of pawns
     * @return {@code int} amount of pawns of that player
     */
    public int getPawnCount(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 1;
        
        return players[playerInd].getPawnCount();
    }
    
    /**
     * Get the color of the player from its index
     * @param playerInd {@code int} index of the player that you want to get the color
     * @return {@code PlayerColor} of that player, {@code PlayerColor.BLUE} as default
     */
    public PlayerColor getPlayerColor(int playerInd) {
        if (playerIndIsInValid(playerInd)) return PlayerColor.BLUE;

        return players[playerInd].getColor();
    }

    /**
     * Get the Heir name from a player
     * @param playerInd {@code int} index of the player that you want to get the Heir name
     * @return {@code String} name of the Heir from that player
     */
    public String getPlayerHeirName(int playerInd) {
        if (playerIndIsInValid(playerInd)) return Heir.TOLUI.getDisplayName();
        
        return players[playerInd].getHeir().getDisplayName();
    }

    /**
     * Get the amount of a tribute from a player
     * @param playerInd {@code int} index of the player that you want to get the tribute amount
     * @param type {@code TributeType} type of the tribute that want to get info about
     * @return {@code int[]} in {@code TributePosition.TRIBUTE.ordinal()} position with the amount of such tribute that the player has.
     * {@code TributePosition.VIRTUAL.ordinal()} position with the virtual amount of that tribute
     */
    public int[] getPlayerTributeAmount(int playerInd, TributeType type) {
        if (playerIndIsInValid(playerInd) || type == null) return null;
        
        int[] ret = new int[2];
        ret[TributePosition.TRIBUTE.ordinal()] = players[playerInd].getTributeAmount(type);
        ret[TributePosition.VIRTUAL.ordinal()] = players[playerInd].getVirtualTributeAmount(type);
        return ret;
    }

    /**
     * Get how many of one tribute the player really have
     * @param playerInd {@code int} index of the player that you want to get the tribute amount
     * @param type {@code TributeType} type of the tribute that want to get info about
     * @return {@code int} the amount that playerInd has of such tribute
     */
    public int getPlayerTributeRaw(int playerInd, TributeType type) {
        if (playerIndIsInValid(playerInd) || type == null) return 0;
        
        return getPlayerTributeAmount(playerInd, type)[TributePosition.TRIBUTE.ordinal()];
    }

    /**
     * Get the amount of a tribute from a player
     * @param playerInd {@code int} index of the player that you want to get the treasure amount
     * @param type {@code TreasureType} type of the treasure that want to get info about
     * @return {@code int[]} in {@code TributePosition.TRIBUTE.ordinal()} position with the amount of such treasure that the player has.
     * {@code TributePosition.VIRTUAL.ordinal()} position with the virtual amount of that treasure
     */
    public int[] getPlayerTreasure(int playerInd, TreasureType type) {
        if (playerIndIsInValid(playerInd) || type == null) return null;
        
        int[] ret = new int[2];
        ret[TributePosition.TRIBUTE.ordinal()] = players[playerInd].getTreasureAmount(type);
        ret[TributePosition.VIRTUAL.ordinal()] = players[playerInd].getVirtualTreasureAmount(type);
        return ret;
    }
    
    /**
     * Similar to {@link #getPlayerTreasure(int, TreasureType)}, but just returns the amount of treasure that the player really have
     * @param playerInd {@code int} index of the player that you want to get the treasure amount
     * @param type {@code TreasureType} type of the treasure that want to get info about
     * @return {@code int} the amount of such treasure that the player has.
     */
    public int getPlayerTreasureRaw(int playerInd, TreasureType type) {
        if (playerIndIsInValid(playerInd) || type == null) return 0;
        
        return getPlayerTreasure(playerInd, type)[TributePosition.TRIBUTE.ordinal()];
    }
    
    /**
     * Get how many yurts the player can still place
     * @param playerInd {@code int} index of the player
     * @return {@code int} the amount that the player has left
     */
    public int getPlayerYurtsLeft(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;
        
        return players[playerInd].getYurtsLeft();
    }

    /**
     * Get the total amount of yurts that the player have placed
     * @param playerInd {@code int} index of the player
     * @return {@code int} the amount of yurts that the player have placed
     */
    public int getPlayerAmountYurts(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;
        
        return players[playerInd].getAmountYurts();
    }
    
    /**
     * Get how many votes a player have at that time
     * @param playerInd {@code int} index of the player
     * @return {@code int} amount of votes that the player has at the moment
     */
    public int getPlayerVotes(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;
        
        return players[playerInd].getVotes();
    }

    /**
     * Get how many cities a player have conquered
     * @param playerInd {@code int} index of the player
     * @return {@code int} amount of cities that the player has conquered
     */
    public int getPlayerConqueredCities(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;
        
        return players[playerInd].getConqueredCities();
    }
    
    /**
     * Get all the bonus card that a player have
     * @param playerInd {@code int} index of the player
     * @return {@code List<String>} containing all the names of the bonus cards that the player have
     */
    public List<String> getPlayerBonusCardNames(int playerInd) {
    	if (playerIndIsInValid(playerInd)) return null;
    	
    	List<String> names = new ArrayList<>();
    	for (BonusCard card : players[playerInd].getBonuses()) names.add(card.getDisplayName());
    	return names;
    }
    
    /**
     * Get all the bonus cards that the player can still use it
     * @param playerInd {@code int} index of the player
     * @return {@code List<String>} containing all the names of the cards that the player have not used
     */
    public List<String> getPlayerUnusedBonusCardNames(int playerInd) {
    	if (playerInd < 0 || playerInd >= players.length || players == null || players[playerInd] == null) return null;
    	
    	List<String> names = new ArrayList<>();
    	for (BonusCard card : players[playerInd].getBonuses()) {
    		if (!card.hasBeenUsed()) names.add(card.getDisplayName());
    	}
    	return names;
    }
    
    /**
     * Get all the bonus cards that the player have already used
     * @param playerInd {@code int} index of the player
     * @return {@code List<String>} containing all the names of the cards that the player have already used
     */
    public List<String> getPlayerUsedBonusCardNames(int playerInd) {
    	if (playerIndIsInValid(playerInd)) return null;
    	
    	List<String> names = new ArrayList<String>();
    	for (BonusCard card : players[playerInd].getBonuses()) {
    		if (card.hasBeenUsed()) names.add(card.getDisplayName());
    	}
    	return names;
    }
    
    /**
     * Get how many movements are left for the player
     * @param playerInd {@code int} index of the player
     * @return {@code int} amount of movements left
     */
    public int getMovementsLeft(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;

        return players[playerInd].getMovementsLeft();
    }
    
    /**
     * Get how many "hands" the player has left
     * @param playerInd {@code int} index of the player
     * @return {@code int} amount of hands left in this turn
     */
    public int getHandsLeft(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;

        return players[playerInd].getHandsLeft();
    }
    
    /**
     * Verify if the player still needs to move Khan to end the turn
     * @param playerInd {@code int} index of the player
     * @return {@code true} if the player still needs to move Khan, {@code false} otherwise
     */
    public boolean isKhanPending(int playerInd) {
        if (playerIndIsInValid(playerInd)) return false;
        
        return players[playerInd].mustUseKhan();
    }
    
    /**
     * Verify if the column is free to activate
     * @param playerInd {@code int} index of the player
     * @param column {@code ColumnColor} column that wants to be verified
     * @return {@code true} if the column haven't been activated before, therefore valid. 
     * {@code false} otherwise
     */
    public boolean isColumnFree(int playerInd, ColumnColor column) {
        if (playerIndIsInValid(playerInd) || column == null) return false;
        
        return players[playerInd].canActivate(column);
    }  
    
    /*
     * ============================================
     * ============ Scoring Methods ===============
     * ============================================
     */
    
    /**
     * Calculate the player score
     * @param playerInd {@code int} index of the player
     * @return {@code int} how many votes that player have
     */
    public int calculatePlayerTotalScore(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;
        
        Player p = players[playerInd];
        evaluatePrivateCounselorProgress(p);
        int privatePts = p.getSecretCard() != null ? p.getSecretCard().gainPoints() : 0;
        return p.getVotes() + calculateInfluenceVotes(playerInd) + privatePts;
    }
    
    /*
     * Influence table printed on the main board (index = number of upgrades the
     * player has in the column of the region, from 0 to 4).
     */
    private static final int[] FIRST_PLACE_VOTES  = {1, 1, 2, 2, 3};
    private static final int[] SECOND_PLACE_VOTES = {0, 1, 1, 2, 2};

    /** The upgrade column that is tied to each region of the map. */
    private static ColumnColor columnOfRegion(Region region) {
        switch (region) {
            case RUSSIA: return ColumnColor.YELLOW;
            case PERSIA: return ColumnColor.GREEN;
            default:     return ColumnColor.RED; // CHINA
        }
    }

    /**
     * End-of-game votes for the influence over the 3 regions. In each region the
     * player with more yurts is 1st and the player with the second most is 2nd;
     * the votes come from the table above, using the upgrades the player has in
     * the column of that region.
     * Ties: a tie for 1st leaves nobody in 2nd place; a tie for 2nd puts
     * everybody tied in 2nd place. Nobody wins votes in a region with no yurts.
     *
     * @param playerInd index of the player
     * @return votes won from the 3 regions
     */
    public int calculateInfluenceVotes(int playerInd) {
        if (playerIndIsInValid(playerInd)) return 0;
        
        int total = 0;
        for (Region region : Region.values()) {
            // yurts of every player in this region
            int[] yurts = new int[players.length];
            for (int i = 0; i < players.length; i++) yurts[i] = getMap().getYurtsPerRegion(players[i]).get(region);

            int best = 0;
            for (int y : yurts) best = Math.max(best, y);
            
            if (best == 0 || yurts[playerInd] == 0) continue;

            int upgrades = players[playerInd].getBoard().getColumn(columnOfRegion(region)).getUpgradeCount();
            upgrades = Math.min(upgrades, FIRST_PLACE_VOTES.length - 1);

            if (yurts[playerInd] == best) {
                total += FIRST_PLACE_VOTES[upgrades];
                continue;
            }

            int tiedForFirst = 0;
            int second = 0;
            
            for (int y : yurts) {
                if (y == best)tiedForFirst++;
                else second = Math.max(second, y);
            }
            
            if (tiedForFirst == 1 && yurts[playerInd] == second) total += SECOND_PLACE_VOTES[upgrades];
        }
        return total;
    }
    
    /*
     * ============================================
     * ============== Open Counselor ==============
     * ============================================
     */
    
    private static void createOpenCounselors() {
        if (openCounselorCards == null) openCounselorCards = OpenCounselor.giveCardsInGame();
    }

    /**
     * Get the openCounselor Ids from the game
     * @return {@code List<String>} containing all the ids for the open counselors in match
     */
    public List<String> getGameOpenCounselorIds() {
        createOpenCounselors();
        return OpenCounselor.getOpenCounselorsIdGame();
    }

    /**
     * Get the name of the Heir who have completed that openCounselor
     * @param cardInd {@code int} index of the open counselor
     * @return {@code String} of the name of the Heir who has completed, {@code null} if no one completed
     */
    public String getOpenCounselorOwnerName(int cardInd) {
        createOpenCounselors();
        if (openCounselorCards == null || cardInd < 0 || cardInd >= openCounselorCards.size())return null;
        
        Player owner = openCounselorCards.get(cardInd).getCompletedBy();
        return owner != null ? owner.getName() : null;
    }

    private void checkAutoOpenCounselors(int playerInd) {
        createOpenCounselors();
        if (openCounselorCards == null || playerIndIsInValid(playerInd)) return;
        
        Player p = players[playerInd];
        for (int i = 0; i < openCounselorCards.size(); i++) {
            OpenCounselor card = openCounselorCards.get(i);
            if (!card.hasBeenCompleted() && verifyOpenCounselorCondition(card, p)) {
                if (card.playerHasCompleted(p)) p.addVotes(1);
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
                for (Region region : Region.values()) if (qty.get(region) < target) return false;

                return true;
            case SWORD:
                return (p.getTributeAmount(TributeType.SWORD) + p.getVirtualTributeAmount(TributeType.SWORD)) >= target;
            case EACHRESOURCE:
                for (TreasureType type : TreasureType.values()) if (p.getTreasureAmount(type) < 1) return false;

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
     * ============================================
     * ========== Private Counselor ===============
     * ============================================
     */
    
    /**
     * Verify if the player needs to chose his private counselor
     * @param playerInd {@code int} index of the player
     * @return {@code true} if the player still needs to chose one private counselor, {@code false} otherwise
     */
    public boolean needsPrivateCounselorChoice(int playerInd) {
        if (playerIndIsInValid(playerInd)) return false;
        
        return players[playerInd].getSecretCard() == null;
    }

    /**
     * Action of the player choosing his private counselor
     * @param playerInd {@code int} index of the player
     * @param name {@code String} name of the private counselor card chosen
     * @return {@code true} if the card could be assigned to the player
     */
    public boolean choosePrivateCounselor(int playerInd, String name) {
        if (!needsPrivateCounselorChoice(playerInd) || name == null) return false;
        
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
    
    private int computePrivateMissionMetric(Player p, PrivateMissionType type) {
        if (p == null || type == null) return 0;
        
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
                for (Upgrade u : upgrades) distinct.add(u.getIcon().name() + "_" + u.getColor().name());
                return distinct.size();
            }
            case POWERUPS_HORSE_HAND: {
                int count = 0;
                for (Upgrade u : upgrades) {
                    if (u.getIcon() == Icon.HORSE || u.getIcon() == Icon.HAND) count++;
                }
                return count;
            }
            case POWERUPS_YURT: {
                int count = 0;
                for (Upgrade u : upgrades) {
                    if (u.getIcon() == Icon.VIRTUAL_YURT) count++;
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
                    if (c >= 2) citiesWithAtLeastTwo++;
                }
                return citiesWithAtLeastTwo;
            }
            case YURT_TRADINGPOST: {
                int postYurts = 0;
                for (String nodeId : getAllNodeIds()) {
                    Node n = nodeById(nodeId);
                    if ((n instanceof DoubleNode || n instanceof CentralNode) && n.hasYurtOf(p)) postYurts++;
                }
                return postYurts;
            }
            case YURT_KHAN: {
                Set<Province> khanProvinces = new HashSet<>();
                for (String provId : getKhanProvincesIds()) {
                    Province kp = provinceById(provId);
                    if (kp != null) khanProvinces.add(kp);
                }
                Set<Province> coveredKhanProvs = new HashSet<>();
                for (String nodeId : getAllNodeIds()) {
                    Node n = nodeById(nodeId);
                    if (n != null && n.hasYurtOf(p)) {
                        for (Province np : n.getProvinces()) {
                            if (khanProvinces.contains(np)) coveredKhanProvs.add(np);
                        }
                    }
                }
                return coveredKhanProvs.size();
            }
            case YURTINSEQUENCE: {
                Set<Node> ownedNodes = new HashSet<>();
                for (String nodeId : getAllNodeIds()) {
                    Node n = nodeById(nodeId);
                    if (n != null && n.hasYurtOf(p)) ownedNodes.add(n);
                }
                int maxChain = 0;
                Set<Node> visited = new HashSet<>();
                for (Node start : ownedNodes) {
                    if (visited.contains(start)) continue;
                    
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
    
    /**
     * Get the names of the candidates for private counselor for the player
     * @return {@code List<String>} containing the 2 names for the private counselor
     */
    public List<String> getPrivateCounselorCandidates() {
        if (offered.isEmpty()) offered = PrivateCounselor.giveTwoCards();
        
        List<String> names = new ArrayList<>();
        for (PrivateCounselor p : offered) names.add(p.getName());
        
        return names;
    }

    /**
     * Get the private counselor name of the player
     * @param playerInd {@code int} index of the player
     * @return {@code String} of the name of private counselor. "None" if no private counselor have been chosen
     */
    public String getPlayerPrivateCounselorName(int playerInd) {
        if (playerIndIsInValid(playerInd)) return "None";
        
        PrivateCounselor card = players[playerInd].getSecretCard();
        return card != null ? card.getName() : "None";
    }
    
    /*
     * ============================================
     * ================ Game Start ================
     * ============================================
     */
    
    /**
     * Set up a new match for Herdeiros de Khan
     * @param heirNames {@code List<String>} containing all the Heir names chosen for the match
     * @param alt {@code true} if it's to use the alternative Karakorum 
     * @return {@code true} if it could initialize everything, {@code false} otherwise
     */
    public boolean setupMatch(List<String> heirNames, boolean alt) {
        altKarakorum = alt;
        Resource.resetResources();
        OpenCounselor.resetDeck();
        PrivateCounselor.resetDeck();
        createOpenCounselors();
        Map.resetMap(altKarakorum);
        
        this.upgradeMarket.resetMarket();
        this.offered.clear();
        players = null;

        if (!createArrayPlayers(heirNames.size())) return false;
        
        for (int i = 0; i < heirNames.size(); i++) assignPlayer(i, PlayerColor.values()[i], heirNames.get(i), (i >= 2) ? 2 : 1);

        this.remainingCityDeck.clear();
        this.activeSackCities.clear();
        this.remainingCityDeck.addAll(getCityNames());
        
        Collections.shuffle(this.remainingCityDeck);
        revealNextCityIfAvailable();

        currentPlayer = 0;
        notifyObservers();
        return true;
    }
    
    private boolean createArrayPlayers(int qtyPlayers) {
        if (qtyPlayers < 2 || qtyPlayers > 5) return false;
        
        players = new Player[qtyPlayers];
        return true;
    }
    
    private boolean assignPlayer(int ind, PlayerColor color, String heirName, int amountCoins) {
        if (players == null || ind < 0 || ind >= players.length) return false;
        
        Player p = new Player(color, Heir.fromName(heirName), getMap().getCentralNode(), amountCoins);
        players[ind] = p;
        return true;
    }
    
    /**
     * Start a player turn
     * @param playerInd {@code int} index of the player
     * @param column {@code ColumnColor} column chosen for the turn
     * @param pawnIndex {@code int} index of the pawn
     * @return {@code true} if it could start the player turn, {@code false} otherwise
     */
    public boolean startTurn(int playerInd, ColumnColor column, int pawnIndex) {
        if (playerIndIsInValid(playerInd)) return false;
        
        if (!players[playerInd].startTurn(column, pawnIndex)) return false;
        notifyObservers();
        return true;
    }

    /**
     * End a player turn
     * @param playerInd {@code int} index of the player
     */
    public void endTurn(int playerInd) {
    	if (playerIndIsInValid(playerInd)) return;
        
    	checkAutoOpenCounselors(playerInd);
        players[playerInd].endTurn();
        offered.clear();
    }
    
    /*
     * ============================================
     * ================= Actions ==================
     * ============================================
     */

    /**
     * Action of moving the pawn along the board
     * @param playerInd {@code int} index of the player
     * @param nodeId {@code String} id of the node that the pawn will move to
     * @return {@code true} if the movement was a success, {@code false} otherwise
     */
    public boolean MovePawn(int playerInd, String nodeId) {
        if (playerIndIsInValid(playerInd) || nodeId == null) return false;
        
        Node target = nodeById(nodeId);
        if (target == null) return false;
        
        newMoveAction(players[playerInd], target, 1);
        return doAction();
    }

    /**
     * Action of creating a yurt on a node
     * @param playerInd {@code int} index of the player
     * @param nodeId {@code String} id of the node that the yurt will be built on
     * @return {@code true} if the yurt could be built, {@code false} otherwise
     */
    public boolean placeYurtOnNode(int playerInd, String nodeId) {
        if (playerIndIsInValid(playerInd) || nodeId == null) return false;
        
        Player p = players[playerInd];
        Node n = nodeById(nodeId);
        if (n == null || !p.hasVisited(n)) return false;
        
        newYurtAction(playerInd, n);
        boolean success = doAction();
        if (success) {
            checkAutoOpenCounselors(playerInd);
            notifyObservers();
        }
        return success;
    }

    /**
     * Action of harvesting a tribute from a province
     * @param playerInd {@code int} index of the player
     * @param nodeId {@code String} id of the node
     * @param provinceId {@code String} id of the province that will collect the resource from
     * @return {@code true} if it could harvest a tribute, {@code false} otherwise
     */
    public boolean harvestProvinceResource(int playerInd, String nodeId, String provinceId) {
        if (playerIndIsInValid(playerInd) || provinceId == null) return false;
        
        Player p = players[playerInd];
        Province province = provinceById(provinceId);
        if (province == null) return false;
        

        Node validNode = null;
        Node candidate = nodeById(nodeId);
        if (candidate != null && p.hasVisited(candidate) && candidate.getProvinces().contains(province)) validNode = candidate;
        else {
            for (Node visited : p.getTurnPath()) {
                if (visited != null && visited.getProvinces().contains(province)) {
                    validNode = visited;
                    break;
                }
            }
        }

        if (validNode == null) return false;
        
        newTributeAction(p, validNode, province);
        boolean success = doAction();
        if (success) {
            checkAutoOpenCounselors(playerInd);
            notifyObservers();
        }
        return success;
    }

    /**
     * Action of sacking a city
     * @param playerInd {@code int} index of the player
     * @param nodeOrCityName {@code String} nodeId or a name of a city
     * @param treasureInd {@code int} index of the treasure to be sacked
     * @param qtySword {@code int} amount of swords required to sack
     * @return {@code true} if the player had the amount of swords, the treasure was available and the city was enabled for sack.
     * {@code false} otherwise
     */
    public boolean attackCityForTreasure(int playerInd, String nodeOrCityName, int treasureInd, int qtySword) {
        if (playerIndIsInValid(playerInd) || nodeOrCityName == null) return false;
        
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

        if (cityInd < 0) return false;

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

    /**
     * Action of a player to move Khan
     * @param playerInd {@code int} index of the player
     * @param provinceId {@code String} id of the province that the Khan will move to. "UPGRADES" to the upgrades area
     * @return {@code true} if the movement was a success, {@code false} otherwise
     */
    public boolean moveKhan(int playerInd, String provinceId) {
        if (playerIndIsInValid(playerInd)) return false;
        
        boolean toUpgrades = "UPGRADES".equalsIgnoreCase(provinceId);
        Province p = toUpgrades ? null : provinceById(provinceId);
        if (!toUpgrades && p == null) return false;
        
        Node currentNode = players[playerInd].getCurrentNode() != null
            ? players[playerInd].getCurrentNode()
            : getMap().getCentralNode();
        
        newKhanAction(playerInd, currentNode, p);
        boolean success = doAction();
        if (success && toUpgrades) {
            int emptySlots = this.upgradeMarket.refreshMarket();
            
            if (emptySlots >= 2) players[playerInd].addTribute(TributeType.COIN, 1);
            if (emptySlots >= 4) players[playerInd].addTribute(TributeType.SWORD, 1);
            
            checkAutoOpenCounselors(playerInd);
            notifyObservers();
        }
        return success;
    }

    /**
     * Action of the player to execute a trade
     * @param playerInd {@code int} index of the player
     * @param nodeId {@code String} nodeId of a DoubleNode or CentralNode
     * @return {@code true} if the player can do the trade, giving him its bonus. 
     * {@code false} otherwise
     */
    public boolean executeTrade(int playerInd, String nodeId) {
        if (playerIndIsInValid(playerInd) || nodeId == null) return false;
        
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
        if (n == null) return false;
        
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
     * ============================================
     * ============== Bonus Cards =================
     * ============================================
     */
    
    /**
     * Get all the paths for the images of the bonus cards
     * @return {@code HashMap<String, String>} mapping the bonus card name to its image path
     */
    public HashMap<String, String> getBonusCardImgPath() { return BonusCard.getImagePath(); }
    
    /**
     * Action to use the bonus card
     * @param name {@code String} name of the bonus card
     * @param playerInd {@code int} index of the player
     * @return {@code true} if the card hasn't been used and could use it.
     * {@code false} otherwise
     */
    public boolean useBonusCard(String name, int playerInd) {
    	if (playerIndIsInValid(playerInd)) return false;
    	
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
    
    /**
     * Get bonus card of a trading post
     * @param nodeId {@code String} of the DoubleNode or CentralNode
     * @return {@code String} of the name of the bonus card
     */
    public String getBonusCardOnPost(String nodeId) {
    	Node n = this.nodeById(nodeId);
    	if (!(n instanceof CentralNode || n instanceof DoubleNode)) return null;
    	
    	TradingPost p = (n instanceof CentralNode) ? ((CentralNode) n).getTradingPost() : ((DoubleNode) n).getTradingPost();
    	
    	return (p.isBonusAvailable()) ? null : p.getBonusCardName();
    }
    
    /**
     * Get the bonus cards coordinate to show on the map
     * @return {@code HashMap<String, int[]>} mapping each bonus card name to its coordinate.
     * The first element of the array is the x coordinate, the second is the y. 
     */
    public HashMap<String, int[]> getBonusCardCoordinates() { return getMap().getBonusCardCoordinates(); }
    
    /*
     * ============================================
     * ================ GameLoader ================
     * ============================================
     */
    
    public void setPlayerTributeRaw(int playerInd, TributeType type, int qty) {
        if (playerIndIsInValid(playerInd) || type == null) return;
        
        players[playerInd].setTributeAmount(type, qty);
    }

    public void setPlayerTreasureRaw(int playerInd, TreasureType type, int qty) {
        if (playerIndIsInValid(playerInd) || type == null) return;
        
        players[playerInd].setTreasureAmount(type, qty);
    }
    
    /** Puts one specific pawn (not only the active one) on the given node. Used when loading a save. */
    public void setPawnNodeId(int playerInd, int pawnIndex, String nodeId) {
        if (playerIndIsInValid(playerInd) || pawnIndex < 0 || pawnIndex >= players[playerInd].getPawnCount()) return;
        
        Node target = nodeById(nodeId);
        if (target != null) {
            Pawn pawn = players[playerInd].getPawn(pawnIndex);
            if (pawn.getCurrentNode() != null) pawn.getCurrentNode().leaveNode();
            
            target.enterNode();
            pawn.moveTo(target);
            notifyObservers();
        }
    }
    
    public void restoreNodeYurt(String nodeId, int playerInd) {
        if (playerIndIsInValid(playerInd)) return;
        
        Node node = nodeById(nodeId);
        if (node != null) node.buildYurt(players[playerInd]);
    }

    public void setTurnPathNodeIds(int playerInd, List<String> nodeIds) {
        if (playerIndIsInValid(playerInd)) return;
        
        List<Node> nodes = new ArrayList<>();
        if (nodeIds != null) {
            for (String id : nodeIds) {
                Node n = nodeById(id);
                if (n != null) nodes.add(n);
            }
        }
        players[playerInd].setTurnPath(nodes);
        notifyObservers();
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

    public void setProvinceResourceCount(String provinceId, int qty) {
        Province p = provinceById(provinceId);
        if (p != null) {
            p.resourceCount = Math.max(0, Math.min(3, qty));
            notifyObservers();
        }
    }

    public void restoreKhanState(boolean atUpgrades, String provinceId) {
        Province target = atUpgrades ? null : provinceById(provinceId);
        getMap().getKhan().restoreLocation(atUpgrades, target);
        notifyObservers();
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
                                } catch (IllegalArgumentException ignored) { }
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
                    if (city != null) this.remainingCityDeck.add(city.getName());
                }
            }
        }
    }    
    
    public void restoreAvailableUpgrades(List<String> codes) { this.upgradeMarket.restoreAvailableByCodes(codes); }

    public void restorePlayerColumnUpgrades(int playerInd, ColumnColor column, List<String> codes) {
        if (playerIndIsInValid(playerInd) || column == null) return;
        
        players[playerInd].getBoard().restoreColumnUpgrades(column, codes);
    }

    public void restorePlayerRewardedRows(int playerInd, int rewardedRows) {
        if (playerIndIsInValid(playerInd)) return;
        
        players[playerInd].getBoard().setRewardedRows(rewardedRows);
    }    

    /** Puts the conqueror back on a city when loading a saved match. */
    public void restoreCityConqueror(String cityName, int playerInd) {
        int cityInd = findCityIndexFlexible(cityName);
        if (cityInd < 0 || playerIndIsInValid(playerInd)) return;
        
        getMap().getCity(cityInd).restoreConqueror(players[playerInd]);
    }

    public void setPlayerYurtsState(int playerInd, int left, int placed) {
        if (playerIndIsInValid(playerInd)) return;
        
        players[playerInd].setYurtsState(left, placed);
    }

    public void setPlayerVotes(int playerInd, int votes) {
        if (playerIndIsInValid(playerInd)) return;
        
        players[playerInd].setVotes(votes);
    }

    public void setPlayerConqueredCities(int playerInd, int count) {
        if (playerIndIsInValid(playerInd)) return;
        
        players[playerInd].setConqueredCities(count);
    }

    public void restorePlayerBonusCard(int playerInd, List<String> names, List<String> usedNames) {
        if (playerIndIsInValid(playerInd)) return;
        
        
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

    public void setMovementsLeft(int playerInd, int steps) {
    	if (playerIndIsInValid(playerInd)) return;
        
        players[playerInd].setMovementsLeft(steps);
        notifyObservers();
    }

    public void setHandsLeft(int playerInd, int hands) {
    	if (playerIndIsInValid(playerInd)) return;
    	
        players[playerInd].setHandsLeft(hands);
        notifyObservers();
    }

    public void setKhanPending(int playerInd, boolean pending) {
    	if (playerIndIsInValid(playerInd)) return;
    	
        players[playerInd].setKhanPending(pending);
        notifyObservers();
    }

    public void setColumnOccupied(int playerInd, ColumnColor column, boolean occupied) {
    	if (playerIndIsInValid(playerInd)) return;

        players[playerInd].setColumnOccupied(column, occupied);
    }

    /**
     * Restores a player's Secret Counselor card directly when loading a saved match.
     */
    public void restorePrivateCounselor(int playerInd, String cardName) {
        if (playerIndIsInValid(playerInd)) return;
        
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
}
