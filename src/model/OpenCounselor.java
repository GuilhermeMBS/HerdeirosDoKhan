package model;


import java.util.*;


class OpenCounselor extends Card {
	private static ArrayList<OpenCounselor> cardsInGame = null;
	protected static ArrayList<OpenCounselor> cards = new ArrayList<OpenCounselor>();
	
	
	public OpenCounselor(int qtd, OpenMissionType type) {
		m1 = new Mission(qtd, type);
		m2 = m3 = null;
		p = null;
	}
	
	
	static {
		// Don't know
		cards.add(new OpenCounselor(4, OpenMissionType.SWORD));
		
		// Have n yurts
		cards.add(new OpenCounselor(6, OpenMissionType.YURT));
		cards.add(new OpenCounselor(10, OpenMissionType.YURT));
		
		// Have n yurts in each region
		cards.add(new OpenCounselor(1, OpenMissionType.YURTREGION));
		cards.add(new OpenCounselor(2, OpenMissionType.YURTREGION));
		
		// Treasures
		cards.add(new OpenCounselor(4, OpenMissionType.TREASURE));
		cards.add(new OpenCounselor(1, OpenMissionType.EACHRESOURCE));
		
		// Have n points
		cards.add(new OpenCounselor(2, OpenMissionType.POINTS));
		cards.add(new OpenCounselor(3, OpenMissionType.POINTS));
		
		// Have n city conquered
		cards.add(new OpenCounselor(2, OpenMissionType.CITIESDOMINATED));
		cards.add(new OpenCounselor(3, OpenMissionType.CITIESDOMINATED));
		
		// Have 1 line of power ups
		cards.add(new OpenCounselor(1, OpenMissionType.POWERUPS_LINE));
		cards.add(new OpenCounselor(1, OpenMissionType.POWERUPS_COL));
		cards.add(new OpenCounselor(1, OpenMissionType.POWERUPS_SQUARE));
		
		Collections.shuffle(cards);
	}
	
	
	private static boolean createFourCards() {
		cardsInGame = new ArrayList<OpenCounselor>();
		for (int i = 0; i < 4; i++)
			cardsInGame.add(cards.remove(0));
		return true;
	}
	
	
	public static ArrayList<OpenCounselor> giveCardsInGame() {
		if (cardsInGame == null) createFourCards();
		return cardsInGame;
	}
	
	
	public boolean playerHasCompleted(Player p) {
		if (this.p != null) return false;
		this.p = p;
		m1.setFinished();
		
		return true;
	}
	
	
	public int gainPoints() {
		return hasBeenCompleted() ? 1: 0;
	}
	
	
	@Override
	protected boolean hasBeenCompleted() {
		return p != null;
	}
}
