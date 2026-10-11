package model;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class PlayerTests {
	private CentralNode karakorum;
	private Node nodeA;
	private Node nodeB;
	private Player tolui;

	@Before
	public void initializeVariables() {
		karakorum = new CentralNode(false);
		nodeA = new SingleNode("CHINA", "Pequim");
		nodeB = new DoubleNode("CHINA", new TradingPost(1, new TreasureRequirements[6], 0));
		tolui = new Player(PlayerColor.BLUE, Heir.TOLUI, karakorum, 2);
	}

	/**
	 * Goal: a player cannot spend tributes he does not have.
	 * Scenario: a player with 2 coins tries to spend 3, then 2.
	 * Expected: the first attempt fails without changes; the second works.
	 */
	@Test
	public void spendingMoreThanOwnedFailsWithoutChanges() {
		assertFalse(tolui.spendTribute(TributeType.COIN, 3));
		assertEquals(2, tolui.getTributeAmount(TributeType.COIN));

		assertTrue(tolui.spendTribute(TributeType.COIN, 2));
		assertEquals(0, tolui.getTributeAmount(TributeType.COIN));
	}

	/**
	 * Goal: activating a column loads its actions for the turn.
	 * Scenario: Tolui activates the yellow column (horse, 2 hands, Khan).
	 * Expected: 1 movement, 2 hands and the Khan must be used.
	 */
	@Test
	public void startTurnLoadsActionsOfTheColumn() {
		assertTrue(tolui.startTurn(ColumnColor.YELLOW, 0));

		assertEquals(1, tolui.getMovementsLeft());
		assertEquals(2, tolui.getHandsLeft());
		assertTrue(tolui.mustUseKhan());
		assertFalse(tolui.useMovement(2));
	}

	/**
	 * Goal: the virtual coin is spent first and is lost when the turn ends.
	 * Scenario: Tolui (2 coins) activates the red column, which has a virtual coin,
	 * and spends 1 coin.
	 * Expected: the virtual coin is used and the 2 real coins remain;
	 * at the end of the turn no virtual coin is left.
	 */
	@Test
	public void virtualCoinIsSpentFirstAndLostAtEndOfTurn() {
		tolui.startTurn(ColumnColor.RED, 0);
		assertEquals(1, tolui.getVirtualTributeAmount(TributeType.COIN));

		assertTrue(tolui.spendTribute(TributeType.COIN, 1));
		assertEquals(0, tolui.getVirtualTributeAmount(TributeType.COIN));
		assertEquals(2, tolui.getTributeAmount(TributeType.COIN));

		tolui.startTurn(ColumnColor.GREEN, 0);
		tolui.endTurn();
		assertEquals(0, tolui.getVirtualTributeAmount(TributeType.COIN));
	}

	/**
	 * Goal: an upgrade costs 2 coins.
	 * Scenario: a player with 1 coin tries to buy an upgrade, then receives 1 more coin.
	 * Expected: the first purchase fails without changes; the second costs 2 coins.
	 */
	@Test
	public void buyingUpgradeCostsTwoCoins() {
		Player p = new Player(PlayerColor.RED, Heir.JOCHI, karakorum, 1);
		Upgrade upgrade = new Upgrade(Icon.HORSE, 2, ColumnColor.GREEN);

		assertFalse(p.buyUpgrade(ColumnColor.GREEN, upgrade));
		assertEquals(1, p.getTributeAmount(TributeType.COIN));

		p.addTribute(TributeType.COIN, 1);
		assertTrue(p.buyUpgrade(ColumnColor.GREEN, upgrade));
		assertEquals(0, p.getTributeAmount(TributeType.COIN));
		assertEquals(1, p.getBoard().getColumn(ColumnColor.GREEN).getUpgradeCount());
	}

	/**
	 * Goal: building a yurt needs a yurt tribute and a piece from the stock.
	 * Scenario: a player without yurt tributes tries to pay for a yurt, then gets 1.
	 * Expected: the first attempt fails; the second spends the tribute and 1 piece.
	 */
	@Test
	public void payForYurtNeedsTributeAndStock() {
		assertFalse(tolui.payForYurt());
		assertEquals(Player.INITIAL_YURTS, tolui.getYurtsLeft());

		tolui.addTribute(TributeType.YURT, 1);
		assertTrue(tolui.payForYurt());
		assertEquals(0, tolui.getTributeAmount(TributeType.YURT));
		assertEquals(Player.INITIAL_YURTS - 1, tolui.getYurtsLeft());
	}

	/**
	 * Goal: a player has only 12 yurt pieces.
	 * Scenario: the player takes 12 yurts from the stock and tries a 13th.
	 * Expected: the first 12 work; the 13th fails.
	 */
	@Test
	public void playerHasOnlyTwelveYurts() {
		for (int i = 0; i < Player.INITIAL_YURTS; i++) {
			assertTrue(tolui.takeYurtFromStock());
		}
		assertFalse(tolui.takeYurtFromStock());
		assertEquals(0, tolui.getYurtsLeft());
	}

	/**
	 * Goal: the turn path starts over every turn.
	 * Scenario: the pawn goes from Karakorum to A and B; then a new turn starts.
	 * Expected: first turn path is Karakorum, A, B; second turn path is only B.
	 */
	@Test
	public void turnPathRestartsEveryTurn() {
		tolui.startTurn(ColumnColor.GREEN, 0);
		tolui.moveTo(nodeA);
		tolui.moveTo(nodeB);
		assertEquals(Arrays.asList(karakorum, nodeA, nodeB), tolui.getTurnPath());
		tolui.endTurn();

		tolui.startTurn(ColumnColor.RED, 0);
		assertEquals(Arrays.asList(nodeB), tolui.getTurnPath());
		assertFalse(tolui.hasVisited(nodeA));
	}

	/**
	 * Goal: Altani has 2 pawns, but only the chosen one moves.
	 * Scenario: Altani chooses her 2nd pawn and moves it to A.
	 * Expected: 2nd pawn at A, 1st pawn still at Karakorum; a 3rd pawn does not exist.
	 */
	@Test
	public void onlyTheChosenPawnMoves() {
		Player altani = new Player(PlayerColor.GREEN, Heir.ALTANI, karakorum, 1);
		assertEquals(2, altani.getPawnCount());
		assertFalse(altani.startTurn(ColumnColor.GREEN, 2));

		assertTrue(altani.startTurn(ColumnColor.GREEN, 1));
		altani.moveTo(nodeA);

		assertSame(nodeA, altani.getPawn(1).getCurrentNode());
		assertSame(karakorum, altani.getPawn(0).getCurrentNode());
	}

	/**
	 * Goal: a player created only by name uses the matching heir.
	 * Scenario: players named "Altani" and "Batu" (Batu is not an heir).
	 * Expected: "Altani" gets her 2 pawns; "Batu" keeps his name and the default board.
	 */
	@Test
	public void playerCreatedByNameUsesMatchingHeir() {
		Player altani = new Player("Altani");
		Player batu = new Player("Batu");

		assertEquals("Altani", altani.getName());
		assertEquals(Heir.ALTANI, altani.getHeir());
		assertEquals(2, altani.getPawnCount());
		assertEquals("Batu", batu.getName());
		assertEquals(Heir.TOLUI, batu.getHeir());
	}
}
