package model;

class TradingPost {
    protected int voteCount;
    protected boolean bonusAvailable; 
    protected int upgrade; 
    private final TreasureRequirements[] requiredTreasures;
    private final int anyAmount;

    TradingPost(int votes, TreasureRequirements[] treasures, int anyTreasureCost) {
        this.voteCount = votes;
        this.bonusAvailable = true; 
        this.upgrade = -1; 
        requiredTreasures = treasures;
        anyAmount = anyTreasureCost;
    }
    

    boolean processDelivery(TreasureRequirements[] offeredTreasures) {
        if (validateTransaction(offeredTreasures)) {
            return true;
        }
        return false;
    }

    
    private boolean validateTransaction(TreasureRequirements[] offeredTreasures) {
    	for (TreasureRequirements req : requiredTreasures) {
    		boolean found = false;
    		
    		for (TreasureRequirements given : offeredTreasures) {
    			if (given.getTreasureType() == req.getTreasureType()) {
    				if (given.getAmount() < req.getAmount()) return false;
    				found = true;
    				break;
    			}
    		}
    		
    		if (!found && req.getAmount() > 0) return false;
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
    
    
    public TreasureRequirements[] getRequirements() {
    	return requiredTreasures;
    }
    
    
    public int getAnyTreasureCost() {
    	return anyAmount;
    }
}


class TreasureRequirements {
	private final TreasureType type;
	private final int amount;
	
	
	TreasureRequirements(TreasureType type, int qty) {
		amount = qty;
		this.type = type;
	}
	
	
	TreasureType getTreasureType() {
		return type;
	}
	
	
	int getAmount() {
		return amount;
	}
}
