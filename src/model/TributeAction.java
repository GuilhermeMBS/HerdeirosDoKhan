package model;

public class TributeAction implements GameAction {
	Province province;
	
	
	public TributeAction(Province p) {
		province = p;
	}

	
	@Override
	public DebugReturn execute(Player p, Node node) {
		if (!p.useHand(1)) return DebugReturn.EMPTY_HANDS;
		
		if (province.collectResource()) {
			p.addTribute(province.getTributeType(), 1);
		} else {
			return DebugReturn.MISSING_TRIBUTE;
		}
		
		return DebugReturn.SUCCESS;
	}

}
