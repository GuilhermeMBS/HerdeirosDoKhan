package Model;

import static org.junit.Assert.*;
import org.junit.Before;
import java.util.*;

import org.junit.Test;

public class MapaTests {
	private Mapa map;
	private Jogador p;
	private ArrayList<Cidade> cityArray;
	private Cidade city;


	@Before
	public void initializeVariables() {
		map = new Mapa();
		p = new Jogador();
		cityArray = map.createCityCopies();
		city = cityArray.get(0);
	}
	

	@Test
	public void testMapa() {
		assertEquals(4, map.getLengthNodeArray()); // 3 regions + central node
		assertEquals(11, map.getNodePerRegion());
		assertEquals(9, map.getAmountCity());
		
		// The order must be maintained as in "createCities" inside Mapa.java
		String[] cityNames = {"Cantão", "Pequim", "Kaesong", "Moscou", "Kiev", "Sarai", "Cabul", "Bagdá", "Samarcanda"};
		int i = 0;
		
		for (String name : cityNames) {
			assertTrue(cityArray.get(i).getName().equals(name));
			i++;
		}
	}
	
	
	@Test
	public void testCityDomination() {		
		// A city can't be dominated if it is not available for sacking
		assertFalse(city.defineDomination(p));
		
		// You can't remove a resource if a city is not available for sacking
		assertNull(city.removeResource(0));
		
		city.activateSacking();
		city.removeResource(0);
		city.removeResource(0);
		city.removeResource(0);
		city.removeResource(0);
		
		assertNull(city.dominatedBy());
		
		map.setCityConquered(city.getName(), p);
		
		assertEquals(p, city.dominatedBy());
	}

	
	@Test
	public void testAllCityConquered() {		
		assertFalse(map.allCityConquered());
		
		for (Cidade city : cityArray) {
			city.activateSacking();
			for (int i = 0; i < 4; i++)
				city.removeResource(0);
			map.setCityConquered(city.getName(), p);
		}
		
		assertTrue(map.allCityConquered());
	}

}
