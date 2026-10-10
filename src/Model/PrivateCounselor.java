package Model;


import java.util.*;


class PrivateCounselor extends Card {
	private boolean hasFinished;
	String name;
	
	
	public PrivateCounselor(Mission m1, Mission m2, Mission m3, String name) {
		this.m1 = m1;
		this.m2 = m2;
		this.m3 = m3;
		hasFinished = false;
		cards = new ArrayList<Card>();
		this.name = name;
	}
	
	
	static {
		// Code
	}
	
	
	public static ArrayList<PrivateCounselor> giveFourCards() {
		ArrayList<PrivateCounselor> tmp = new ArrayList<PrivateCounselor>();
		for (int i = 0; i < 4; i++)
			tmp.add((PrivateCounselor) cards.remove(0));
		return tmp;
	}
	
	
	@Override
	public boolean hasBeenCompleted() {
		return hasFinished == true;
	}
	
	
	public boolean setOwner(Player p) {
		if (p != null) return false;
		
		this.p = p;
		return true;
	}
}
