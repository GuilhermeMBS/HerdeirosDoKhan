package model;

import java.util.List;

/**
 * Represents a single-capacity transit node adjacent to a City entity.
 * Delegates resource plunder and conquest mechanics to the associated City instance.
 */
class SingleNode extends CommonNode {
    protected City city;
    protected final int maxCapacity = 1;
    protected final List<TreasureType> treasures;

    SingleNode(String region, City city) {
        super(region);
        this.city = city != null ? city : new City("");
        this.treasures = this.city.getResourcesInternal();
    }

    SingleNode(String region, String cityName) {
        this(region, new City(cityName));
    }

    @Override
    int getMaxCapacity() {
        return this.maxCapacity;
    }

    City getCity() {
        return this.city;
    }

    String getCityName() {
        return this.city != null ? this.city.getName() : "";
    }

    TreasureType plunderTreasure(int index) {
        if (this.city == null) {
            return null;
        }
        if (!this.city.canBeSacked() && !this.treasures.isEmpty()) {
            return this.treasures.remove(index);
        }
        return this.city.removeResource(index);
    }

    boolean isConquered() {
        if (this.city == null) {
            return false;
        }
        boolean cityDominatedOrEmpty = (this.city.dominatedBy() != null) || this.treasures.isEmpty();
        return cityDominatedOrEmpty && this.hasYurt();
    }
}
