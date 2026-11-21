package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

enum BonusCard {
	VOTE,
	SWORDS,
	COINS,
	YURTS,
	MOVEMENT,
	HANDS,
	TREASURES;
	
	private boolean hasBeenUsed;
	private Player player;
	
	BonusCard() {
		hasBeenUsed = false;
		player = null;
	}
	
	public String getDisplayName() { return name();	}
	public boolean hasBeenUsed() { return hasBeenUsed == true; }
	public boolean hasBeenClaimed() { return player != null; }
	
	public boolean useBonus() {
		if (hasBeenUsed == true) return false;
		hasBeenUsed = true;
		return true;
	}
	
	public boolean assignPlayer(Player p) {
		if (player != null) return false;
		player = p;
		return true;
	}
	
	private static ArrayList<String> bonusCardsName = new ArrayList<>();
	static {
		for (BonusCard card : BonusCard.values()) bonusCardsName.add(card.toString());
	}
	
	public static ArrayList<String> getCardsName() { return bonusCardsName; }
	
	private static HashMap<String, String> imgFilePath = new HashMap<>();
	
	public static HashMap<String, String> getImagePath() {
		if (imgFilePath.isEmpty()) {
			String base = "assets/bonusCards/";
			for (String card : bonusCardsName)imgFilePath.put(card, base + card + ".jpg");
		}
		
		return imgFilePath;
	}
	
	private static final ArrayList<BonusCard> cards = new ArrayList<>();
	
	public static void resetDeck() {
		cards.clear();
		for (BonusCard card : BonusCard.values()) cards.add(card);
		Collections.shuffle(cards);
	}
	
	static { resetDeck(); }
	
	public static BonusCard getOneCard() { 
		return cards.remove(0); 
	}
}
