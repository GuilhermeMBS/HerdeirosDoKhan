package model;


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
 * @see Jogador
 * 
 * @author joaop
 * @version 1.0
 */
public class Acao {
	private Node currNode = null;
	private tiposAcao type = null;
	private Jogador player = null;
	
	public boolean setType(tiposAcao tipo) {
		this.type = tipo;
		return true;
	}
	
	public boolean setNode(Node no) {
		currNode = no;
		return true;
	}
	
	public boolean setPlayer(Jogador jog) {
		player = jog;
		return true;
	}
	
	private boolean resetInfo() {
		currNode = null;
		type = null;
		player = null;
		
		return true;
	}
	
	public DebugRetAction doAction(int qtd) {
		if (player == null) return DebugRetAction.missingPlayer;
		if (type == null) return DebugRetAction.missingAction;
		if (currNode == null) return DebugRetAction.missingNode;
		
		boolean ret;
		// Criar uma interface com as infos das ações e depois criar outros tipos que as implementem
		switch (type) {
		case movimento:
			ret = player.removerMovimento(qtd);
			if (ret == false) return DebugRetAction.noMovementLeft;
			
			break;
		case yurt:
			ret = player.removerAssentamento(qtd);
			if (!ret) return DebugRetAction.noHouseLeft;
			
			currNode.colocarYurt(player);
			
			break;
		case saquear:
			ret = player.removerEspada(qtd);
			if (!ret) return DebugRetAction.noSwordLeft;
			
			break;
		case pegarTributo:
			ret = player.removerMao(qtd);
			if (!ret) return DebugRetAction.noHandLeft;
			
			break;
		case moverKhan:
			break;
		case troca:
			break;
		default:
			return DebugRetAction.noValidAction;
		}
		
		resetInfo();
		return retSuccess;
	}


}
