package model;

/**
 * Command wrapper executing a player action on a target node.
 */
class Action {
    private Node node;
    private GameAction action;
    private Player player;

    Action(Player p, Node n, GameAction action) {
        this.node = n;
        this.player = p;
        this.action = action;
    }

    boolean setAction(GameAction type) {
        this.action = type;
        return true;
    }

    boolean setNode(Node no) {
        this.node = no;
        return true;
    }

    boolean setPlayer(Player jog) {
        this.player = jog;
        return true;
    }

    private void resetInfo() {
        this.node = null;
        this.action = null;
        this.player = null;
    }

    DebugReturn doAction() {
        if (this.player == null) return DebugReturn.MISSING_PLAYER;
        if (this.action == null) return DebugReturn.MISSING_ACTION;
        if (this.node == null) return DebugReturn.MISSING_NODE;

        DebugReturn result = this.action.execute(this.player, this.node);
        if (result == DebugReturn.SUCCESS) {
            resetInfo();
        }
        return result;
    }
}
