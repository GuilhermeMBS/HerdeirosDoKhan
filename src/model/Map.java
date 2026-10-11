package model;

import java.util.*;


/**
 * Must keep this order, or change the order in 
 * {@link #cityNames}
 */
enum Region {
	CHINA,
	RUSSIA,
	PERSIA
}


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
 * <p>The order in here is starting at the left top corner
 * and following clockwise order for all the provinces.</p>
 */
enum ProvinceId {
	RUSSIA_YURT1,
	RUSSIA_SWORD1,
	RUSSIA_YURT2,
	RUSSIA_COIN1,
	CHINA_SWORD1,
	CHINA_COIN1,
	CHINA_YURT1,
	CHINA_SWORD2,
	PERSIA_YURT1,
	PERSIA_COIN1,
	PERSIA_SWORD1,
	PERSIA_COIN2,
}


/**
 * Represents the board game map
 * 
 * <p>It creates and builds all connections between nodes, cities and provinces. 
 * You can sack a city, verify if all cities have been conquered/dominated, and other things</p>
 *  
 * <p>Available methods:</p>
 * <ul>
 * 		<li>{@link #allCityConquered()}</li>
 * 		<li>{@link #setCityConquered(String name, Player player)}</li>
 * 		<li>{@link #getAmountCity()}</li>
 * 		<li>{@link #getAmountProvinces()}</li>
 * 		<li>{@link #getLengthNodeArray()}</li>
 * 		<li>{@link #getNodePerRegion()}</li>
 * </ul>
 * 
 * @author joaop
 * @version 1.5
 */
public class Map {
	private ArrayList<City> cityArray = new ArrayList<City>();
	
	// The order for the nodes will be clockwise, starting from the top (for each city)
	private ArrayList<ArrayList<Node>> nodeArray = new ArrayList<ArrayList<Node>>();
	
	private EnumMap<Region, String[]> cityNames = new EnumMap<>(Region.class);
	
	private final EnumMap<ProvinceId, Province> provinces = new EnumMap<>(ProvinceId.class);
	EnumMap<TributeType, ProvinceId[]> provinceType = new EnumMap<>(TributeType.class);
	
	private final int top = NodePosition.TOP.ordinal();
	private final int right = NodePosition.RIGHT.ordinal();
	private final int left = NodePosition.LEFT.ordinal();
	
	private final int city1 = 3*CityPosition.CITY1.ordinal();
	private final int city2 = 3*CityPosition.CITY2.ordinal();
	private final int city3 = 3*CityPosition.CITY3.ordinal();
	
