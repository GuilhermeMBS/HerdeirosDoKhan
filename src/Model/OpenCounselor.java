package Model;


import java.util.*;


class OpenCounselor extends Card {
	public OpenCounselor(int qtd, missionType type) {
		m1 = new Mission(qtd, type, 1);
		m2 = m3 = null;
		p = null;
		cards = new ArrayList<Card>();
	}
	
	
	static {
		// Don't know
		cards.add(new OpenCounselor(4, missionType.SWORD));
		
		// Have n yurts
		cards.add(new OpenCounselor(6, missionType.YURT));
		cards.add(new OpenCounselor(10, missionType.YURT));
		
		// Have n yurts in each region
		cards.add(new OpenCounselor(1, missionType.YURTREGION));
		cards.add(new OpenCounselor(2, missionType.YURTREGION));
		
		// Treasures
		cards.add(new OpenCounselor(4, missionType.TREASURE));
		cards.add(new OpenCounselor(1, missionType.EACHRESOURCE));
		
		// Have n points
		cards.add(new OpenCounselor(2, missionType.POINTS));
		cards.add(new OpenCounselor(3, missionType.POINTS));
		
		// Have n city conquered
		cards.add(new OpenCounselor(2, missionType.CITIESDOMINATED));
		cards.add(new OpenCounselor(3, missionType.CITIESDOMINATED));
		
		// Have 1 line of power ups
		cards.add(new OpenCounselor(1, missionType.POWERUPS_LINE));
		cards.add(new OpenCounselor(1, missionType.POWERUPS_COL));
		cards.add(new OpenCounselor(1, missionType.POWERUPS_SQUARE));
		
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
