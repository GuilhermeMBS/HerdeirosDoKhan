package Model;

class PrivateCounselor extends Card {
	private boolean hasFinished;
	
	
	public PrivateCounselor(Mission m1, Mission m2, Mission m3) {
		this.m1 = m1;
		this.m2 = m2;
		this.m3 = m3;
		hasFinished = false;
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
