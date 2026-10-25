package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

enum NodePosition {
    TOP,
    RIGHT,
    LEFT
}

enum CityPosition {
    CITY1,
    CITY2,
    CITY3
}

/**
 * Represents the board game map, managing cities, topological nodes,
 * provinces, trading posts, and the Khan token.
 */
class Map {
    private final ArrayList<City> cityArray = new ArrayList<>();
    private final ArrayList<ArrayList<Node>> nodeArray = new ArrayList<>();
    private final EnumMap<Region, String[]> cityNames = new EnumMap<>(Region.class);
    private final EnumMap<ProvinceId, Province> provinces = new EnumMap<>(ProvinceId.class);
    final EnumMap<TributeType, ProvinceId[]> provinceType = new EnumMap<>(TributeType.class);

    private final Khan khan;

    private final int top = NodePosition.TOP.ordinal();
    private final int right = NodePosition.RIGHT.ordinal();
    private final int left = NodePosition.LEFT.ordinal();

    private final int city1 = 3 * CityPosition.CITY1.ordinal();
    private final int city2 = 3 * CityPosition.CITY2.ordinal();
    private final int city3 = 3 * CityPosition.CITY3.ordinal();

    private final int trade1 = city3 + 3;
    private final int trade2 = trade1 + 1;

    private static Map map = null;

    private Map(boolean alternativeKarakorum) {
        createCities();
        createProvinces();
        initializeNodes(alternativeKarakorum);
        initializeConnections();
        connectNodeProvinces();
        connectKhanProvinces();
        this.khan = new Khan();
    }

    static Map getMap(boolean altKarakorum) {
        if (map == null) {
            map = new Map(altKarakorum);
        }
        return map;
    }

    static Map resetMap(boolean altKarakorum) {
        map = new Map(altKarakorum);
        return map;
    }

    private void createCities() {
        cityNames.put(Region.CHINA, new String[] { "Pequim", "Kaesong", "Cantão" });
        cityNames.put(Region.RUSSIA, new String[] { "Sarai", "Moscou", "Kiev" });
        cityNames.put(Region.PERSIA, new String[] { "Samarcanda", "Cabul", "Bagdá" });

        cityArray.clear();
        for (Region region : Region.values()) {
            String[] names = cityNames.get(region);
            for (int i = 0; i < 3; i++) {
                cityArray.add(new City(names[i]));
            }
        }
    }

    private void createProvinces() {
        provinces.clear();
        provinceType.clear();

        provinceType.put(TributeType.SWORD, new ProvinceId[] {
            ProvinceId.CHINA_SWORD1, ProvinceId.CHINA_SWORD2, ProvinceId.PERSIA_SWORD1, ProvinceId.RUSSIA_SWORD1
        });
        provinceType.put(TributeType.COIN, new ProvinceId[] {
            ProvinceId.CHINA_COIN1, ProvinceId.PERSIA_COIN1, ProvinceId.PERSIA_COIN2, ProvinceId.RUSSIA_COIN1
        });
        provinceType.put(TributeType.YURT, new ProvinceId[] {
            ProvinceId.CHINA_YURT1, ProvinceId.PERSIA_YURT1, ProvinceId.RUSSIA_YURT1, ProvinceId.RUSSIA_YURT2
        });

        for (TributeType type : TributeType.values()) {
            for (ProvinceId id : provinceType.get(type)) {
                provinces.put(id, new Province(type));
            }
        }
    }

    private City findCityByName(String name) {
        for (City c : cityArray) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return new City(name);
    }

