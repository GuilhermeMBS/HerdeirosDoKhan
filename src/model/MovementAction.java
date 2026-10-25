package model;

class MovementAction implements GameAction {
    MovementAction() {
    }

    MovementAction(int qty) {
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
