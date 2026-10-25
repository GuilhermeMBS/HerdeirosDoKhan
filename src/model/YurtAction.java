package model;

class YurtAction implements GameAction {
    @Override
    public DebugReturn execute(Player p, Node node) {
        if (!node.canBuildYurt(p)) {
            return DebugReturn.NODE_FULL_YURT;
        }
        if (!p.payForYurt()) {
            return DebugReturn.EMPTY_HOUSES;
        }
        node.buildYurt(p);
        return DebugReturn.SUCCESS;
    }
}
