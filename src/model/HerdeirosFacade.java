package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Public facade providing controlled access to the package-private domain model.
 * Enforces encapsulation while exposing topological queries and board actions
 * required by the View, Controller, and Loader components.
 */
public class HerdeirosFacade {

    private final Map<String, Node> boardNodes;
    private final Map<String, Province> boardProvinces;
    private final Map<Province, List<Province>> khanArrowGraph;
    private final List<Player> players;
    private final Khan genghisKhan;
    private boolean alternativeKarakorum;

    /**
     * Initializes the game model facade with default board containers.
     */
    public HerdeirosFacade() {
        this.boardNodes = new HashMap<>();
        this.boardProvinces = new HashMap<>();
        this.khanArrowGraph = new HashMap<>();
        this.players = new ArrayList<>();
        this.genghisKhan = new Khan();
        this.alternativeKarakorum = false;
        initializeDefaultTopology();
    }

    /**
     * Configures the match parameters chosen on the initial setup screen.
     *
     * @param heirNames            List of selected heir names for each active player.
     * @param alternativeKarakorum True to use the alternative Karakorum trading post setup; false for default.
     */
    public void setupMatch(List<String> heirNames, boolean alternativeKarakorum) {
        this.players.clear();
        for (String name : heirNames) {
            // [TEAMMATE INTEGRATION - PLAYER]: Instantiates the Player class developed by your teammate.
            this.players.add(new Player(name));
        }
        this.alternativeKarakorum = alternativeKarakorum;
        this.boardNodes.put("Karakorum", new CentralNode(alternativeKarakorum));
    }

    /**
     * Populates the default board graph with nodes and provinces.
     */
    private void initializeDefaultTopology() {
        CentralNode karakorum = new CentralNode(false);
        SingleNode kiev = new SingleNode("Russia", "Kiev");
        SingleNode samarkand = new SingleNode("Persia", "Samarkand");
        SingleNode beijing = new SingleNode("China", "Beijing");

        TreasureRequirements[] counselorReqs = new TreasureRequirements[] {
            new TreasureRequirements(TreasureType.FUR, 1),
            new TreasureRequirements(TreasureType.SILK, 1)
        };
        DoubleNode persiaPost = new DoubleNode("Persia", new TradingPost(2, counselorReqs, 0));

        this.boardNodes.put("Karakorum", karakorum);
        this.boardNodes.put("Kiev", kiev);
        this.boardNodes.put("Samarkand", samarkand);
        this.boardNodes.put("Beijing", beijing);
        this.boardNodes.put("PersiaPost", persiaPost);

        karakorum.addNeighbor(kiev);
        karakorum.addNeighbor(samarkand);
        karakorum.addNeighbor(beijing);
        samarkand.addNeighbor(persiaPost);

        Province russiaCoin = new Province(TributeType.COIN);
        Province persiaSword = new Province(TributeType.SWORD);
        Province chinaYurt = new Province(TributeType.YURT);

        this.boardProvinces.put("Prov_Russia_1", russiaCoin);
        this.boardProvinces.put("Prov_Persia_1", persiaSword);
        this.boardProvinces.put("Prov_China_1", chinaYurt);

        kiev.addProvince(russiaCoin);
        samarkand.addProvince(persiaSword);
        beijing.addProvince(chinaYurt);

        List<Province> russiaAdjacents = new ArrayList<>();
        russiaAdjacents.add(persiaSword);
        russiaAdjacents.add(chinaYurt);
        this.khanArrowGraph.put(russiaCoin, russiaAdjacents);
    }

    /**
     * Executes a tribute harvest on a specific province if adjacent to the player's current node.
     *
     * @param currentNodeId The ID of the node where the active pawn is located.
     * @param provinceId    The ID of the target province to harvest from.
     * @return True if a tribute resource was successfully collected; false otherwise.
     */
    public boolean harvestProvinceResource(String currentNodeId, String provinceId) {
        Node node = this.boardNodes.get(currentNodeId);
        Province targetProvince = this.boardProvinces.get(provinceId);
        if (node == null || targetProvince == null) {
            return false;
        }
        if (node.getProvinces().contains(targetProvince)) {
            return targetProvince.collectResource();
        }
        return false;
    }

    /**
     * Plunders a treasure from a city node.
     *
     * @param nodeId        The identifier of the city node.
     * @param treasureIndex The index of the treasure slot to plunder.
     * @return The name of the plundered treasure, or null if the action is invalid.
     */
    public String attackCityForTreasure(String nodeId, int treasureIndex) {
        Node node = this.boardNodes.get(nodeId);
        if (node instanceof SingleNode) {
            TreasureType loot = ((SingleNode) node).plunderTreasure(treasureIndex);
            return loot != null ? loot.name() : null;
        }
        return null;
    }

