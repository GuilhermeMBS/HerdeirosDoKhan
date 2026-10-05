package Model;

import java.util.*;


public class Mapa {
	private static final int QTD_CIDADES = 8;
	
	private ArrayList<Cidade> cityArray = new ArrayList<Cidade>(QTD_CIDADES);
	private ArrayList<No> listaNo;
	
	public Mapa() {
		// Chinese region (red)
		cityArray.add(new Cidade("Cantão"));
		cityArray.add(new Cidade("Pequim"));
		cityArray.add(new Cidade("Kaesong"));
		
		// Russian region (yellow)
		cityArray.add(new Cidade("Moscou"));
		cityArray.add(new Cidade("Kiev"));
		cityArray.add(new Cidade("Sarai"));
		
		// Persian region (green)
		cityArray.add(new Cidade("Cabul"));
		cityArray.add(new Cidade("Bagdá"));
		cityArray.add(new Cidade("Samarcanda"));
	}
	
	boolean todasCidadesConquistadas() {
		for (Cidade city : cityArray) {
			if (city.dominatedByPlayer() == null) return false;
		}
		return true;
	}
}
