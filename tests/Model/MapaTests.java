package Model;

import static org.junit.Assert.*;
import org.junit.Before;
import java.util.*;

import org.junit.Test;

public class MapaTests {
	private Mapa map;
	private Player p;
	private ArrayList<Cidade> cityArray;
	private Cidade city;


	@Before
	public void initializeVariables() {
		map = new Mapa();
		p = new Player();
		cityArray = map.createCityCopies();
		city = cityArray.get(0);
	}
	

	@Test
	public void testMapa() {
		assertEquals(4, map.getLengthNodeArray()); // 3 regions + central node
		assertEquals(11, map.getNodePerRegion());
		assertEquals(9, map.getAmountCity());
		
		// The order must be maintained as in "createCities" inside Mapa.java
		String[] cityNames = map.getNameOrder();
		
		assertEquals(cityNames.length, cityArray.size());
		
		for (int i = 0; i < cityNames.length; i++) {
			assertEquals(
				"Missmatch at index " + i,
				cityNames[i], 
				cityArray.get(i).getName()
			);
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
		
		for (Cidade cidades : cityArray) {
			conquerCity(cidades, p);
		}
		
		assertTrue(map.allCityConquered());
	}
	
	
	private void conquerCity(Cidade city, Player player) {
		city.activateSacking();
		for (int i = 0; i < 4; i++)
			city.removeResource(0);
		city.defineDomination(player);
		return;
	}

}
