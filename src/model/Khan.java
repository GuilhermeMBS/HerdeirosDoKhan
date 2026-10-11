package model;

import java.util.List;

/**
 * Autonomous game piece representing Genghis Khan.
 * Acts as a catalyst to generate tributes in specific map provinces or update the upgrade market.
 */
class Khan {
    protected boolean isAtUpgradesArea; 
    protected Province currentProvince;

    /**
     * Initializes the Khan in an off-board default state.
     */
    Khan() {
        this.isAtUpgradesArea = false;
        this.currentProvince = null;
    }

    /**
     * Relocates the Khan piece to the global Upgrades Market.
     */
    void moveToUpgradesArea() {
        this.isAtUpgradesArea = true;
        this.currentProvince = null;
    }
    
    /**
     * Verifies if the Khan is currently hovering over a specific geographic province.
     */
    boolean isAtProvince(Province province) {
        return this.currentProvince == province;
    }

    /**
     * Drops the Khan on a map province, triggering a cascading tribute yield 
     * on the target and its topologically adjacent neighbors.
     * 
     * @param targetProvince    The drop location.
     * @param adjacentProvinces The directional neighbors designated by the board's arrows.
     */
    void moveToProvince(Province targetProvince, List<Province> adjacentProvinces) {
        this.isAtUpgradesArea = false;
        this.currentProvince = targetProvince;

        targetProvince.addResource();
        for (Province adjacent : adjacentProvinces) {
            adjacent.addResource();
        }
    }

    boolean isAtUpgradesArea() {
        return this.isAtUpgradesArea;
    }
}
