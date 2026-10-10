package model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PlayerBoardTests {
	private PlayerBoard board;

	@Before
	public void initializeVariables() {
		board = Heir.TOLUI.createBoard();
	}

	/**
	 * Goal: an occupied column cannot be activated again.
	 * Scenario: the red column is activated twice.
	 * Expected: the second activation fails and 3 tokens are left.
	 */
	@Test
	public void cannotActivateOccupiedColumn() {
		assertTrue(board.activate(ColumnColor.RED));
		assertFalse(board.activate(ColumnColor.RED));
		assertEquals(3, board.availableTokens());
	}

	/**
	 * Goal: the white column takes back all the activation tokens.
	 * Scenario: the three colored columns are activated, then the white one.
	 * Expected: the 4 tokens come back and every column is free.
	 */
	@Test
	public void whiteColumnRecoversAllTokens() {
		board.activate(ColumnColor.RED);
		board.activate(ColumnColor.GREEN);
		board.activate(ColumnColor.YELLOW);
		assertEquals(1, board.availableTokens());

		assertTrue(board.activate(ColumnColor.WHITE));
		assertEquals(4, board.availableTokens());
		assertTrue(board.canActivate(ColumnColor.RED));
	}

	/**
	 * Goal: completing a row of 3 upgrades gives exactly one free upgrade.
	 * Scenario: one upgrade on each colored column, then two free upgrade requests.
	 * Expected: the first request goes to the white column; the second is refused.
	 */
	@Test
	public void completeRowGivesSingleFreeUpgrade() {
		board.addUpgrade(ColumnColor.RED, new Upgrade(Icon.HAND, 1, ColumnColor.RED));
		board.addUpgrade(ColumnColor.GREEN, new Upgrade(Icon.HORSE, 2, ColumnColor.GREEN));
		assertFalse(board.hasFreeUpgradePending());

		board.addUpgrade(ColumnColor.YELLOW, new Upgrade(Icon.VIRTUAL_SWORD, 1, ColumnColor.YELLOW));
		assertTrue(board.hasFreeUpgradePending());

		assertTrue(board.receiveFreeUpgrade(new Upgrade(Icon.VIRTUAL_COIN, 1, ColumnColor.GREEN)));
		assertFalse(board.receiveFreeUpgrade(new Upgrade(Icon.VIRTUAL_COIN, 1, ColumnColor.GREEN)));
		assertEquals(1, board.getColumn(ColumnColor.WHITE).getUpgradeCount());
	}
}
