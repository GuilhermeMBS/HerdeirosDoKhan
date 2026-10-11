package model;

public class TradeAction implements GameAction {
	private final TradingPost post;
	
	
	public TradeAction(TradingPost post) {
		this.post = post;
	}

	@Override
	public DebugReturn execute(Player p, Node node) {
		int specificCost = 0;
		for (TreasureRequirements req : post.getRequirements()) {
			if (p.getTreasureAmount(req.getTreasureType()) < req.getAmount()) return DebugReturn.MISSING_RESOURCE;
			
			specificCost += req.getAmount();
		}
		
		int flexibleCost = post.getAnyTreasureCost();
		
		if (p.getTotalTreasures() < flexibleCost + specificCost) return DebugReturn.MISSING_RESOURCE;
		
		for (TreasureRequirements req : post.getRequirements()) 
			p.spendTreasure(req.getTreasureType(), req.getAmount());
		
		for (TreasureType type : TreasureType.values()) {
			int amount = Math.min(p.getTreasureAmount(type), flexibleCost);
			
			p.spendTreasure(type, amount);
			flexibleCost -= amount;
			
			if (flexibleCost == 0) break;
		}
		
		return DebugReturn.SUCCESS;
	}

}
