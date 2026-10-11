package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a standard city on the board, restricted to single occupancy.
 * Stores a dynamic pool of lootable treasures.
 */
class SingleNode extends CommonNode {
    protected String city;
    protected final int maxCapacity = 1;
    protected List<TreasureType> treasures;

    /**
     * Initializes a city node.
     * 
     * @param region The geographic territory of the city.
     * @param city   The unique identifier name of the city.
     */
    SingleNode(String region, String city) {
        super(region);
        this.city = city;
        this.treasures = new ArrayList<>();
    }

    @Override
    int getMaxCapacity() {
        return this.maxCapacity;
    }

    String getCityName() {
        return this.city;
    }

    /**
     * Extracts a treasure from the city's stash based on an internal index.
     * 
     * @param index The zero-based position of the targeted treasure.
     * @return The removed TreasureType if found, or null if the index is out of bounds.
     */
    TreasureType plunderTreasure(int index) {
        if (index >= 0 && index < this.treasures.size()) {
            return this.treasures.remove(index);
        }
        return null;
    }

    /**
     * Determines the geopolitical subjugation status of the city.
     * A city falls when its treasury is empty AND a yurt has been established.
     * 
     * @return true if the conquest conditions are met.
     */
    boolean isConquered() {
        return this.treasures.isEmpty() && this.hasYurt();
    }
}
