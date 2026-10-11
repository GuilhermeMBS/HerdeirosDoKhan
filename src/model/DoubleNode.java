package model;

/**
 * Represents a dual-capacity rest stop on the map.
 * Typically hosts an embedded TradingPost mechanism for exchanging loot.
 */
class DoubleNode extends CommonNode {
    protected TradingPost tradingPost;
    protected final int maxCapacity = 2;

    /**
     * Initializes a dual-capacity node containing a Trading Post.
     * 
     * @param region The geographic territory.
     * @param post   The TradingPost engine associated with this location.
     */
    DoubleNode(String region, TradingPost post) {
        super(region);
        this.tradingPost = post;
    }

    @Override
    int getMaxCapacity() {
        return this.maxCapacity;
    }

    TradingPost getTradingPost() {
        return this.tradingPost;
    }
}
