package model;

class TradingPost {
    protected int voteCount;
    protected boolean bonusAvailable; 
    protected int upgrade; 
    protected int[] requiredTreasures; 

    TradingPost(int votes, int[] requirements) {
        this.voteCount = votes;
        this.requiredTreasures = requirements;
        this.bonusAvailable = true; 
        this.upgrade = -1; 
    }

    boolean processDelivery(int[] offeredTreasures) {
        if (validateTransaction(offeredTreasures)) {
            return true;
        }
        return false;
    }

    private boolean validateTransaction(int[] offeredTreasures) {
        for (int i = 0; i < this.requiredTreasures.length; i++) {
            if (offeredTreasures[i] < this.requiredTreasures[i]) {
                return false; 
            }
        }
        return true;
    }

    boolean claimBonus() {
        if (this.bonusAvailable) {
            this.bonusAvailable = false;
            return true;
        }
        return false;
    }

    void setUpgrade(int upgradeId) {
        this.upgrade = upgradeId;
    }

    int claimUpgrade() {
        int currentUpgrade = this.upgrade;
        this.upgrade = -1;
        return currentUpgrade;
    }
}
