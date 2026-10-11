package model;


enum ActionType {
	MOVIMENT,
	YURT,
	TAKE_TRIBUTE,
	TRADE,
	SACK,
	MOVE_KHAN
}


/**
 * Class that represents all available actions for the player
 * 
 * <p>Available methods:</p>
 * <ul>
 * 		<li></li>
 * </ul>
 * 
 * @see No
 * @see Player
 * 
 * @author joaop
 * @version 1.0
 */
public class Action {
	private Node node = null;
	private GameAction action = null;
	private Player player = null;
	
	
	public Action(Player p, Node n, GameAction action) {
		node = n;
		player = p;
		this.action = action;
	}
	
	
	public boolean setAction(GameAction tipo) {
		this.action = tipo;
		return true;
	}
	
	
	public boolean setNode(Node no) {
		node = no;
		return true;
	}
	
	
	public boolean setPlayer(Player jog) {
		player = jog;
		return true;
	}
	
	
	private void resetInfo() {
		node = null;
		action = null;
		player = null;
	}
	
	
	public DebugReturn doAction(int qtd) {
		if (player == null) return DebugReturn.MISSING_PLAYER;
		if (action == null) return DebugReturn.MISSING_ACTION;
		if (node == null) return DebugReturn.MISSING_NODE;
		
		DebugReturn result = action.execute(player, node);
		
		if (result == DebugReturn.SUCCESS) resetInfo();
		return result;
	}
}
