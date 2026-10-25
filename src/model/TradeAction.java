package model;

class TradeAction implements GameAction {
    private final TradingPost post;

    TradeAction(TradingPost post) {
        this.post = post;
    }

    @Override
    public DebugReturn execute(Player p, Node node) {
        if (this.post == null) {
            return DebugReturn.MISSING_ACTION;
        }

        int specificCost = 0;
        for (TreasureRequirements req : this.post.getRequirements()) {
            if (p.getTreasureAmount(req.getTreasureType()) < req.getAmount()) {
                return DebugReturn.MISSING_RESOURCE;
            }
            specificCost += req.getAmount();
        }

        int flexibleCost = this.post.getAnyTreasureCost();
        if (p.getTotalTreasures() < flexibleCost + specificCost) {
            return DebugReturn.MISSING_RESOURCE;
        }

        for (TreasureRequirements req : this.post.getRequirements()) {
            p.spendTreasure(req.getTreasureType(), req.getAmount());
        }

        for (TreasureType type : TreasureType.values()) {
            int amount = Math.min(p.getTreasureAmount(type), flexibleCost);
            if (amount > 0) {
                p.spendTreasure(type, amount);
                flexibleCost -= amount;
            }
            if (flexibleCost == 0) {
                break;
            }
        }

        p.addVotes(this.post.voteCount);
        if (this.post.claimBonus()) {
            p.registerBonus();
        }

        return DebugReturn.SUCCESS;
    }
}