    /**
     * Attempts to place a Yurt on the specified node for the active player.
     *
     * @param nodeId     The target node identifier.
     * @param playerName The name of the heir building the yurt.
     * @return True if the yurt was legally constructed; false otherwise.
     */
    public boolean placeYurtOnNode(String nodeId, String playerName) {
        Node node = this.boardNodes.get(nodeId);
        Player targetPlayer = findPlayerByName(playerName);
        if (node == null || targetPlayer == null) {
            return false;
        }
        return node.buildYurt(targetPlayer);
    }

    /**
     * Moves Genghis Khan either to the global upgrades area or to a specific province,
     * triggering cascading tribute generation.
     *
     * @param targetProvinceId The province ID, or "UPGRADES" to move to the upgrade area.
     * @return True if the Khan was moved; false if the target province does not exist.
     */
    public boolean moveKhan(String targetProvinceId) {
        if ("UPGRADES".equalsIgnoreCase(targetProvinceId)) {
            this.genghisKhan.moveToUpgradesArea();
            return true;
        }
        Province target = this.boardProvinces.get(targetProvinceId);
        if (target == null) {
            return false;
        }
        List<Province> adjacents = this.khanArrowGraph.getOrDefault(target, Collections.emptyList());
        this.genghisKhan.moveToProvince(target, adjacents);
        return true;
    }

    /**
     * Executes a treasure trade at a DoubleNode or CentralNode trading post.
     *
     * @param nodeId          The node hosting the trading post.
     * @param offeredAmounts  Map pairing treasure type names with offered quantities.
     * @return True if the trade contract was fulfilled; false otherwise.
     */
    public boolean executeTrade(String nodeId, Map<String, Integer> offeredAmounts) {
        Node node = this.boardNodes.get(nodeId);
        TradingPost post = null;
        if (node instanceof DoubleNode) {
            post = ((DoubleNode) node).getTradingPost();
        } else if (node instanceof CentralNode) {
            post = ((CentralNode) node).tradingPost;
        }
        if (post == null) {
            return false;
        }

        List<TreasureRequirements> offerList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : offeredAmounts.entrySet()) {
            try {
                TreasureType type = TreasureType.valueOf(entry.getKey());
                offerList.add(new TreasureRequirements(type, entry.getValue()));
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }
        TreasureRequirements[] offerArray = offerList.toArray(new TreasureRequirements[0]);
        return post.processDelivery(offerArray);
    }

    /**
     * Checks if a given node currently contains at least one constructed yurt.
     *
     * @param nodeId The node identifier.
     * @return True if a yurt is present on the node.
     */
    public boolean nodeHasYurt(String nodeId) {
        Node node = this.boardNodes.get(nodeId);
        return node != null && node.hasYurt();
    }

    /**
     * Retrieves the current resource count of a given province.
     *
     * @param provinceId The province identifier.
     * @return The number of tribute tokens currently on the province (0 to 3).
     */
    public int getProvinceResourceCount(String provinceId) {
        Province province = this.boardProvinces.get(provinceId);
        return province != null ? province.resourceCount : 0;
    }

    /**
     * Sets the resource count of a province directly (used by the state loader).
     *
     * @param provinceId The province identifier.
     * @param count      The tribute count to restore.
     */
    public void setProvinceResourceCount(String provinceId, int count) {
        Province province = this.boardProvinces.get(provinceId);
        if (province != null) {
            province.resourceCount = Math.max(0, Math.min(3, count));
        }
    }

    /**
     * Checks whether Genghis Khan is currently positioned at the upgrades area.
     *
     * @return True if at the upgrades market; false otherwise.
     */
    public boolean isKhanAtUpgradesArea() {
        return this.genghisKhan.isAtUpgradesArea();
    }

    /**
     * Returns all registered node identifiers on the board.
     *
     * @return Unmodifiable set of node IDs.
     */
    public Iterable<String> getAllNodeIds() {
        return Collections.unmodifiableSet(this.boardNodes.keySet());
    }

    /**
     * Returns all registered province identifiers on the board.
     *
     * @return Unmodifiable set of province IDs.
     */
    public Iterable<String> getAllProvinceIds() {
        return Collections.unmodifiableSet(this.boardProvinces.keySet());
    }

    /**
     * Checks if the match is using the alternative Karakorum setup.
     *
     * @return True if alternative setup is active.
     */
    public boolean isAlternativeKarakorum() {
        return this.alternativeKarakorum;
    }

    private Player findPlayerByName(String name) {
        for (Player p : this.players) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return new Player(name);
    }
}
