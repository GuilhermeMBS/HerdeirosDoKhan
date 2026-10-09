package Model;

import java.util.*;


enum nodeType {
	SINGLE,
	DOUBLE,
	CENTRAL
}


enum regions {
	RUSSIA,
	CHINA,
	PERSIA
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
	private ArrayList<ArrayList<Node>> nodeArray = new ArrayList<ArrayList<Node>>();
	
	
	/**
	 * When creating an instance of this variable,
	 * it creates all the city and nodes for the map
	 */
	public Mapa() {
		// Chinese region (red)
		createCities();
		initializeNodes();
	}
	
	
	private void createCities() {
		cityArray.add(new Cidade("Cantão"));
		cityArray.add(new Cidade("Pequim"));
		cityArray.add(new Cidade("Kaesong"));
		
		// Russian region (yellow)
		cityArray.add(new Cidade("Moscou"));
		cityArray.add(new Cidade("Kiev"));
		cityArray.add(new Cidade("Sarai"));
		
		// Persian region (green)
		cityArray.add(new Cidade("Cabul"));
		cityArray.add(new Cidade("Bagdá"));
		cityArray.add(new Cidade("Samarcanda"));
		
		return;
	}
	
	
	private void initializeNodes() {
		nodeType[] layoutRegion = {
				nodeType.SINGLE,
				nodeType.SINGLE,
				nodeType.SINGLE,
				nodeType.SINGLE,
				nodeType.SINGLE,
				nodeType.SINGLE,
				nodeType.SINGLE,
				nodeType.SINGLE,
				nodeType.SINGLE,
				nodeType.DOUBLE,
				nodeType.DOUBLE,
		};
		
		for (regions region : regions.values()) {
			// Nodes per region
			ArrayList<Node> regionNodes = new ArrayList<Node>();
		
			for (nodeType type : layoutRegion) {
				switch(type) {
					case SINGLE:
						regionNodes.add(new SingleNode());
						break;
					case DOUBLE:
						regionNodes.add(new DoubleNode());
						break;
					case CENTRAL:
						break;
				}
			}
			nodeArray.add(regionNodes);
		}
		
		ArrayList<Node> centraCity = new ArrayList<Node>();
		centraCity.add(new CentralNode());
		nodeArray.add(centraCity);
		
		return;
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
	
	
	public boolean setCityConquered(String name, Jogador player) {
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
}
