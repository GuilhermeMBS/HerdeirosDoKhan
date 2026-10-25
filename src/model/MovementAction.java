package model;

/**
 * Moves the player's active pawn to the node that was clicked.
 * The rules of the movement are checked by {@link Player#moveActivePawn(Node)}.
 * If the move is not allowed, nothing changes and
 * {@link DebugReturn#EMPTY_MOVEMENT} is returned.
 */
public class MovementAction implements GameAction {

	private MovementAction() {
	}


	public MovementAction(int qty) {
		this();
	}


	@Override
	 public DebugReturn execute(Player p, Node node) {
		if (!p.moveActivePawn(node)) {
			return DebugReturn.EMPTY_MOVEMENT;
		}
		return DebugReturn.SUCCESS;
	}
}
