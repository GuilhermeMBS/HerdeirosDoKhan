package Model;

class Province {
    protected int resourceCount;
    protected int khanPosition; 
    protected TreasureType treasureType;

    Province(TreasureType type) {
        this.treasureType = type;
        this.resourceCount = 1; 
        this.khanPosition = 0;
    }

    boolean collectResource() {
        if (this.resourceCount > 0) {
            this.resourceCount--;
            return true;
        }
        return false;
    }

    void addResource() {
        if (this.resourceCount < 3) {
            this.resourceCount++;
        }
    }

    TreasureType getTreasureType() {
        return this.treasureType;
    }
}
