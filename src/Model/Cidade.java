package Model;

import java.util.*;

class Cidade {
	private String name;
	private boolean saqueavel = false;
	private Jogador player = null;
	private ArrayList<typeResource> listaRecursos = new ArrayList<typeResource>();
	
	public Cidade(String name) {
		this.name = name;
	}
	
	public typeResource removeResource(int ind) {
		if (!saqueavel || listaRecursos.isEmpty()) return null;
		
		typeResource r = listaRecursos.get(ind);
		listaRecursos.remove(ind);
		return r;
	}

	public boolean activateSacking() {
		saqueavel = true;
		listaRecursos = Recursos.gerarRecursos();
		
		return true;
	}
	
	public boolean defineDomination(Jogador num) {
		if (!(saqueavel && listaRecursos.isEmpty())) return false;
		
		player = num;
		saqueavel = false;
		
		return true;
	}
	
	public Jogador dominatedByPlayer() {
		return player;
	}	
	
	public String getName() {
		return name;
	}
}
