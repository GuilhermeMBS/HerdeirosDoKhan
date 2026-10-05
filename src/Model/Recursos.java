package Model;

import java.util.*;

enum typeResource {
	iron,
	treasure,
	wool,
	silk,
	grain
}

class Recursos {
	private static ArrayList<typeResource> resources = new ArrayList<typeResource>();
	private static final int QTY_PER_RESOURCE = 8; 
	
	static {
		for (typeResource tipo : typeResource.values()) {
			for (int i = 0; i < QTY_PER_RESOURCE; i++) {
				resources.add(tipo);
			}
		}
		
		Collections.shuffle(resources);
	}
	
	public static ArrayList<typeResource> gerarresources() {
		if (resources.isEmpty()) return null;
		
		ArrayList<typeResource> tmp = new ArrayList<typeResource>();
		for (int i = 0; i < 4; i++) {
			tmp.add(resources.remove(0));
		}
		
		return tmp;
	}
}
