package model;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Manages the global deck of 40 treasure tokens (8 of each of the 5 types).
 */
class Resource {
    private static final ArrayList<TreasureType> resources = new ArrayList<>();
    private static final int QTY_PER_RESOURCE = 8;

    static {
        resetResources();
    }

    static void resetResources() {
        resources.clear();
        for (TreasureType type : TreasureType.values()) {
            for (int i = 0; i < QTY_PER_RESOURCE; i++) {
                resources.add(type);
            }
        }
        Collections.shuffle(resources);
    }

    static ArrayList<TreasureType> generateResources() {
        if (resources.size() < 4) {
            return null;
        }
        ArrayList<TreasureType> tmp = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            tmp.add(resources.remove(0));
        }
        return tmp;
    }
}
