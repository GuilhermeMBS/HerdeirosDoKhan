package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Geographic zone bordered by travel paths, capable of generating tributes.
 */
class Province {
    protected int resourceCount;
    protected int khanPosition;
    protected TributeType tributeType;
    private final List<Province> khanNeighbors = new ArrayList<>();

    Province(TributeType type) {
        this.tributeType = type;
        this.resourceCount = 1;
        this.khanPosition = 0;
    }

    boolean collectResource() {
        if (this.resourceCount > 0) {
            this.resourceCount--;
            return true;
        }
        return false;
    }

    void addResource() {
        if (this.resourceCount < 3) {
            this.resourceCount++;
        }
    }

    TributeType getTributeType() {
        return this.tributeType;
    }

    int getResourceCount() {
        return this.resourceCount;
    }

    void addKhanNeighbor(Province neighbor) {
        if (neighbor != null && neighbor != this && !this.khanNeighbors.contains(neighbor)) {
            this.khanNeighbors.add(neighbor);
        }
    }

    List<Province> getKhanNeighbors() {
        return Collections.unmodifiableList(this.khanNeighbors);
    }
}
