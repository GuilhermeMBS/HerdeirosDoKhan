package model;

class Province {
    protected int resourceCount;
    protected int khanPosition; 
    protected TributeType tributeType;

    Province(TributeType type) {
        this.tributeType = type;
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

    TributeType getTributeType() {
        return this.tributeType;
    }
}
