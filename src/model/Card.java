package model;


abstract class Card {
	protected Mission m1;
	protected Mission m2;
	protected Mission m3;
	protected Player p;
	
	
	/**
	 * Calculates how many points this Counselor gives to the player
	 * @return The amount of points that the player will receive from this card ({@code int})
	 */
	abstract public int gainPoints();
}
