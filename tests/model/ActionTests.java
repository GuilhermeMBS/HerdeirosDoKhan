package model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ActionTests {
	private Player p;
	private Node node, centralNode;
	private Khan khan;
	private Province province;
	private City city;
	private TradingPost trade;
	private Action action;
	
	
	
	private void createMovementAction() {
		action = new Action(p, node, new MovementAction(1));
	}
	
	private void createYurtAction() {
		action = new Action(p, node, new YurtAction());
	}
	
	private void createKhanAction() {
		province = new Province(TributeType.COIN);
		khan = new Khan();
		action = new Action(p, node, new KhanAction(province, khan));
	}
	
	private void createSackAction(int qtd) {
		action = new Action(p, node, new SackAction(city, qtd, 0));
	}
	
	private void createTradeAction() {
		action = new Action(p, node, new TradeAction(trade));
	}
	
	private void createTributeAction() {
		action = new Action(p, node, new TributeAction(province));
	}

	@Before
	public void setUp() {
		Resource.resetResources();
		centralNode = new CentralNode(false);
		p = new Player(PlayerColor.BLUE, Heir.TOLUI, centralNode, 2);
		node = new SingleNode("a", "b");
	}

	@Test
	public void testSetAction() {
		createMovementAction();
		assertTrue(action.setAction(new YurtAction()));
	}

	@Test
	public void testSetNode() {
		createMovementAction();
		Node b = new SingleNode("c", "d");
		assertTrue(action.setNode(b));
	}

	@Test
	public void testSetPlayer() {
		createMovementAction();
		Player b = new Player();
		assertTrue(action.setPlayer(b));
	}

	@Test
	public void testDoActionMovement() {
		p.startTurn(ColumnColor.GREEN, 0);

		createMovementAction();
		assertEquals(DebugReturn.SUCCESS, action.doAction());
		
		createMovementAction();
		assertEquals(DebugReturn.SUCCESS, action.doAction());
		
		createMovementAction();
		assertEquals(DebugReturn.EMPTY_MOVEMENT, action.doAction());
	}
	
	@Test
	public void testDoActionYurt() {
		p.startTurn(ColumnColor.GREEN, 0);
		
		createYurtAction();
		assertEquals(DebugReturn.EMPTY_HOUSES, action.doAction());
		
		p.addTribute(TributeType.YURT, 2);
		
		createYurtAction();
		assertEquals(DebugReturn.SUCCESS, action.doAction());
		
		createYurtAction();
		assertEquals(DebugReturn.NODE_FULL_YURT, action.doAction());
		
		node = new SingleNode("c", "d");
		createYurtAction();
		assertEquals(DebugReturn.SUCCESS, action.doAction());
		
		node = new SingleNode("e", "f");
		createYurtAction();
		assertEquals(DebugReturn.EMPTY_HOUSES, action.doAction());
	}
	
	@Test
	public void testDoKhanAction() {
		p.startTurn(ColumnColor.YELLOW, 0);
		
		createKhanAction();
		assertEquals(DebugReturn.SUCCESS, action.doAction());
		
		createKhanAction();
		assertEquals(DebugReturn.MISSING_KHANMOVE, action.doAction());
	}
	
	@Test
	public void testDoSackAction() {
		city = new City("ab");
		
		p.startTurn(ColumnColor.YELLOW, 0);
		
		createSackAction(1);
		assertEquals(DebugReturn.CANT_SACK_CITY, action.doAction());
		
		city.activateSacking();
		p.addTribute(TributeType.SWORD, 2);
		
		createSackAction(1);
		assertEquals(DebugReturn.SUCCESS, action.doAction());
		
		createSackAction(2);
		assertEquals(DebugReturn.EMPTY_SWORDS, action.doAction());
	}
	
	@Test
	public void testDoTradeAction() {
		trade = new TradingPost(1, new TreasureRequirements[] {
				new TreasureRequirements(TreasureType.ANIMAL, 2)
		}, 0);
		
		p.startTurn(ColumnColor.YELLOW, 0);
		p.addTreasure(TreasureType.FUR, 3);
		
		createTradeAction();
		assertEquals(DebugReturn.MISSING_RESOURCE, action.doAction());
		
		p.addTreasure(TreasureType.ANIMAL, 3);
		createTradeAction();
		assertEquals(DebugReturn.SUCCESS, action.doAction());
		
		trade = new TradingPost(1, new TreasureRequirements[] {
				new TreasureRequirements(TreasureType.ANIMAL, 2)
		}, 10);
		createTradeAction();
		assertEquals(DebugReturn.MISSING_RESOURCE, action.doAction());
	}
	
	@Test
	public void testDoTributeAction() {
		province = new Province(TributeType.COIN);
		
		p.startTurn(ColumnColor.YELLOW, 0);
		
		createTributeAction();
		assertEquals(DebugReturn.SUCCESS, action.doAction());
		
		createTributeAction();
		assertEquals(DebugReturn.MISSING_TRIBUTE, action.doAction());

		province.addResource();
		createTributeAction();
		action.doAction();
		
		province.addResource();
		createTributeAction();
		assertEquals(DebugReturn.EMPTY_HANDS, action.doAction());
	}
}
