package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Transactional entity that exchanges a requested set of looted treasures for political votes.
 */
class TradingPost {
    protected int voteCount;
    protected boolean bonusAvailable; 
    protected int upgrade; 
    private final TreasureRequirements[] requiredTreasures;
    private final int anyAmount;

    /**
     * Constructs a trade contract.
     * 
     * @param votes           The generated political influence yield.
     * @param treasures       Specific typified requirements (e.g., 2 SILK).
     * @param anyTreasureCost The baseline cost in non-specific treasures (the "ANY" modifier).
     */
    TradingPost(int votes, TreasureRequirements[] treasures, int anyTreasureCost) {
        this.voteCount = votes;
        this.bonusAvailable = true; 
        this.upgrade = -1; 
        this.requiredTreasures = treasures;
        this.anyAmount = anyTreasureCost;
    }
    
    /**
     * Processes a proposed exchange against the node's internal contract.
     * 
     * @param offeredTreasures The array of treasures tendered by the player.
     * @return true if the offer matches or exceeds the threshold.
     */
    boolean processDelivery(TreasureRequirements[] offeredTreasures) {
        return validateTransaction(offeredTreasures);
    }

    /**
     * Validates the transaction logic. First deducts specifically requested items,
     * then verifies if the remaining surplus satisfies the generic 'anyAmount' requirement.
     */
    private boolean validateTransaction(TreasureRequirements[] offeredTreasures) {
        // Deep copy the offer to manipulate counts without modifying the original references
        List<TreasureRequirements> offerPool = new ArrayList<>();
        for (TreasureRequirements o : offeredTreasures) {
            offerPool.add(new TreasureRequirements(o.getTreasureType(), o.getAmount()));
        }

        // Validate specific requirements
        for (TreasureRequirements req : this.requiredTreasures) {
            boolean found = false;
            
            for (TreasureRequirements given : offerPool) {
                if (given.getTreasureType() == req.getTreasureType()) {
                    if (given.getAmount() < req.getAmount()) return false;
                    
                    // Deduct the used amount from the pool
                    given.setAmount(given.getAmount() - req.getAmount());
                    found = true;
                    break;
                }
            }
            if (!found && req.getAmount() > 0) return false;
        }
        
        // Validate generic "ANY" requirement
        int surplusSum = 0;
        for (TreasureRequirements given : offerPool) {
            surplusSum += given.getAmount();
        }
        
        return surplusSum >= this.anyAmount;
    }

    /**
     * Exhausts the initial-delivery bonus token if it exists.
     * 
     * @return true if the bonus was successfully claimed.
     */
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
        return this.requiredTreasures;
    }
    
    public int getAnyTreasureCost() {
        return this.anyAmount;
    }
}

/**
 * Data encapsulation class pairing a specific treasure type with a numeric quantity.
 */
class TreasureRequirements {
    private final TreasureType type;
    private int amount; // Non-final to allow runtime deductions during validation
    
    TreasureRequirements(TreasureType type, int qty) {
        this.amount = qty;
        this.type = type;
    }
    
    TreasureType getTreasureType() {
        return this.type;
    }
    
    int getAmount() {
        return this.amount;
    }
    
    void setAmount(int amount) {
        this.amount = amount;
    }
}
