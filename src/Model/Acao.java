package Model;

enum tiposAcao {
	movimento,
	yurt,
	pegarTributo,
	troca,
	saquear,
	moverKhan
}


enum DebugRetAction {
	retSuccess,
	missingPlayer,
	missingNode,
	missingAction,
	noMovementLeft,
	noHouseLeft,
	noSwordLeft,
	noHandLeft,
	noValidAction
}


public class Acao {
	private No noAtual = null;
	private tiposAcao tipo = null;
	private Jogador player = null;
	
	public boolean setTipo(tiposAcao tipo) {
		this.tipo = tipo;
		return true;
	}
	
	public boolean setNo(No no) {
		noAtual = no;
		return true;
	}
	
	public boolean setJogador(Jogador jog) {
		player = jog;
		return true;
	}
	
	private boolean resetInfo() {
		noAtual = null;
		tipo = null;
		player = null;
		
		return true;
	}
	
	public DebugRetAction realizarAcao(int qtd) {
		if (player == null) return DebugRetAction.missingPlayer;
		if (tipo == null) return DebugRetAction.missingAction;
		if (noAtual == null) return DebugRetAction.missingNode;
		
		boolean ret;
		switch (tipo) {
		case movimento:
			ret = player.removerMovimento(qtd);
			if (ret == false) return DebugRetAction.noMovementLeft;
			
			break;
		case yurt:
			ret = player.removerAssentamento(qtd);
			if (!ret) return DebugRetAction.noHouseLeft;
			
			noAtual.colocarYurt(player);
			
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
