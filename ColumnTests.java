package model;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class ColumnTests {
	private Column red;

	@Before
	public void initializeVariables() {
		red = new Column(ColumnColor.RED, Arrays.asList(Icon.HORSE, Icon.HAND));
	}

	/**
	 * Goal: a colored column only accepts upgrades of its own color.
	 * Scenario: the red column receives a green upgrade.
	 * Expected: the upgrade is rejected and the column stays empty.
	 */
	@Test
	public void upgradeOfOtherColorIsRejected() {
		assertFalse(red.addUpgrade(new Upgrade(Icon.HAND, 1, ColumnColor.GREEN)));
		assertEquals(0, red.getUpgradeCount());
	}

	/**
	 * Goal: a column holds at most 4 upgrades.
	 * Scenario: the red column already has 4 upgrades and receives a 5th.
	 * Expected: the 5th upgrade is rejected.
	 */
	@Test
	public void columnDoesNotAcceptFifthUpgrade() {
		for (int i = 0; i < Column.MAX_UPGRADES; i++) {
			assertTrue(red.addUpgrade(new Upgrade(Icon.VIRTUAL_COIN, 1, ColumnColor.RED)));
		}
		assertFalse(red.addUpgrade(new Upgrade(Icon.VIRTUAL_COIN, 1, ColumnColor.RED)));
		assertEquals(4, red.getUpgradeCount());
	}

	/**
	 * Goal: an upgrade cannot be used in the turn it was bought.
	 * Scenario: a column with 1 base horse receives the "two horses" upgrade.
	 * Expected: 1 horse in the buying turn; 3 horses after the turn ends.
	 */
	@Test
	public void upgradeOnlyCountsNextTurn() {
		red.addUpgrade(new Upgrade(Icon.HORSE, 2, ColumnColor.RED));
		assertEquals(1, red.count(Icon.HORSE));

		red.endTurn();
		assertEquals(3, red.count(Icon.HORSE));
	}
}
