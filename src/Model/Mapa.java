package Model;

import java.util.*;


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
	private ArrayList<Node> listaNo;
	
	/**
	 * When creating an instance of this variable,
	 * it creates all the city and nodes for the map
	 */
	public Mapa() {
		// Chinese region (red)
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
}
