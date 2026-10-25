package model;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.*;


public class CardsTests {
	private ArrayList<OpenCounselor> open;
	private ArrayList<PrivateCounselor> pri;
	private Player p;

	@Before
	public void initializeVariables() {
		open = OpenCounselor.giveCardsInGame();
		pri = PrivateCounselor.giveTwoCards();
		p = new Player();
	}
	

	@Test
	public void testOpenCounselor4Cards() {
		assertEquals(4, open.size());
	}
	
	
	@Test
	public void testPrivateCounselor4Cards() {
		assertEquals(2, pri.size());
	}
	
	
	@Test
	public void testsetFinishedMissionOpen() {
		p.setSecretCard(pri.get(0));
		assertEquals(pri.get(0), p.getSecretCard());
		
		assertTrue(pri.get(0).setFinished(pri.get(0).m1));
		assertTrue(p.getSecretCard().m1.hasFinishedMission());
	}
	
	
	@Test
	public void testGainPointsPrivate() {
		PrivateCounselor privC = pri.get(0);

		assertEquals(0, privC.gainPoints());
		
		privC.setFinished(privC.m1);
		assertEquals(1, privC.gainPoints());
		
		privC.setFinished(privC.m2); privC.setFinished(privC.m3); privC.setFinished(privC.m1);
		assertEquals(3, privC.gainPoints());
	}
	
	
	@Test
	public void testGainPointsOpen() {
		OpenCounselor openC = open.get(0);
		
		assertEquals(0, openC.gainPoints());
		
		openC.playerHasCompleted(p);
		
		assertEquals(1, openC.gainPoints());
	}
}
