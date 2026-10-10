package model;

import java.util.ArrayList;
import java.util.List;

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

    boolean hasYurt() {
        return !this.yurtOwners.isEmpty();
    }

    boolean buildYurt(Player player) {
        if (this.yurtOwners.size() < getMaxCapacity()) {
            this.yurtOwners.add(player);
            return true;
        }
        return false;
    }

    boolean enterNode() {
        if (this.currentOccupancy < getMaxCapacity()) {
            this.currentOccupancy++;
            return true;
        }
        return false;
    }

    void leaveNode() {
        if (this.currentOccupancy > 0) {
            this.currentOccupancy--;
        }
    }

    abstract int getMaxCapacity();
}
