package model;

public class YurtAction implements GameAction {
	@Override
	public DebugReturn execute(Player p, Node node) {
		if (node.hasYurt()) {
			return DebugReturn.NODE_FULL_YURT;
		} else if (!p.payForYurt()) {
			return DebugReturn.EMPTY_HOUSES;
		}
		
		node.buildYurt(p);
		
		return DebugReturn.SUCCESS;
	}

}
