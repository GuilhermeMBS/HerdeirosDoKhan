package model;


import java.util.*;


class PrivateCounselor extends Card {
	private boolean hasFinished;
	private String name;
	protected static ArrayList<PrivateCounselor> cards = new ArrayList<PrivateCounselor>();
	
	
	public PrivateCounselor(Mission m1, Mission m2, Mission m3, String name) {
		this.m1 = m1;
		this.m2 = m2;
		this.m3 = m3;
		hasFinished = false;
		this.name = name;
	}
	
	
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
	
	
	public static ArrayList<PrivateCounselor> giveTwoCards() {
		ArrayList<PrivateCounselor> tmp = new ArrayList<PrivateCounselor>();
		for (int i = 0; i < 2; i++)
			tmp.add(cards.remove(0));
		return tmp;
	}
	
	
	@Override
	protected boolean hasBeenCompleted() {
		return hasFinished == true;
	}
	
	
	public boolean setOwner(Player p) {
		if (this.p != null) return false;
		
		this.p = p;
		return true;
	}
	
	
	public boolean setFinished(Mission m) {
		if (!m.hasFinishedMission()) {
			m.setFinished();
			
			return true;			
		}
		return false;
	}
	
	
	public int gainPoints() {
		int ret = (m1.hasFinishedMission() ? 1 : 0) + (m2.hasFinishedMission() ? 1 : 0) + (m3.hasFinishedMission() ? 1 : 0);
		return ret;
	}
	
	
	public String getName() {
		return name;
	}
}
