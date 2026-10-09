package model;

import java.util.*;


/**
 * Represents a city in the game
 * 
 * <p>A city can be sacked by a player to obtain resources. Once all resources
 * have been removed, the city will be dominated by the player. </p>
 * 
 * <p>Available methods:</p>
 * <ul>
 * 		<li>{@link #activateSacking()}</li>
 * 		<li>{@link #removeResource()}</li>
 * 		<li>{@link #dominatedBy()}</li>
 * 		<li>{@link #getName()}</li>
 * </ul>
 * 
 * @author joaop
 * @version 1.0
 */
class Cidade {
	private String name;
	private boolean saqueavel = false;
	private Jogador player = null;
	private ArrayList<ResourceType> listaRecursos = new ArrayList<ResourceType>();
	
	
	/**
	 * Constructor for the class "Cidade".
	 * 
	 * @param name the name of the city
	 */
	public Cidade(String name) {
		this.name = name;
	}
	
	
	/**
	 * Makes it possible so the player can sack the city and add the resources for it
	 * @return {@code true} if it could make the city available for sacking
	 */
	public boolean activateSacking() {
		saqueavel = true;
		listaRecursos = Resource.generateResources();
		
		return true;
	}
	
	
	/**
	 * Removes a resource from a city, if available, and returns it.
	 * 
	 * @param ind position of the resource that the player wants
	 * @return the removed resource, or {@code null} if the city can not
	 * be sacked
	 */
	public ResourceType removeResource(int ind) {
		if (!saqueavel || listaRecursos.isEmpty()) return null;
		
		ResourceType r = listaRecursos.get(ind);
		listaRecursos.remove(ind);
		return r;
	}
	
	
	/**
	 * Attach the city to a player which collected the last resource
	 * @param num the reference to which player conquered the city
	 * @return {@code true} if it could assign the player as the conquerer
	 */
	public boolean defineDomination(Jogador num) {
		if (!(saqueavel && listaRecursos.isEmpty())) return false;
		
		player = num;
		saqueavel = false;
		
		return true;
	}
	
	
	/**
	 * Get which player conquered the city
	 * @return {@code null} if the city has not been conquered, 
	 * else {@code Jogador} that has dominated the city
	 */
	public Jogador dominatedBy() {
		return player;
	}	
	
	
	/**
	 * Get the name of the city
	 * @return the name ({@code String}) of the city
	 */
	public String getName() {
		return name;
	}
}
