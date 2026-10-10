package Model;

class OpenCounselor extends Card {
	public OpenCounselor(int qtd, missionType type) {
		m1 = new Mission(qtd, type, 1);
		m2 = m3 = null;
		p = null;
	}
	
	
	public Player playerHasDone() {
		return p;
	}
	
	
	@Override
	public boolean hasBeenCompleted() {
		return p == null;
	}
	
	
	public boolean setPlayer(Player player) {
		if (p == null) {
			p = player;
			return true;
		}
		return false;
	}
}
