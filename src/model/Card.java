package model;


abstract class Card {
	protected Mission m1;
	protected Mission m2;
	protected Mission m3;
	protected Player p;
	
	abstract protected boolean hasBeenCompleted();
}
