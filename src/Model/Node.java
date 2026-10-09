package Model;

import java.util.ArrayList;
import java.util.List;

abstract class Node {
    protected List<Province> provinces;
    protected List<Node> neighbors;

    protected boolean hasYurt;

    Node() {
        this.provinces = new ArrayList<>();
        this.neighbors = new ArrayList<>();
    }
}

abstract class CommonNode extends Node {
    protected int currentOccupancy;
    protected String region; 
}

class SingleNode extends CommonNode {
    protected String city;
    protected final int maxCapacity = 1;
}

class DoubleNode extends CommonNode {
    protected TradingPost tradingPost;
    protected final int maxCapacity = 2;
}

class CentralNode extends Node {
    protected TradingPost tradingPost;
}

class Province {
    protected int resourceCount;
    protected int khan; // 0 for None, or 1, 2, 3, 4
}

class Khan {
    protected boolean isUpgrade;
    protected int position; // -1 (off map) or other position value
    protected Province[][] provinces = new Province[4][3];
}

class TradingPost {
    protected int voteCount;
    protected int upgrade; // -1 if already taken

    /**
     * Each index corresponds to one different resource and stores
     * it's quantity.
     */
    protected int[] resources = new int[6];
}
