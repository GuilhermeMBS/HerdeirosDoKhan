package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract representation of a geographic node within the game board.
 * Defines the foundational topological constraints, tracking adjacent regions
 * and neighboring interconnected nodes.
 */
abstract class Node {
    protected List<Province> provinces;
    protected List<Node> neighbors;

    /**
     * Initializes a structural node with empty neighborhood collections.
     */
    Node() {
        this.provinces = new ArrayList<>();
        this.neighbors = new ArrayList<>();
    }

    /**
     * Connects this node to an adjacent node on the topological map.
     * 
     * @param neighbor The adjacent Node instance to link.
     */
    void addNeighbor(Node neighbor) {
        if (!this.neighbors.contains(neighbor)) {
            this.neighbors.add(neighbor);
        }
    }

    /**
     * Associates this node with an overlapping or touching geographical province.
     * 
     * @param province The Province instance to link.
     */
    void addProvince(Province province) {
        if (!this.provinces.contains(province)) {
            this.provinces.add(province);
        }
    }

    /**
     * Retrieves the linked provinces, usually to calculate tribute extraction.
     * 
     * @return A list of the connected provinces.
     */
    List<Province> getProvinces() {
        return this.provinces;
    }

    /**
     * Evaluates if a given player is legally permitted to construct a Yurt at this node.
     * Must be implemented by concrete topology derivatives.
     * 
     * @param player The player attempting to construct.
     * @return true if the construction succeeds; false otherwise.
     */
    abstract boolean buildYurt(Player player);

    /**
     * Checks if this node contains at least one Yurt structure.
     * 
     * @return true if there is a yurt present.
     */
    abstract boolean hasYurt();
}
