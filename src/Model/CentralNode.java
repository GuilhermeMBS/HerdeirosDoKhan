package Model;

class CentralNode extends Node {
    protected TradingPost tradingPost;
    protected final int maxCapacity = 5;

    CentralNode() {
        super();
        this.tradingPost = new TradingPost();
    }

    @Override
    boolean buildYurt(Player player) {
        return false;
    }
}
