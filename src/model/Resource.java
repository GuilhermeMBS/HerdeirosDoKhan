package model;

import java.util.*;


/**
 * Enum to represent the multiple types of resources in the game
 */
enum ResourceType {
	IRON,
	TREASURE,
	WOOL,
	SILK,
	GRAIN
}


/**
 * Is a collection of resources of the game
 * 
 * <p>Once this file is loaded for the first onto the virtual machine, it
 *  creates all the resources and shuffles them.</p>
 *  
 * <p>Available methods:</p>
 * <ul>
 * 		<li>{@link #generateResources()}</li>
 * </ul>
 * 
 * @author joaop
 * @version 1.0
 */
class Resource {
	private static ArrayList<ResourceType> resources = new ArrayList<ResourceType>();
	private static final int QTY_PER_RESOURCE = 8; // I understood that there are these many resources for each type
	
	
	/**
	 * Static creation of the array for all resources
	 */
	static {
		for (ResourceType tipo : ResourceType.values()) {
			for (int i = 0; i < QTY_PER_RESOURCE; i++) {
				resources.add(tipo);
			}
		}
		
		Collections.shuffle(resources);
	}
	
	
	/**
	 * Creates the 4 resources for the city to be sacked
	 * @return {@code null} if all resources have been consumed,
	 * or an {@code ArrayList<ResourceType>} with the 4 resources of the city
	 */
	public static ArrayList<ResourceType> generateResources() {
		if (resources.isEmpty()) return null;
		
		ArrayList<ResourceType> tmp = new ArrayList<ResourceType>();
		for (int i = 0; i < 4; i++) {
			tmp.add(resources.remove(0));
		}
		
		return tmp;
	}
}
