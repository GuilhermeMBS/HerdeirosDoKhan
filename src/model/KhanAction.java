package model;

public class KhanAction implements GameAction {
	private final Province province;
	private final Khan khan;
	
	
	public KhanAction(Province province, Khan khan) {
		this.province = province;
		this.khan = khan;
	}
	

	@Override
	public DebugReturn execute(Player p, Node node) {
		if (!p.mustUseKhan()) return DebugReturn.MISSING_KHANMOVE;
		
		if (province == null) {
			if (khan.isAtUpgradesArea()) return DebugReturn.REPEAT_KHAN_POS;
			else khan.moveToUpgradesArea();
		} else {
			if (khan.isAtProvince(province)) return DebugReturn.REPEAT_KHAN_POS;

			khan.moveToProvince(province, node.getProvinces());
		}
		
		p.useKhan();
		
		return DebugReturn.SUCCESS;
	}

}
