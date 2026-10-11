package model;


class Mission {
	int target;
	MissionType type;
	boolean hasFinished;
	
	public Mission(int qtd, MissionType type) {
		target = qtd;
		this.type = type;
		hasFinished = false;
	}
	
	
	public boolean setFinished() {
		hasFinished = true;
		return true;
	}
	
	
	public boolean hasFinishedMission() {
		return hasFinished == true;
	}
}
