package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract representation of a geographic node within the game board.
 * Defines the foundational topological constraints, tracking adjacent regions
 * and neighboring interconnected nodes.
 */
abstract class Node {
    protected List<Province> provinces;
    protected List<Node> neighbors;

    Node() {
        this.provinces = new ArrayList<>();
        this.neighbors = new ArrayList<>();
    }

    void addNeighbor(Node neighbor) {
        if (neighbor == null || neighbor == this) {
            return;
        }
        if (!this.neighbors.contains(neighbor)) {
            this.neighbors.add(neighbor);
        }
        if (!neighbor.neighbors.contains(this)) {
            neighbor.neighbors.add(this);
        }
    }

    List<Node> getNeighbors() {
        return Collections.unmodifiableList(this.neighbors);
    }

    void addProvince(Province province) {
        if (province != null && !this.provinces.contains(province)) {
            this.provinces.add(province);
        }
    }

    List<Province> getProvinces() {
        return Collections.unmodifiableList(this.provinces);
    }

    abstract boolean canBuildYurt(Player player);

    abstract boolean buildYurt(Player player);

    abstract boolean hasYurt();

    abstract boolean hasYurtOf(Player player);

    abstract boolean enterNode();

    abstract void leaveNode();
}
