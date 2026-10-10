package Model;

import java.util.*;


/**
 * Must keep this order, or change the order in 
 * {@link #cityNames}
 */
enum regions {
	CHINA,
	RUSSIA,
	PERSIA
}


enum posNode {
	TOP,
	RIGHT,
	LEFT
}


/**
 * Is a collection of resources of the game
 * 
 * <p>Once this file is loaded for the first onto the virtual machine, it
 *  creates all the resources and shuffles them.</p>
 *  
 * <p>Available methods:</p>
 * <ul>
 * 		<li></li>
 * </ul>
 * 
 * @author joaop
 * @version 1.0
 */
public class Mapa {
	
	private ArrayList<Cidade> cityArray = new ArrayList<Cidade>();
	
	// The order for the nodes will be clockwise, starting from the top (for each city)
	private ArrayList<ArrayList<Node>> nodeArray = new ArrayList<ArrayList<Node>>();
	
	private final String[][] cityNames = {
			{
				"Pequim",
				"Kaesong",
				"Cantão"
			},
			{
				"Sarai",
				"Moscou",
				"Kiev"
			},
			{
				"Samarcanda",
				"Cabul",
				"Bagdá"
			}
	};
	
	
	/**
	 * When creating an instance of this variable,
	 * it creates all the city and nodes for the map
	 */
	public Mapa() {
		createCities();
		initializeNodes();
		initializeConnections();
	}
	
	
	/**
	 * The cities will be created following clockwise order
	 * starting from connection to the central node
	 */
	private void createCities() {
		for (regions region : regions.values()) {
			for (int i = 0; i < 3; i++) {
				cityArray.add(new Cidade(cityNames[region.ordinal()][i]));
			}
		}
		
		return;
	}
	
	
	private void initializeNodes() {
		for (regions region : regions.values()) {
			// Nodes per region
			ArrayList<Node> regionNodes = new ArrayList<Node>();
			
			for (int i = 0; i < 3; i++) {
				String cityName = cityArray.get(region.ordinal()*3 + i).getName();
				for (int j = 0; j < 3; j++) {
					regionNodes.add(new SingleNode(region.toString(), cityName));					
				}
			}
			
			for (int i = 0; i < 2; i++) {
				regionNodes.add(new DoubleNode(region.toString(), new TradingPost()));
			}

			nodeArray.add(regionNodes);
		}
		
		ArrayList<Node> centralCity = new ArrayList<Node>();
		centralCity.add(new CentralNode());
		nodeArray.add(centralCity);
		
		return;
	}
	
	
	private void connect(regions regionA, int nodeAInd, regions regionB, int nodeBInd) {
		Node a = nodeArray.get(regionA.ordinal()).get(nodeAInd);
		Node b = nodeArray.get(regionB.ordinal()).get(nodeBInd);
		
		a.addNeighbor(b);
		return;
	}
	
	
	private void connectCityNodes(regions region, int nodeAInd) {
		Node a = nodeArray.get(region.ordinal()).get(nodeAInd);
		Node b = nodeArray.get(region.ordinal()).get(nodeAInd + 1);
		Node c = nodeArray.get(region.ordinal()).get(nodeAInd + 2);
		
		a.addNeighbor(b);
		a.addNeighbor(c);
		b.addNeighbor(c);
	}
	

