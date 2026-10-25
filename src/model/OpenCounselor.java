package model;


import java.util.*;


enum OpenMissionType implements MissionType {
	YURT,
	YURTREGION,
	SWORD,
	EACHRESOURCE,
	TREASURE,
	CITIESDOMINATED,
	POWERUPS_LINE,
	POWERUPS_COL,
	POWERUPS_SQUARE,
	POINTS
}



/**
 * Creates all instances of Open Counselor automatically
 * 
 * <p>Available methods:</p>
 * <ul>
 * 		<li>{@link #giveCardsInGame()}</li>
 * 		<li>{@link #playerHasCompleted(Player p)}</li>
 * 		<li>{@link #gainPoints()}</li>
 * </ul>
 * 
 * @author joaop
 * @version 1.0
 */
class OpenCounselor extends Card {
	private static ArrayList<OpenCounselor> cardsInGame = null;
	private static ArrayList<OpenCounselor> cards = new ArrayList<OpenCounselor>();
	
	
	/**
	 * 
	 * @param qtd the amount necessary to complete the mission
	 * @param type type of mission to be done
	 */
	private OpenCounselor(int qtd, OpenMissionType type) {
		m1 = new Mission(qtd, type);
		m2 = m3 = null;
		p = null;
	}
	
	
	/**
	 * Creates all instances of Open Counselors
	 * when it loads for the first time into the 
	 * java virtual machine
	 */
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
	
	
	/**
	 * Creates all instances of Open Counselors
	 * when it loads for the first time into the 
	 * java virtual machine
	 */
	private static boolean createFourCards() {
		cardsInGame = new ArrayList<OpenCounselor>();
		for (int i = 0; i < 4; i++)
			cardsInGame.add(cards.remove(0));
		return true;
	}
	
	
	/**
	 * Creates the 4 cards that will be in the game, 
	 * if they were not created up until that point
	 * 
	 * @return {@code ArrayList<OpenCounselor>} with 4 elements that
	 * represents all cards that will be in the game
	 */
	public static ArrayList<OpenCounselor> giveCardsInGame() {
		if (cardsInGame == null) createFourCards();
		return cardsInGame;
	}
	
	
	/**
	 * Sets a player as the one who has completed that card
	 * @param p the Player who has finished the mission
	 * @return {@code true} if the card was not already assigned to a player,
	 * else {@code false} if there were already a player assigned to the card.
	 */
	public boolean playerHasCompleted(Player p) {
		if (this.p != null) return false;
		this.p = p;
		m1.setFinished();
		
		return true;
	}
	
	@Override
	public int gainPoints() {
		return hasBeenCompleted() ? 1: 0;
	}
	
	
	private boolean hasBeenCompleted() {
		return p != null;
	}
	
	
	public OpenMissionType getMissionType() {
		return (OpenMissionType) m1.getMissionType();
	}
	
	
	public int getMissionTarget() {
		return m1.getTargetAmount();
	}
}
