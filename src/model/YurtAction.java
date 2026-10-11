package model;

public class YurtAction implements GameAction {
	@Override
	public DebugReturn execute(Player p, Node node) {
		if (!p.payForYurt()) {
			return DebugReturn.EMPTY_HOUSES;
		}
		else if (node.hasYurt()) {
			return DebugReturn.NODE_FULL_YURT;
		}
		
		node.buildYurt(p);
		
		return DebugReturn.SUCCESS;
	}

}
