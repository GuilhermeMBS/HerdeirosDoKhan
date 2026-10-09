package Model;

import java.util.*;


/**
 * Enum to represent the multiple types of resources in the game
 */
enum resourceType {
	iron,
	treasure,
	wool,
	silk,
	grain
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
class Recursos {
	private static ArrayList<resourceType> resources = new ArrayList<resourceType>();
	private static final int QTY_PER_RESOURCE = 8; // I understood that there are these many resources for each type
	
	
	/**
	 * Static creation of the array for all resources
	 */
	static {
		for (resourceType tipo : resourceType.values()) {
			for (int i = 0; i < QTY_PER_RESOURCE; i++) {
				resources.add(tipo);
			}
		}
		
		Collections.shuffle(resources);
	}
	
	
	/**
	 * Creates the 4 resources for the city to be sacked
	 * @return {@code null} if all resources have been consumed,
	 * or an {@code ArrayList<resourceType>} with the 4 resources of the city
	 */
	public static ArrayList<resourceType> generateResources() {
		if (resources.isEmpty()) return null;
		
		ArrayList<resourceType> tmp = new ArrayList<resourceType>();
		for (int i = 0; i < 4; i++) {
			tmp.add(resources.remove(0));
		}
		
		return tmp;
	}
}
