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

    @Override
    int getMaxCapacity() { return this.maxCapacity; }

    SingleNode(String region, String cityName) { this(region, new City(cityName)); }
    String getCityName() { return this.city != null ? this.city.getName() : ""; }
}
