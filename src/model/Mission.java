package model;


/**
 * Represents a mission from the cards (Open and Private Counselors)
 * 
 * <p>Available methods:</p>
 * <ul>
 * 		<li>{@link #setFinished()}</li>
 * 		<li>{@link #hasFinishedMission()}</li>
 * </ul>
 * 
 * @author joaop
 * @version 1.0
 */
class Mission {
	int target;
	MissionType type;
	boolean hasFinished;
	
	public Mission(int qtd, MissionType type) {
		target = qtd;
		this.type = type;
		hasFinished = false;
	}
	
	
	/**
	 * Sets the mission as completed
	 * @return {@code true} if it could assign as done,
	 * else {@code false} if it was already done
	 */
	public boolean setFinished() {
		if (hasFinished) return false;
		
		hasFinished = true;
		return true;
	}
	
	
	/**
	 * Verify if the mission has already been done
	 * @return {@code true} if the mission has already been done,
	 * else {@code false} otherwise
	 */
	public boolean hasFinishedMission() {
		return hasFinished == true;
	}
}
