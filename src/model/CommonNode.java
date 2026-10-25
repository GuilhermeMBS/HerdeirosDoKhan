package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Intermediate topological class for nodes that reside within standard geographic regions
 * and follow capacity-based occupancy and construction constraints.
 */
abstract class CommonNode extends Node {
    protected int currentOccupancy;
    protected List<Player> yurtOwners;
    protected String region;

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
    boolean hasYurtOf(Player player) {
        return this.yurtOwners.contains(player);
    }

    @Override
    boolean canBuildYurt(Player player) {
        return player != null && this.yurtOwners.size() < getMaxCapacity();
    }

    @Override
    boolean buildYurt(Player player) {
        if (canBuildYurt(player)) {
            this.yurtOwners.add(player);
            return true;
        }
        return false;
    }

    @Override
    boolean enterNode() {
        if (this.currentOccupancy < getMaxCapacity()) {
            this.currentOccupancy++;
            return true;
        }
        return false;
    }

    @Override
    void leaveNode() {
        if (this.currentOccupancy > 0) {
            this.currentOccupancy--;
        }
    }

    List<Player> getYurtOwners() {
        return Collections.unmodifiableList(this.yurtOwners);
    }

    String getRegion() {
        return this.region;
    }

    abstract int getMaxCapacity();
}
