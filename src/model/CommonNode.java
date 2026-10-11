package model;

import java.util.ArrayList;
import java.util.List;

/**
 * An intermediate topological class for nodes that reside within standard geographic regions
 * and follow basic occupancy and construction constraints.
 */
abstract class CommonNode extends Node {
    protected int currentOccupancy;
    protected List<Player> yurtOwners;
    protected String region;

    /**
     * Instantiates a common node tied to a specific geographic region.
     * 
     * @param region The name of the territory (e.g., "Russia", "China").
     */
    CommonNode(String region) {
        super();
        this.region = region;
        this.currentOccupancy = 0;
        this.yurtOwners = new ArrayList<>();
    }

    @Override
    boolean hasYurt() {
        return !this.yurtOwners.isEmpty();
    }

    @Override
    boolean buildYurt(Player player) {
        if (this.yurtOwners.size() < getMaxCapacity()) {
            this.yurtOwners.add(player);
            return true;
        }
        return false;
    }

    /**
     * Attempts to place a pawn on this node.
     * Validates against the specific maximum structural capacity of the derived class.
     * 
     * @return true if the pawn successfully enters the node.
     */
    boolean enterNode() {
        if (this.currentOccupancy < getMaxCapacity()) {
            this.currentOccupancy++;
            return true;
        }
        return false;
    }

    /**
     * Removes a pawn from this node, decreasing its internal occupancy counter.
     */
    void leaveNode() {
        if (this.currentOccupancy > 0) {
            this.currentOccupancy--;
        }
    }

    /**
     * Defines the maximum allowed concurrent occupants (pawns/yurts) for this specific node type.
     * 
     * @return The maximum numeric capacity.
     */
    abstract int getMaxCapacity();
}