    private void initializeNodes(boolean alternativeKarakorum) {
        nodeArray.clear();
        for (Region region : Region.values()) {
            ArrayList<Node> regionNodes = new ArrayList<>();
            String[] names = cityNames.get(region);

            for (int i = 0; i < 3; i++) {
                City sharedCity = findCityByName(names[i]);
                for (int j = 0; j < 3; j++) {
                    regionNodes.add(new SingleNode(region.toString(), sharedCity));
                }
            }

            switch (region) {
                case RUSSIA:
                    regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
                        new TreasureRequirements(TreasureType.SILK, 2)
                    }, 0)));
                    regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
                        new TreasureRequirements(TreasureType.IRON, 2)
                    }, 0)));
                    break;
                case CHINA:
                    regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
                        new TreasureRequirements(TreasureType.ANIMAL, 2)
                    }, 0)));
                    regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {}, 3)));
                    break;
                case PERSIA:
                    regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
                        new TreasureRequirements(TreasureType.FUR, 2)
                    }, 0)));
                    regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
                        new TreasureRequirements(TreasureType.SPICE, 2)
                    }, 0)));
                    break;
                default:
                    break;
            }

            nodeArray.add(regionNodes);
        }

        ArrayList<Node> centralCity = new ArrayList<>();
        centralCity.add(new CentralNode(alternativeKarakorum));
        nodeArray.add(centralCity);
    }

    private void connect(Region regionA, int nodeAInd, Region regionB, int nodeBInd) {
        Node a = nodeArray.get(regionA.ordinal()).get(nodeAInd);
        Node b = nodeArray.get(regionB.ordinal()).get(nodeBInd);
        a.addNeighbor(b);
    }

    private void connectCityNodes(Region region, int baseNodeIndex) {
        Node a = nodeArray.get(region.ordinal()).get(baseNodeIndex);
        Node b = nodeArray.get(region.ordinal()).get(baseNodeIndex + 1);
        Node c = nodeArray.get(region.ordinal()).get(baseNodeIndex + 2);

        a.addNeighbor(b);
        a.addNeighbor(c);
        b.addNeighbor(c);
    }

    private void initializeConnections() {
        Node centralNode = nodeArray.get(3).get(0);

        for (Region region : Region.values()) {
            for (int i = 0; i < 3; i++) {
                connectCityNodes(region, i * 3);
            }

            switch (region) {
                case CHINA:
                    nodeArray.get(region.ordinal()).get(city1 + top).addNeighbor(centralNode);

                    connect(Region.CHINA, city1 + top, Region.CHINA, city2 + left);
                    connect(Region.CHINA, city1 + right, Region.CHINA, city3 + top);

                    connect(Region.CHINA, city1 + left, Region.CHINA, trade2);
                    connect(Region.CHINA, city2 + left, Region.CHINA, trade1);
                    connect(Region.CHINA, city3 + top, Region.CHINA, trade1);

                    connect(Region.CHINA, city2 + top, Region.RUSSIA, trade2);
                    connect(Region.CHINA, trade2, Region.PERSIA, city2 + right);
                    connect(Region.CHINA, city1 + left, Region.PERSIA, city2 + top);
                    break;
                case RUSSIA:
                    nodeArray.get(region.ordinal()).get(city1 + left).addNeighbor(centralNode);

                    connect(Region.RUSSIA, city1 + left, Region.RUSSIA, city2 + top);
                    connect(Region.RUSSIA, city2 + left, Region.RUSSIA, city3 + top);

                    connect(Region.RUSSIA, city3 + top, Region.RUSSIA, trade1);
                    connect(Region.RUSSIA, city2 + top, Region.RUSSIA, trade1);
                    connect(Region.RUSSIA, city1 + right, Region.RUSSIA, trade2);
                    nodeArray.get(region.ordinal()).get(trade2).addNeighbor(centralNode);

                    connect(Region.RUSSIA, city3 + right, Region.PERSIA, trade1);
                    connect(Region.RUSSIA, city2 + left, Region.PERSIA, trade1);
                    connect(Region.RUSSIA, trade2, Region.CHINA, city2 + top);
                    connect(Region.RUSSIA, city2 + right, Region.PERSIA, city1 + top);
                    break;
                case PERSIA:
                    nodeArray.get(region.ordinal()).get(city1 + right).addNeighbor(centralNode);

                    connect(Region.PERSIA, city1 + right, Region.PERSIA, city2 + top);
                    connect(Region.PERSIA, city1 + left, Region.PERSIA, city3 + top);

                    connect(Region.PERSIA, city1 + top, Region.PERSIA, trade1);
                    connect(Region.PERSIA, city3 + top, Region.PERSIA, trade1);
                    connect(Region.PERSIA, city3 + right, Region.PERSIA, trade2);
                    connect(Region.PERSIA, city2 + top, Region.PERSIA, trade2);

                    connect(Region.PERSIA, city1 + top, Region.RUSSIA, city2 + right);
                    connect(Region.PERSIA, city2 + top, Region.CHINA, city1 + left);
                    connect(Region.PERSIA, city2 + right, Region.CHINA, trade2);
                    connect(Region.PERSIA, trade1, Region.RUSSIA, city3 + right);
                    connect(Region.PERSIA, trade1, Region.RUSSIA, city2 + left);
                    break;
                default:
                    break;
            }
        }
    }

    private void connectProvinceToNodes(ProvinceId id, Node... nodes) {
        for (Node node : nodes) {
            node.addProvince(provinces.get(id));
        }
    }

    private void connectNodeProvinces() {
        Node centralNode = nodeArray.get(3).get(0);
        final int russiaPos = Region.RUSSIA.ordinal();
        final int chinaPos = Region.CHINA.ordinal();
        final int persiaPos = Region.PERSIA.ordinal();

        connectProvinceToNodes(ProvinceId.RUSSIA_YURT1,
            nodeArray.get(russiaPos).get(city3 + top),
            nodeArray.get(russiaPos).get(city3 + right),
            nodeArray.get(russiaPos).get(city2 + left),
            nodeArray.get(persiaPos).get(trade1)
        );

        connectProvinceToNodes(ProvinceId.RUSSIA_SWORD1,
            nodeArray.get(russiaPos).get(city3 + top),
            nodeArray.get(russiaPos).get(city2 + left),
            nodeArray.get(russiaPos).get(city2 + top),
            nodeArray.get(russiaPos).get(trade1)
        );

        connectProvinceToNodes(ProvinceId.RUSSIA_YURT2,
            nodeArray.get(russiaPos).get(city2 + top),
            nodeArray.get(russiaPos).get(city2 + right),
            nodeArray.get(russiaPos).get(city1 + left),
            nodeArray.get(persiaPos).get(city1 + top),
            nodeArray.get(persiaPos).get(city1 + right),
            centralNode
        );

        connectProvinceToNodes(ProvinceId.RUSSIA_COIN1,
            nodeArray.get(russiaPos).get(city1 + left),
            nodeArray.get(russiaPos).get(city1 + right),
            nodeArray.get(russiaPos).get(trade2),
            centralNode
        );

        connectProvinceToNodes(ProvinceId.CHINA_SWORD1,
            nodeArray.get(chinaPos).get(city1 + top),
            nodeArray.get(chinaPos).get(city2 + top),
            nodeArray.get(chinaPos).get(city2 + left),
            nodeArray.get(russiaPos).get(trade2),
            centralNode
        );

        connectProvinceToNodes(ProvinceId.CHINA_COIN1,
            nodeArray.get(chinaPos).get(city2 + left),
            nodeArray.get(chinaPos).get(city1 + right),
            nodeArray.get(chinaPos).get(city1 + top),
            nodeArray.get(chinaPos).get(city3 + top),
            nodeArray.get(chinaPos).get(trade1)
        );

        connectProvinceToNodes(ProvinceId.CHINA_YURT1,
            nodeArray.get(chinaPos).get(city1 + right),
            nodeArray.get(chinaPos).get(city1 + left),
            nodeArray.get(chinaPos).get(city3 + top),
            nodeArray.get(chinaPos).get(city3 + left),
            nodeArray.get(chinaPos).get(trade2)
        );

        connectProvinceToNodes(ProvinceId.CHINA_SWORD2,
            nodeArray.get(chinaPos).get(city1 + left),
            nodeArray.get(chinaPos).get(trade2),
            nodeArray.get(persiaPos).get(city2 + right),
            nodeArray.get(persiaPos).get(city2 + top)
        );

        connectProvinceToNodes(ProvinceId.PERSIA_YURT1,
            nodeArray.get(chinaPos).get(city1 + top),
            nodeArray.get(chinaPos).get(city1 + left),
            nodeArray.get(persiaPos).get(city1 + right),
            nodeArray.get(persiaPos).get(city2 + top),
            centralNode
        );

        connectProvinceToNodes(ProvinceId.PERSIA_COIN1,
            nodeArray.get(persiaPos).get(city1 + right),
            nodeArray.get(persiaPos).get(city1 + left),
            nodeArray.get(persiaPos).get(city2 + top),
            nodeArray.get(persiaPos).get(city3 + right),
            nodeArray.get(persiaPos).get(city3 + top),
            nodeArray.get(persiaPos).get(trade2)
        );

        connectProvinceToNodes(ProvinceId.PERSIA_SWORD1,
            nodeArray.get(persiaPos).get(city1 + top),
            nodeArray.get(persiaPos).get(city1 + left),
            nodeArray.get(persiaPos).get(city3 + top),
            nodeArray.get(persiaPos).get(trade1)
        );

        connectProvinceToNodes(ProvinceId.PERSIA_COIN2,
            nodeArray.get(persiaPos).get(city1 + top),
            nodeArray.get(persiaPos).get(trade1),
            nodeArray.get(russiaPos).get(city2 + right),
            nodeArray.get(russiaPos).get(city2 + left)
        );
    }

    private void connectKhanProvinces() {
        ProvinceId[] ids = ProvinceId.values();
        for (int i = 0; i < ids.length; i++) {
            Province current = provinces.get(ids[i]);
            Province prev = provinces.get(ids[(i - 1 + ids.length) % ids.length]);
            Province next = provinces.get(ids[(i + 1) % ids.length]);
            current.addKhanNeighbor(prev);
            current.addKhanNeighbor(next);
        }
    }

    boolean allCityConquered() {
        for (City city : cityArray) {
            if (city.dominatedBy() == null) {
                return false;
            }
        }
        return true;
    }

    boolean setCityConquered(String name, Player player) {
        for (City city : cityArray) {
            if (city.getName().equals(name) && city.canBeSacked()) {
                return city.defineDomination(player);
            }
        }
        return false;
    }

    int getLengthNodeArray() {
        return nodeArray.size();
    }

    int getNodePerRegion() {
        return nodeArray.get(0).size();
    }

    int getAmountCity() {
        return cityArray.size();
    }

    int getAmountProvinces() {
        return provinces.size();
    }

    List<Province> getProvinces() {
        return Collections.unmodifiableList(new ArrayList<>(provinces.values()));
    }

    List<City> createCityCopies() {
        return Collections.unmodifiableList(cityArray);
    }

    Node getCentralNode() {
        return nodeArray.get(3).get(0);
    }

    City getCity(int ind) {
        if (ind < 0 || ind >= getAmountCity()) {
            return null;
        }
        return cityArray.get(ind);
    }

    int getCityInd(String name) {
        for (int i = 0; i < cityArray.size(); i++) {
            if (cityArray.get(i).getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    Province getProvince(int ind) {
        if (ind < 0 || ind >= getAmountProvinces()) {
            return null;
        }
        ProvinceId id = ProvinceId.values()[ind];
        return provinces.get(id);
    }

    Khan getKhan() {
        return khan;
    }

    TradingPost getTradingPost(Region region, boolean firstTradePost) {
        int post = firstTradePost ? trade1 : trade2;
        return ((DoubleNode) nodeArray.get(region.ordinal()).get(post)).getTradingPost();
    }

    EnumMap<Region, Integer> getYurtsPerRegion(Player p) {
        EnumMap<Region, Integer> qty = new EnumMap<>(Region.class);
        for (Region region : Region.values()) {
            qty.put(region, 0);
            for (Node n : nodeArray.get(region.ordinal())) {
                if (n.hasYurtOf(p)) {
                    qty.merge(region, 1, Integer::sum);
                }
            }
        }
        return qty;
    }

    Node getNodeById(String id) {
        if (id == null) {
            return null;
        }
        if ("CENTRAL".equalsIgnoreCase(id) || "Karakorum".equalsIgnoreCase(id)) {
            return getCentralNode();
        }

        int sep = id.lastIndexOf('_');
        if (sep < 0) {
            return null;
        }

        try {
            Region region = Region.valueOf(id.substring(0, sep).toUpperCase());
            int i = Integer.parseInt(id.substring(sep + 1));
            ArrayList<Node> list = nodeArray.get(region.ordinal());
            return (i >= 0 && i < list.size()) ? list.get(i) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    String getNodeId(Node node) {
        if (node == null) {
            return null;
        }
        for (int r = 0; r < nodeArray.size(); r++) {
            int i = nodeArray.get(r).indexOf(node);
            if (i < 0) {
                continue;
            }
            if (r == Region.values().length) {
                return "CENTRAL";
            }
            return Region.values()[r].name() + "_" + i;
        }
        return null;
    }

    List<String> getAllNodeIds() {
        List<String> ids = new ArrayList<>();
        for (ArrayList<Node> list : nodeArray) {
            for (Node n : list) {
                ids.add(getNodeId(n));
            }
        }
        return ids;
    }
}
