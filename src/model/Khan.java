package model;

import java.util.List;

/**
 * Autonomous game piece representing Genghis Khan.
 * Acts as a catalyst to generate tributes in specific map provinces or update the upgrade market.
 */
class Khan {
    protected boolean isAtUpgradesArea;
    protected Province currentProvince;

    Khan() {
        this.isAtUpgradesArea = false;
        this.currentProvince = null;
    }

    void moveToUpgradesArea() {
        this.isAtUpgradesArea = true;
        this.currentProvince = null;
    }

    boolean isAtProvince(Province province) {
        return this.currentProvince == province;
    }

    void moveToProvince(Province targetProvince, List<Province> adjacentProvinces) {
        this.isAtUpgradesArea = false;
        this.currentProvince = targetProvince;

        targetProvince.addResource();
        for (Province adjacent : adjacentProvinces) {
            adjacent.addResource();
        }
    }

    /**
     * Directly restores the Khan's location when loading a saved match
     * without triggering cascading resource generation.
     */
    void restoreLocation( boolean atUpgradesArea, Province province) {
        this.isAtUpgradesArea = atUpgradesArea;
        this.currentProvince = atUpgradesArea ? null : province;
    }

    boolean isAtUpgradesArea() {
        return this.isAtUpgradesArea;
    }
}
