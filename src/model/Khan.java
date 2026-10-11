package model;

import java.util.List;

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
