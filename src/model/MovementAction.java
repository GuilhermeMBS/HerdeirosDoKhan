package model;

public class MovementAction implements GameAction {
	private final int qty;
	
	
	public MovementAction(int qty) {
		this.qty = qty;
	}

	
	@Override
	 public DebugReturn execute(Player p, Node node) {
		if (!p.useMovement(qty)) {
			return DebugReturn.EMPTY_MOVEMENT;
		}
		p.moveTo(node);
		return DebugReturn.SUCCESS;
	}
}
