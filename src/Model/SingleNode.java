package Model;

import java.util.ArrayList;
import java.util.List;

class SingleNode extends CommonNode {
    protected String city;
    protected final int maxCapacity = 1;
    protected List<TreasureType> treasures;

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

    TreasureType plunderTreasure(int index) {
        if (index >= 0 && index < this.treasures.size()) {
            return this.treasures.remove(index);
        }
        return null;
    }

    boolean isConquered() {
        return this.treasures.isEmpty() && this.hasYurt();
    }
}
