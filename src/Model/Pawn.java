package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A player's pawn on the map. Keeps its current node and the nodes
 * it stopped at during the current turn.
 *
 * @author Vinicius
 */
class Pawn {
    private final PlayerColor color;
    private Node currentNode;
    private final List<Node> turnPath = new ArrayList<>();

    Pawn(PlayerColor color, Node startNode) {
        this.color = color;
        this.currentNode = startNode;
    }

    PlayerColor getColor() { return color; }
    Node getCurrentNode() { return currentNode; }

    /** Starts the turn: the path only contains the current node. */
    void startTurn() {
        turnPath.clear();
        turnPath.add(currentNode);
    }

    /**
     * Records that the pawn stopped at this node. Nodes skipped
     * through the player's own yurts must NOT be passed here.
     */
    void moveTo(Node node) {
        currentNode = node;
        turnPath.add(node);
    }

    boolean hasVisited(Node node) { return turnPath.contains(node); }

    List<Node> getTurnPath() {
        return Collections.unmodifiableList(turnPath);
    }
}
