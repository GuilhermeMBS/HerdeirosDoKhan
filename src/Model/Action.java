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
	private Node currNode = null;
	private ActionType type = null;
	private Player player = null;
	
	public boolean setType(ActionType tipo) {
		this.type = tipo;
		return true;
	}
	
	public boolean setNode(Node no) {
		currNode = no;
		return true;
	}
	
	public boolean setPlayer(Player jog) {
		player = jog;
		return true;
	}
	
	private boolean resetInfo() {
		currNode = null;
		type = null;
		player = null;
		
		return true;
	}
	
	public DebugReturn doAction(int qtd) {
		if (player == null) return DebugReturn.MISSING_PLAYER;
		if (type == null) return DebugReturn.MISSING_ACTION;
		if (currNode == null) return DebugReturn.MISSING_NODE;
		
		boolean ret;
		// Criar uma interface com as infos das ações e depois criar outros tipos que as implementem
		switch (type) {
		case MOVIMENT:
			ret = player.removerMovimento(qtd);
			if (ret == false) return DebugReturn.EMPTY_MOVIMENT;
			
			break;
		case YURT:
			ret = player.removerAssentamento(qtd);
			if (!ret) return DebugReturn.EMPTY_HOUSES;
			
			currNode.colocarYurt(player);
			
			break;
		case SACK:
			ret = player.removerEspada(qtd);
			if (!ret) return DebugReturn.EMPTY_SWORDS;
			
			break;
		case TAKE_TRIBUTE:
			ret = player.removerMao(qtd);
			if (!ret) return DebugReturn.EMPTY_ACTIONS;
			
			break;
		case MOVE_KHAN:
			break;
		case TRADE:
			break;
		default:
			return DebugReturn.EMPTY_ACTIONS;
		}
		
		resetInfo();
		return DebugReturn.SUCCESS;
	}


}
