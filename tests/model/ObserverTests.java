package model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of the Observer pattern: GameData (observed) must notify the
 * registered observers only when the state of the match really changes.
 *
 * @author Vinicius
 */
public class ObserverTests {

	/** Fake screen that counts the notifications and reads the state back. */
	private static class FakeScreen implements ObservadorIF {
		int notifications = 0;
		int movementsLeft = -1;

		@Override
		public void notify(ObservadoIF o) {
			notifications++;
			movementsLeft = o.get(GameData.MOVEMENTS_LEFT);
		}
	}

	private GameData data;
	private FakeScreen screen;

	@Before
	public void setUp() {
		data = GameData.createData(false);
		data.setupMatch(Arrays.asList("Tolui", "Altani"), false);
		screen = new FakeScreen();
		data.add(screen);
	}

	@After
	public void tearDown() {
		data.remove(screen);
	}

	/**
	 * Goal: choosing a column notifies the screens with the new horses.
	 * Scenario: Tolui activates the green column (2 horses).
	 * Expected: 1 notification, and the screen reads 2 horses with get().
	 */
	@Test
	public void choosingColumnNotifiesObservers() {
		assertTrue(data.startTurn(0, ColumnColor.GREEN, 0));

		assertEquals(1, screen.notifications);
		assertEquals(2, screen.movementsLeft);
	}

	/**
	 * Goal: a valid move notifies the screens, an invalid one does not.
	 * Scenario: Tolui (2 horses) tries to go to a node out of reach, then to a reachable one.
	 * Expected: no notification for the invalid move; 1 for the valid move.
	 */
	@Test
	public void onlyValidMoveNotifiesObservers() {
		data.startTurn(0, ColumnColor.GREEN, 0);
		screen.notifications = 0;

		List<String> reachable = data.getReachableNodeIds(0);
		String outOfReach = null;
		for (String id : data.getAllNodeIds()) {
			if (!reachable.contains(id) && !id.equals(data.getPlayerNodeId(0))) {
				outOfReach = id;
				break;
			}
		}

		assertFalse(data.MovePawn(0, outOfReach));
		assertEquals(0, screen.notifications);

		assertTrue(data.MovePawn(0, reachable.get(0)));
		assertEquals(1, screen.notifications);
	}

	/**
	 * Goal: a removed observer is not notified anymore.
	 * Scenario: the screen is removed and then Tolui chooses a column.
	 * Expected: no notification.
	 */
	@Test
	public void removedObserverIsNotNotified() {
		data.remove(screen);
		data.startTurn(0, ColumnColor.GREEN, 0);

		assertEquals(0, screen.notifications);
	}

	/**
	 * Goal: passing the turn notifies the screens with the new player.
	 * Scenario: the Controller tells GameData that it is now player 1's turn.
	 * Expected: 1 notification, and get(CURRENT_PLAYER) returns 1.
	 */
	@Test
	public void passingTurnNotifiesObservers() {
		data.setCurrentPlayer(1);

		assertEquals(1, screen.notifications);
		assertEquals(1, data.get(GameData.CURRENT_PLAYER));
	}
}
