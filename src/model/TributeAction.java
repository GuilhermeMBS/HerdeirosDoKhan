package model;

class TributeAction implements GameAction {
    private final Province province;

    TributeAction(Province p) {
        this.province = p;
    }

    @Override
    public DebugReturn execute(Player p, Node node) {
        if (this.province == null || this.province.getResourceCount() <= 0) {
            return DebugReturn.MISSING_TRIBUTE;
        }
        if (!p.useHand(1)) {
            return DebugReturn.EMPTY_HANDS;
        }
        this.province.collectResource();
        p.addTribute(this.province.getTributeType(), 1);
        return DebugReturn.SUCCESS;
    }
}
