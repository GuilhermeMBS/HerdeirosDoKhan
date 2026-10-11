package model;


import java.util.*;


enum PrivateMissionType implements MissionType {
	POWERUPS_DIFF,
	POWERUPS_SAME,
	YURTINSEQUENCE,
	POWERUPS_QTD,
	CITY_YURT,
	POWERUPS_HORSE_HAND,
	YURT_TRADINGPOST,
	POWERUPS_YURT,
	YURT_KHAN,
	CITY_2YURT
}


/**
 * Creates all instances of Private Counselor automatically
 * 
 * <p>Available methods:</p>
 * <ul>
 * 		<li>{@link #giveTwoCards()}</li>
 * 		<li>{@link #setFinished(Mission m)}</li>
 * 		<li>{@link #gainPoints()}</li>
 * 		<li>{@link #getName()}</li>
 * </ul>
 * 
 * @author joaop
 * @version 1.0
 */
class PrivateCounselor extends Card {
	private String name;
	private static ArrayList<PrivateCounselor> cards = new ArrayList<PrivateCounselor>();
	
	/**
	 * 
	 * @param m1 mission 1
	 * @param m2 mission 2
	 * @param m3 mission 3
	 * @param name name of the card
	 */
	public PrivateCounselor(Mission m1, Mission m2, Mission m3, String name) {
		this.m1 = m1;
		this.m2 = m2;
		this.m3 = m3;
		this.name = name;
		this.p = null;
	}
	
	
	/**
	 * Creates all instances of Private Counselors
	 * when it loads for the first time into the 
	 * java virtual machine
	 */
	static {
		// Code
		cards.add(new PrivateCounselor(
				new Mission(1, PrivateMissionType.POWERUPS_SAME), new Mission(2, PrivateMissionType.POWERUPS_SAME), 
				new Mission(4, PrivateMissionType.POWERUPS_SAME), "Sacerdote"
		));
		
		cards.add(new PrivateCounselor(
				new Mission(4, PrivateMissionType.POWERUPS_DIFF), new Mission(6, PrivateMissionType.POWERUPS_DIFF), 
				new Mission(7, PrivateMissionType.POWERUPS_DIFF), "Colecionador"
		));
		
		cards.add(new PrivateCounselor(
				new Mission(3, PrivateMissionType.YURTINSEQUENCE), new Mission(6, PrivateMissionType.YURTINSEQUENCE), 
				new Mission(8, PrivateMissionType.YURTINSEQUENCE), "Viajante"
		));
		
		cards.add(new PrivateCounselor(
				new Mission(4, PrivateMissionType.POWERUPS_QTD), new Mission(7, PrivateMissionType.POWERUPS_QTD), 
				new Mission(10, PrivateMissionType.POWERUPS_QTD), "Soldado"
		));
		
		cards.add(new PrivateCounselor(
				new Mission(3, PrivateMissionType.CITY_YURT), new Mission(6, PrivateMissionType.CITY_YURT), 
				new Mission(8, PrivateMissionType.CITY_YURT), "Cidadão"
		));
		
		cards.add(new PrivateCounselor(
				new Mission(2, PrivateMissionType.POWERUPS_HORSE_HAND), new Mission(4, PrivateMissionType.POWERUPS_HORSE_HAND), 
				new Mission(5, PrivateMissionType.POWERUPS_HORSE_HAND), "Cavaleiro"
		));
		
		cards.add(new PrivateCounselor(
				new Mission(2, PrivateMissionType.YURT_TRADINGPOST), new Mission(4, PrivateMissionType.YURT_TRADINGPOST), 
				new Mission(6, PrivateMissionType.YURT_TRADINGPOST), "Mensageiro"
		));
		
		cards.add(new PrivateCounselor(
				new Mission(1, PrivateMissionType.POWERUPS_YURT), new Mission(2, PrivateMissionType.POWERUPS_YURT), 
				new Mission(3, PrivateMissionType.POWERUPS_YURT), "Governador"
		));
		
		cards.add(new PrivateCounselor(
				new Mission(2, PrivateMissionType.CITY_2YURT), new Mission(3, PrivateMissionType.CITY_2YURT), 
				new Mission(4, PrivateMissionType.CITY_2YURT), "Prefeito"
		));
		
		cards.add(new PrivateCounselor(
				new Mission(2, PrivateMissionType.YURT_KHAN), new Mission(3, PrivateMissionType.YURT_KHAN), 
				new Mission(4, PrivateMissionType.YURT_KHAN), "Bajulador"
		));
		
		Collections.shuffle(cards);
	}
	
	
	/**
	 * Gives 2 Private Counselors from the original {@code ArrayList} and removes them,
	 * so no other player can get them
	 * @return {@code ArrayList<PrivateCounselor>} with 2 elements
	 */
	public static ArrayList<PrivateCounselor> giveTwoCards() {
		ArrayList<PrivateCounselor> tmp = new ArrayList<PrivateCounselor>();
		for (int i = 0; i < 2; i++)
			tmp.add(cards.remove(0));
		return tmp;
	}
	
	
	/**
	 * Set a mission as completed
	 * @param m mission that was completed
	 * @return {@code true} if the mission was not completed and could set it as complete,
	 * {@code false} otherwise
	 */
	public boolean setFinished(Mission m) {
		if (!m.hasFinishedMission()) {
			m.setFinished();
			
			return true;			
		}
		return false;
	}
	
	
	@Override
	public int gainPoints() {
		int ret = (m1.hasFinishedMission() ? 1 : 0) + (m2.hasFinishedMission() ? 1 : 0) + (m3.hasFinishedMission() ? 1 : 0);
		return ret;
	}
	
	
	/**
	 * Get the name of the Counselor
	 * @return {@code String} with the name of the Counselor
	 */
	public String getName() {
		return name;
	}
}
