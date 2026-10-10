package model;


import java.util.*;


class OpenCounselor extends Card {
	public OpenCounselor(int qtd, MissionType type) {
		m1 = new Mission(qtd, type, 1);
		m2 = m3 = null;
		p = null;
		cards = new ArrayList<Card>();
	}
	
	
	static {
		// Don't know
		cards.add(new OpenCounselor(4, MissionType.SWORD));
		
		// Have n yurts
		cards.add(new OpenCounselor(6, MissionType.YURT));
		cards.add(new OpenCounselor(10, MissionType.YURT));
		
		// Have n yurts in each region
		cards.add(new OpenCounselor(1, MissionType.YURTREGION));
		cards.add(new OpenCounselor(2, MissionType.YURTREGION));
		
		// Treasures
		cards.add(new OpenCounselor(4, MissionType.TREASURE));
		cards.add(new OpenCounselor(1, MissionType.EACHRESOURCE));
		
		// Have n points
		cards.add(new OpenCounselor(2, MissionType.POINTS));
		cards.add(new OpenCounselor(3, MissionType.POINTS));
		
		// Have n city conquered
		cards.add(new OpenCounselor(2, MissionType.CITIESDOMINATED));
		cards.add(new OpenCounselor(3, MissionType.CITIESDOMINATED));
		
		// Have 1 line of power ups
		cards.add(new OpenCounselor(1, MissionType.POWERUPS_LINE));
		cards.add(new OpenCounselor(1, MissionType.POWERUPS_COL));
		cards.add(new OpenCounselor(1, MissionType.POWERUPS_SQUARE));
		
		Collections.shuffle(cards);
	}
	
	
	public static ArrayList<OpenCounselor> giveFourCards() {
		ArrayList<OpenCounselor> cardsInGame = new ArrayList<OpenCounselor>();
		for (int i = 0; i < 4; i++)
			cardsInGame.add((OpenCounselor) cards.remove(0));
		return cardsInGame;
	}
	
	
	public boolean playerHasCompleted(Player p) {
		if (this.p != null) return false;
		this.p = p;
		return true;
	}
	
	
	@Override
	public boolean hasBeenCompleted() {
		return p == null;
	}
}