	// Positions for the trade nodes in the array
	private final int trade1 = city3 + 3;
	private final int trade2 = trade1 + 1;
	
	
	/**
	 * When creating an instance of this variable,
	 * it creates all the city and nodes for the map
	 */
	public Map(boolean alternativeKarakorum) {
		createCities();
		createProvinces();
		initializeNodes(alternativeKarakorum);
		initializeConnections();
		connectNodeProvinces();
	}
	
	
	/**
	 * The cities will be created following clockwise order
	 * starting from connection to the central node
	 */
	private void createCities() {
		cityNames.put(Region.CHINA, new String[] {
			"Pequim", "Kaesong", "Cantão"
		});
		cityNames.put(Region.RUSSIA, new String[] {
			"Sarai", "Moscou", "Kiev"
		});
		cityNames.put(Region.PERSIA, new String[] {
			"Samarcanda", "Cabul", "Bagdá"
		});
		
		cityArray.clear();
		
		for (Region region : Region.values()) {
			String[] names = cityNames.get(region);
			for (int i = 0; i < 3; i++) {
				cityArray.add(new City(names[i]));
			}
		}
		
		return;
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
	
	
	private void initializeNodes(boolean alternativeKarakorum) {
		for (Region region : Region.values()) {
			// Nodes per region
			ArrayList<Node> regionNodes = new ArrayList<Node>();
			String[] names = cityNames.get(region);
			
			for (int i = 0; i < 3; i++) {
				String cityName = names[i];
				for (int j = 0; j < 3; j++) {
					regionNodes.add(new SingleNode(region.toString(), cityName));					
				}
			}
			
			switch(region) {
			case RUSSIA :
				regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
						new TreasureRequirements(TreasureType.SILK, 2)
				}, 0)));
				regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
						new TreasureRequirements(TreasureType.IRON, 2)
				}, 0)));
				break;
			case CHINA :
				regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
						new TreasureRequirements(TreasureType.ANIMAL, 2)
				}, 0)));
				regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {}, 3)));
				break;
			case PERSIA :
				regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
						new TreasureRequirements(TreasureType.FUR, 2)
				}, 0)));
				regionNodes.add(new DoubleNode(region.toString(), new TradingPost(1, new TreasureRequirements[] {
						new TreasureRequirements(TreasureType.SPICE, 2)
				}, 0)));
				break;
			}

			nodeArray.add(regionNodes);
		}
		
		ArrayList<Node> centralCity = new ArrayList<Node>();
		centralCity.add(new CentralNode(alternativeKarakorum));
		nodeArray.add(centralCity);
		
		return;
	}
	
	
	private void connect(Region regionA, int nodeAInd, Region regionB, int nodeBInd) {
		Node a = nodeArray.get(regionA.ordinal()).get(nodeAInd);
		Node b = nodeArray.get(regionB.ordinal()).get(nodeBInd);
		
		a.addNeighbor(b);
		return;
	}
	
	
	private void connectCityNodes(Region region, int nodeAInd) {
		Node a = nodeArray.get(region.ordinal()).get(nodeAInd);
		Node b = nodeArray.get(region.ordinal()).get(nodeAInd + 1);
		Node c = nodeArray.get(region.ordinal()).get(nodeAInd + 2);
		
		a.addNeighbor(b);
		a.addNeighbor(c);
		b.addNeighbor(c);
	}
	

	private void initializeConnections() {		
		Node centralNode = nodeArray.get(3).get(0);
		
		for (Region region : Region.values()) {
			// Connect the 3 single nodes from each city
			for (int i = 0; i < 3; i++) {
				connectCityNodes(region, region.ordinal()*3 + i);
			}
			
			switch(region) {
			case CHINA:
				// Connection to the central node
				nodeArray.get(region.ordinal()).get(city1*3 + top).addNeighbor(centralNode);
				
				// Connections between cities from the same region
				connect(Region.CHINA, city1 + top, Region.CHINA, city2 + left);
				connect(Region.CHINA, city1 + right, Region.CHINA, city3 + top);
				
				connect(Region.CHINA, city1 + left, Region.CHINA, trade2);
				connect(Region.CHINA, city2 + left, Region.CHINA, trade1);
				connect(Region.CHINA, city3 + top, Region.CHINA, trade1);
				
				// Connections between Region
				connect(Region.CHINA, city2 + top, Region.RUSSIA, trade2);
				connect(Region.CHINA, trade2, Region.PERSIA, city2 + right);
				
				connect(Region.CHINA, city1 + left, Region.PERSIA, city2 + top);
				break;
			case RUSSIA:
				// Connection to the central node
				nodeArray.get(region.ordinal()).get(city1 + left).addNeighbor(centralNode);
				
				// Connections between cities from the same region
				connect(Region.RUSSIA, city1 + left, Region.RUSSIA, city2 + top);
				connect(Region.RUSSIA, city2 + left, Region.RUSSIA, city3 + top);
				
				connect(Region.RUSSIA, city3 + top, Region.RUSSIA, trade1);
				connect(Region.RUSSIA, city2 + top, Region.RUSSIA, trade1);
				connect(Region.RUSSIA, city1 + right, Region.RUSSIA, trade2);
				nodeArray.get(region.ordinal()).get(trade2).addNeighbor(centralNode);
				
				// Connections between Region
				connect(Region.RUSSIA, city3 + right, Region.PERSIA, trade1);
				connect(Region.RUSSIA, city2 + left, Region.PERSIA, trade1);
				connect(Region.RUSSIA, trade2, Region.CHINA, city2 + top);
				
				connect(Region.RUSSIA, city2 + right, Region.PERSIA, city1 + top);
				break;
			case PERSIA:
				// Connection to the central node
				nodeArray.get(region.ordinal()).get(city1 + right).addNeighbor(centralNode);
				
				// Connections between cities from the same region
				connect(Region.PERSIA, city1 + right, Region.PERSIA, city2 + top);
				connect(Region.PERSIA, city1 + left, Region.PERSIA, city3 + top);
				
				connect(Region.PERSIA, city1 + top, Region.PERSIA, trade1);
				connect(Region.PERSIA, city3 + top, Region.PERSIA, trade1);
				connect(Region.PERSIA, city3 + right, Region.PERSIA, trade2);
				connect(Region.PERSIA, city2 + top, Region.PERSIA, trade2);
				
				// Connections between Region
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
	
	
	/**
	 * 
	 * @param id what type is the province, following the enum
	 * @param nodes accept multiple parameters of node (1 or more)
	 */
	private void connectProvinceToNodes(ProvinceId id, Node... nodes) {
		for (Node node : nodes) {
			node.addProvince(provinces.get(id));
		}
	}
	
	
	private void connectNodeProvinces() {
		Node centralNode = nodeArray.get(3).get(0);
		final int RUSSIA_POS = Region.RUSSIA.ordinal();
		final int CHINA_POS = Region.CHINA.ordinal();
		final int PERSIA_POS = Region.PERSIA.ordinal();

		connectProvinceToNodes(ProvinceId.RUSSIA_YURT1, 
				nodeArray.get(RUSSIA_POS).get(city3 + top),
				nodeArray.get(RUSSIA_POS).get(city3 + right),
				nodeArray.get(RUSSIA_POS).get(city2 + left),
				nodeArray.get(PERSIA_POS).get(trade1)
		);
		
		connectProvinceToNodes(ProvinceId.RUSSIA_SWORD1, 
				nodeArray.get(RUSSIA_POS).get(city3 + top),
				nodeArray.get(RUSSIA_POS).get(city2 + left),
				nodeArray.get(RUSSIA_POS).get(city2 + top),
				nodeArray.get(RUSSIA_POS).get(trade1)
		);
		
		connectProvinceToNodes(ProvinceId.RUSSIA_YURT2, 
				nodeArray.get(RUSSIA_POS).get(city2 + top),
				nodeArray.get(RUSSIA_POS).get(city2 + right),
				nodeArray.get(RUSSIA_POS).get(city1 + left),
				nodeArray.get(PERSIA_POS).get(city1 + top),
				nodeArray.get(PERSIA_POS).get(city1 + right),
				centralNode
		);
		
		connectProvinceToNodes(ProvinceId.RUSSIA_COIN1, 
				nodeArray.get(RUSSIA_POS).get(city1 + left),
				nodeArray.get(RUSSIA_POS).get(city1 + right),
				nodeArray.get(RUSSIA_POS).get(trade2),
				centralNode
		);
		
		connectProvinceToNodes(ProvinceId.CHINA_SWORD1, 
				nodeArray.get(CHINA_POS).get(city1 + top),
				nodeArray.get(CHINA_POS).get(city2 + top),
				nodeArray.get(CHINA_POS).get(city2 + left),
				nodeArray.get(RUSSIA_POS).get(trade2),
				centralNode
		);
		
		connectProvinceToNodes(ProvinceId.CHINA_COIN1, 
				nodeArray.get(CHINA_POS).get(city2 + left),
				nodeArray.get(CHINA_POS).get(city1 + right),
				nodeArray.get(CHINA_POS).get(city1 + top),
				nodeArray.get(CHINA_POS).get(city3 + top),
				nodeArray.get(CHINA_POS).get(trade1)
		);
		
		connectProvinceToNodes(ProvinceId.CHINA_YURT1,
				nodeArray.get(CHINA_POS).get(city1 + right),
				nodeArray.get(CHINA_POS).get(city1 + left),
				nodeArray.get(CHINA_POS).get(city3 + top),
				nodeArray.get(CHINA_POS).get(city3 + left),
				nodeArray.get(CHINA_POS).get(trade2)
		);
		
		connectProvinceToNodes(ProvinceId.CHINA_SWORD2,
				nodeArray.get(CHINA_POS).get(city1 + left),
				nodeArray.get(CHINA_POS).get(trade2),
				nodeArray.get(PERSIA_POS).get(city2 + right),
				nodeArray.get(PERSIA_POS).get(city2 + top)
		);
		
		connectProvinceToNodes(ProvinceId.PERSIA_YURT1,
				nodeArray.get(CHINA_POS).get(city1 + top),
				nodeArray.get(CHINA_POS).get(city1 + left),
				nodeArray.get(PERSIA_POS).get(city1 + right),
				nodeArray.get(PERSIA_POS).get(city2 + top),
				centralNode
		);
		
		connectProvinceToNodes(ProvinceId.PERSIA_COIN1,
				nodeArray.get(PERSIA_POS).get(city1 + right),
				nodeArray.get(PERSIA_POS).get(city1 + left),
				nodeArray.get(PERSIA_POS).get(city2 + top),
				nodeArray.get(PERSIA_POS).get(city3 + right),
				nodeArray.get(PERSIA_POS).get(city3 + top),
				nodeArray.get(PERSIA_POS).get(trade2)
		);
		
		connectProvinceToNodes(ProvinceId.PERSIA_SWORD1,
				nodeArray.get(PERSIA_POS).get(city1 + top),
				nodeArray.get(PERSIA_POS).get(city1 + left),
				nodeArray.get(PERSIA_POS).get(city3 + top),
				nodeArray.get(PERSIA_POS).get(trade1)
		);
		
		connectProvinceToNodes(ProvinceId.PERSIA_COIN2,
				nodeArray.get(PERSIA_POS).get(city1 + top),
				nodeArray.get(PERSIA_POS).get(trade1),
				nodeArray.get(RUSSIA_POS).get(city2 + right),
				nodeArray.get(RUSSIA_POS).get(city2 + left)
		);
	}
	
	
	/**
	 * Checks if all city have been conquered
	 * @return {@code true} if all city have been dominated,
	 * else {@code false}
	 */
	public boolean allCityConquered() {
		for (City city : cityArray) {
			if (city.dominatedBy() == null) return false;
		}
		return true;
	}
	
	
	/**
	 * Defines a city as conquered/dominated
	 * @param name The name of the city conquered
	 * @param player The player who dominated the city
	 * @return {@code true} if was possible to sack from the city and
	 * it could define the player as the one who conquered. Otherwise, returns {@code false}
	 */
	public boolean setCityConquered(String name, Player player) {
		for (City city : cityArray) {
			if (city.getName().equals(name) && city.canBeSacked()) {
				city.defineDomination(player);
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns the length of the adjacency matrix for the nodes
	 * @return length of the adjacency matrix ({@code int})
	 */
	public int getLengthNodeArray() {
		return nodeArray.size();
	}
	
	
	/**
	 * <p>Returns how many nodes there are per region.</p>
	 * <p>Obs: All regions have the same amount of nodes, <b>except</b> for the central node.</p>
	 * @return {@code int} for the amount of nodes per region
	 */
	public int getNodePerRegion() {
		return nodeArray.get(0).size();
	}
	
	
	/**
	 * Get how many cities there are in the game
	 * @return {@code int} amount of cities in the board, excluding the central node
	 */
	public int getAmountCity() {
		return cityArray.size();
	}
	
	
	/**
	 * Get how many provinces there are in the map
	 * @return {@code int} amount of provinces
	 */
	public int getAmountProvinces() {
		return provinces.size();
	}
	
	
	/**
	 * Creates a copy of the Provinces that can only be read
	 * @return {@code List<Province>}
	 */
	List<Province> getProvinces() {
		return Collections.unmodifiableList(
				new ArrayList<>(provinces.values())
		);
	}
	
	
	/**
	 * Creates a read only copy for the cities
	 * @return {@code List<City>} that is read-only
	 */
	List<City> createCityCopies(){
		return Collections.unmodifiableList(cityArray);
	}
}
