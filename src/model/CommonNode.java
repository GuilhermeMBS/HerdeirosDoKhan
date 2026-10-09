package model;

abstract class CommonNode extends Node {
    protected int currentOccupancy;
    protected String region;

    CommonNode(String region) {
        super();
        this.region = region;
        this.currentOccupancy = 0;
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
