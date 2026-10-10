package Model;


enum tiposAcao {
	movimento,
	yurt,
	pegarTributo,
	troca,
	saquear,
	moverKhan
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
public class Acao {
	private Node currNode = null;
	private tiposAcao type = null;
	private Player player = null;
	
	public boolean setType(tiposAcao tipo) {
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
		if (player == null) return DebugReturn.missingPlayer;
		if (type == null) return DebugReturn.missingAction;
		if (currNode == null) return DebugReturn.missingNode;
		
		boolean ret;
		// Criar uma interface com as infos das ações e depois criar outros tipos que as implementem
		switch (type) {
		case movimento:
			ret = player.removerMovimento(qtd);
			if (ret == false) return DebugReturn.noMovementLeft;
			
			break;
		case yurt:
			ret = player.removerAssentamento(qtd);
			if (!ret) return DebugReturn.noHouseLeft;
			
			currNode.colocarYurt(player);
			
			break;
		case saquear:
			ret = player.removerEspada(qtd);
			if (!ret) return DebugReturn.noSwordLeft;
			
			break;
		case pegarTributo:
			ret = player.removerMao(qtd);
			if (!ret) return DebugReturn.noHandLeft;
			
			break;
		case moverKhan:
			break;
		case troca:
			break;
		default:
			return DebugReturn.noValidAction;
		}
		
		resetInfo();
		return DebugReturn.retSuccess;
	}


}
