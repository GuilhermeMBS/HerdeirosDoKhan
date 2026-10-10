package Model;


import java.util.*;


abstract class Card {
	protected Mission m1;
	protected Mission m2;
	protected Mission m3;
	protected Player p;
	protected static ArrayList<Card> cards;
	protected static ArrayList<Card> cardsInGame;
	
	abstract boolean hasBeenCompleted();
}