	private void initializeConnections() {		
		Node centralNode = nodeArray.get(3).get(0);
		
		int top = posNode.TOP.ordinal();
		int right = posNode.RIGHT.ordinal();
		int left = posNode.LEFT.ordinal();
		
		int city1 = 0;
		int city2 = 1;
		int city3 = 2;
		
		// Positions for the trade nodes in the array
		int trade1 = (city3 + 1)*3;
		int trade2 = trade1 + 1;
		
		for (regions region : regions.values()) {
			// Connect the 3 single nodes from each city
			for (int i = 0; i < 3; i++) {
				connectCityNodes(region, region.ordinal()*3 + i);
			}
			
			switch(region) {
			case CHINA:
				// Connection to the central node
				nodeArray.get(region.ordinal()).get(city1*3 + top).addNeighbor(centralNode);
				
				// Connections between cities from the same region
				connect(regions.CHINA, city1*3 + top, regions.CHINA, city2*3 + left);
				connect(regions.CHINA, city1*3 + right, regions.CHINA, city3*3 + top);
				
				connect(regions.CHINA, city1*3 + left, regions.CHINA, trade2);
				connect(regions.CHINA, city2*3 + left, regions.CHINA, trade1);
				connect(regions.CHINA, city3*3 + top, regions.CHINA, trade1);
				
				// Connections between regions
				connect(regions.CHINA, city2*3 + top, regions.RUSSIA, trade2);
				connect(regions.CHINA, trade2, regions.PERSIA, city2*3 + right);
				
				connect(regions.CHINA, city1*3 + left, regions.PERSIA, city2*3 + top);
				break;
			case RUSSIA:
				// Connection to the central node
				nodeArray.get(region.ordinal()).get(city1*3 + left).addNeighbor(centralNode);
				
				// Connections between cities from the same region
				connect(regions.RUSSIA, city1*3 + left, regions.RUSSIA, city2*3 + top);
				connect(regions.RUSSIA, city2*3 + left, regions.RUSSIA, city3*3 + top);
				
				connect(regions.RUSSIA, city3*3 + top, regions.RUSSIA, trade1);
				connect(regions.RUSSIA, city2*3 + top, regions.RUSSIA, trade1);
				connect(regions.RUSSIA, city1*3 + right, regions.RUSSIA, trade2);
				nodeArray.get(region.ordinal()).get(trade2).addNeighbor(centralNode);
				
				// Connections between regions
				connect(regions.RUSSIA, city3*3+right, regions.PERSIA, trade1);
				connect(regions.RUSSIA, city2*3+left, regions.PERSIA, trade1);
				connect(regions.RUSSIA, trade2, regions.CHINA, city2*3 + top);
				
				connect(regions.RUSSIA, city2*3 + right, regions.PERSIA, city1*3 + top);
				break;
			case PERSIA:
				// Connection to the central node
				nodeArray.get(region.ordinal()).get(city1*3 + right).addNeighbor(centralNode);
				
				// Connections between cities from the same region
				connect(regions.PERSIA, city1*3 + right, regions.PERSIA, city2*3 + top);
				connect(regions.PERSIA, city1*3 + left, regions.PERSIA, city3*3 + top);
				
				connect(regions.PERSIA, city1*3 + top, regions.PERSIA, trade1);
				connect(regions.PERSIA, city3*3 + top, regions.PERSIA, trade1);
				connect(regions.PERSIA, city3*3 + right, regions.PERSIA, trade2);
				connect(regions.PERSIA, city2*3 + top, regions.PERSIA, trade2);
				
				// Connections between regions
				connect(regions.PERSIA, city1*3 + top, regions.RUSSIA, city2*3 + right);
				connect(regions.PERSIA, city2*3 + top, regions.CHINA, city1*3 + left);
				
				connect(regions.PERSIA, city2*3 + right, regions.CHINA, trade2);
				connect(regions.PERSIA, trade1, regions.RUSSIA, city3*3 + right);
				connect(regions.PERSIA, trade1, regions.RUSSIA, city2*3 + left);
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
		for (Cidade city : cityArray) {
			if (city.dominatedBy() == null) return false;
		}
		return true;
	}
	
	
	public boolean setCityConquered(String name, Player player) {
		for (Cidade city : cityArray) {
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
	
	
	public ArrayList<Cidade> createCityCopies(){
		return new ArrayList<Cidade>(cityArray);
	}
	
	
	public String[][] getNameOrder() {
		return cityNames;
	}
}
