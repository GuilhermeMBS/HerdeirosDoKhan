package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A player's pawn on the map. Tracks its current node and all stops
 * visited during the active turn.
 */
class Pawn {
    private final PlayerColor color;
    private Node currentNode;
    private final List<Node> turnPath = new ArrayList<>();

    Pawn(PlayerColor color, Node startNode) {
        this.color = color;
        this.currentNode = startNode;
        if (startNode != null) {
            startNode.enterNode();
            this.turnPath.add(startNode);
        }
    }

    PlayerColor getColor() { return color; }
    Node getCurrentNode() { return currentNode; }

    /** Starts the turn: resets the path to contain only the current node. */
    void startTurn() {
        turnPath.clear();
        if (currentNode != null) {
            turnPath.add(currentNode);
        }
    }

    /**
     * Records that the pawn stopped at this node during its movement.
     */
    void moveTo(Node node) {
        if (node == null) {
            return;
        }
        currentNode = node;
        if (!turnPath.contains(node)) {
            turnPath.add(node);
        }
    }

    /**
     * Walks through the given stops in order and ends the movement at the last one.
     * The pawn may pass through full stops, but must fit in the final destination.
     */
    boolean walk(List<Node> stops) {
        if (stops == null || stops.isEmpty()) {
            return false;
        }
        Node destination = stops.get(stops.size() - 1);
        if (!destination.enterNode()) {
            return false;
        }
        if (currentNode != null) {
            currentNode.leaveNode();
        }
        if (turnPath.isEmpty() && currentNode != null) {
            turnPath.add(currentNode);
        }
        for (Node stop : stops) {
            moveTo(stop);
        }
        return true;
    }

    boolean hasVisited(Node node) {
        if (node == null) {
            return false;
        }
        if (turnPath.isEmpty()) {
            return node == currentNode;
        }
        return turnPath.contains(node);
    }

    List<Node> getTurnPath() {
        if (turnPath.isEmpty() && currentNode != null) {
            return Collections.singletonList(currentNode);
        }
        return Collections.unmodifiableList(turnPath);
    }

    void setTurnPath(List<Node> restoredPath) {
        turnPath.clear();
        if (restoredPath != null) {
            for (Node n : restoredPath) {
                if (n != null && !turnPath.contains(n)) {
                    turnPath.add(n);
                }
            }
        }
        if (turnPath.isEmpty() && currentNode != null) {
            turnPath.add(currentNode);
        }
    }
}
