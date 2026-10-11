package model;

/**
 * Geographic zone bordered by the travel paths, capable of generating passive tributes.
 */
class Province {
    protected int resourceCount;
    protected int khanPosition; 
    protected TributeType tributeType;

    /**
     * Initializes a province yielding a specific tribute.
     * 
     * @param type The enumerated type of the generated tribute.
     */
    Province(TributeType type) {
        this.tributeType = type;
        this.resourceCount = 1; 
        this.khanPosition = 0;
    }

    /**
     * Extracts a single tribute from the province.
     * 
     * @return true if successful; false if the province is barren.
     */
    boolean collectResource() {
        if (this.resourceCount > 0) {
            this.resourceCount--;
            return true;
        }
        return false;
    }

    /**
     * Increments the tribute count up to the strict mechanical limit of 3.
     */
    void addResource() {
        if (this.resourceCount < 3) {
            this.resourceCount++;
        }
    }

    TributeType getTributeType() {
        return this.tributeType;
    }
}
