package Model;

class DoubleNode extends CommonNode {
    protected TradingPost tradingPost;
    protected final int maxCapacity = 2;

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
