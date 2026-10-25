package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a city on the board that can be sacked for treasures and conquered.
 */
class City {
    private final String name;
    private boolean sackable = false;
    private Player conqueror = null;
    private final ArrayList<TreasureType> resources = new ArrayList<>();

    City(String name) {
        this.name = name;
    }

    boolean activateSacking() {
        this.sackable = true;
        this.resources.clear();
        List<TreasureType> generated = Resource.generateResources();
        if (generated != null) {
            this.resources.addAll(generated);
        }
        return true;
    }

    boolean canBeSacked() {
        return this.sackable;
    }

    TreasureType removeResource(int ind) {
        if (!this.sackable || this.resources.isEmpty() || ind < 0 || ind >= this.resources.size()) {
            return null;
        }
        return this.resources.remove(ind);
    }

    boolean defineDomination(Player player) {
        if (!(this.sackable && this.resources.isEmpty())) {
            return false;
        }
        this.conqueror = player;
        this.sackable = false;
        return true;
    }

    Player dominatedBy() {
        return this.conqueror;
    }

    String getName() {
        return this.name;
    }

    List<TreasureType> getResources() {
        return Collections.unmodifiableList(this.resources);
    }

    ArrayList<TreasureType> getResourcesInternal() {
        return this.resources;
    }
}
