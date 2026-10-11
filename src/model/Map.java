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


/**
 * 
 * 
 * <p>  </p>
 *  
 * <p>Available methods:</p>
 * <ul>
 * 		<li>{@link #allCityConquered()}</li>
 * </ul>
 * 
 * @author joaop
 * @version 1.0
 */
public class Map {
	
	private ArrayList<City> cityArray = new ArrayList<City>();
	
	// The order for the nodes will be clockwise, starting from the top (for each city)
	private ArrayList<ArrayList<Node>> nodeArray = new ArrayList<ArrayList<Node>>();
	
	private EnumMap<Region, String[]> cityNames = new EnumMap<>(Region.class);
	
	
	/**
	 * When creating an instance of this variable,
	 * it creates all the city and nodes for the map
	 */
	public Map(boolean alternativeKarakorum) {
		createCities();
		initializeNodes(alternativeKarakorum);
		initializeConnections();
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
		
		for (Region region : Region.values()) {
			String[] names = cityNames.get(region);
			for (int i = 0; i < 3; i++) {
				cityArray.add(new City(names[i]));
			}
		}
		
		return;
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
		
		int top = NodePosition.TOP.ordinal();
		int right = NodePosition.RIGHT.ordinal();
		int left = NodePosition.LEFT.ordinal();
		
		int city1 = 0;
		int city2 = 1;
		int city3 = 2;
		
		// Positions for the trade nodes in the array
		int trade1 = (city3 + 1)*3;
		int trade2 = trade1 + 1;
		
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
				connect(Region.CHINA, city1*3 + top, Region.CHINA, city2*3 + left);
				connect(Region.CHINA, city1*3 + right, Region.CHINA, city3*3 + top);
				
				connect(Region.CHINA, city1*3 + left, Region.CHINA, trade2);
				connect(Region.CHINA, city2*3 + left, Region.CHINA, trade1);
				connect(Region.CHINA, city3*3 + top, Region.CHINA, trade1);
				
				// Connections between Region
				connect(Region.CHINA, city2*3 + top, Region.RUSSIA, trade2);
				connect(Region.CHINA, trade2, Region.PERSIA, city2*3 + right);
				
				connect(Region.CHINA, city1*3 + left, Region.PERSIA, city2*3 + top);
				break;
			case RUSSIA:
				// Connection to the central node
				nodeArray.get(region.ordinal()).get(city1*3 + left).addNeighbor(centralNode);
				
				// Connections between cities from the same region
				connect(Region.RUSSIA, city1*3 + left, Region.RUSSIA, city2*3 + top);
				connect(Region.RUSSIA, city2*3 + left, Region.RUSSIA, city3*3 + top);
				
				connect(Region.RUSSIA, city3*3 + top, Region.RUSSIA, trade1);
				connect(Region.RUSSIA, city2*3 + top, Region.RUSSIA, trade1);
				connect(Region.RUSSIA, city1*3 + right, Region.RUSSIA, trade2);
				nodeArray.get(region.ordinal()).get(trade2).addNeighbor(centralNode);
				
				// Connections between Region
				connect(Region.RUSSIA, city3*3+right, Region.PERSIA, trade1);
				connect(Region.RUSSIA, city2*3+left, Region.PERSIA, trade1);
				connect(Region.RUSSIA, trade2, Region.CHINA, city2*3 + top);
				
				connect(Region.RUSSIA, city2*3 + right, Region.PERSIA, city1*3 + top);
				break;
			case PERSIA:
				// Connection to the central node
				nodeArray.get(region.ordinal()).get(city1*3 + right).addNeighbor(centralNode);
				
				// Connections between cities from the same region
				connect(Region.PERSIA, city1*3 + right, Region.PERSIA, city2*3 + top);
				connect(Region.PERSIA, city1*3 + left, Region.PERSIA, city3*3 + top);
				
				connect(Region.PERSIA, city1*3 + top, Region.PERSIA, trade1);
				connect(Region.PERSIA, city3*3 + top, Region.PERSIA, trade1);
				connect(Region.PERSIA, city3*3 + right, Region.PERSIA, trade2);
				connect(Region.PERSIA, city2*3 + top, Region.PERSIA, trade2);
				
				// Connections between Region
				connect(Region.PERSIA, city1*3 + top, Region.RUSSIA, city2*3 + right);
				connect(Region.PERSIA, city2*3 + top, Region.CHINA, city1*3 + left);
				
				connect(Region.PERSIA, city2*3 + right, Region.CHINA, trade2);
				connect(Region.PERSIA, trade1, Region.RUSSIA, city3*3 + right);
				connect(Region.PERSIA, trade1, Region.RUSSIA, city2*3 + left);
				break;
			}
		}
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
	
	
	public boolean setCityConquered(String name, Player player) {
		for (City city : cityArray) {
			if (city.getName().equals(name)) {
				city.defineDomination(player);
				return true;
			}
		}
		return false;
	}
	
	
	public int getLengthNodeArray() {
		return nodeArray.size();
	}
	
	
	public int getNodePerRegion() {
		return nodeArray.get(0).size();
	}
	
	
	public int getAmountCity() {
		return cityArray.size();
	}
	
	
	public ArrayList<City> createCityCopies(){
		return new ArrayList<City>(cityArray);
	}
}
