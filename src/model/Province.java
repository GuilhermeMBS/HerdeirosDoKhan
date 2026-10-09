package model;

class Province {
    protected int resourceCount;
    protected int khanPosition; 
    protected ResourceType resourceType;

    Province(ResourceType type) {
        this.resourceType = type;
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

    ResourceType getResourceType() {
        return this.resourceType;
    }
}
