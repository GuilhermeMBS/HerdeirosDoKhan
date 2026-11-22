package model;

/**
 * Transactional entity that exchanges a requested set of looted treasures for political votes.
 */
class TradingPost {
    protected int voteCount;
    protected boolean bonusAvailable; 
    protected int upgrade; 
    private final TreasureRequirements[] requiredTreasures;
    private final int anyAmount;
    
    private BonusCard card;

    /**
     * Constructs a trade contract.
     * 
     * @param votes           The generated political influence yield.
     * @param treasures       Specific typified requirements (e.g., 2 SILK).
     * @param anyTreasureCost The baseline cost in non-specific treasures (the "ANY" modifier).
     */
    TradingPost(int votes, TreasureRequirements[] treasures, int anyTreasureCost, BonusCard card) {
        this.voteCount = votes;
        this.bonusAvailable = true; 
        this.upgrade = -1; 
        this.requiredTreasures = treasures;
        this.anyAmount = anyTreasureCost;
        this.card = card;
    }

    /**
     * Exhausts the initial-delivery bonus token if it exists.
     * 
     * @return true if the bonus was successfully claimed.
     */
    boolean claimBonus(Player p) {
        if (!this.card.hasBeenUsed()) {
            this.card.assignPlayer(p);
            p.addBonusCard(card);
            this.bonusAvailable = false;
            return true;
        }
        return false;
    }
    
    public boolean isBonusAvailable() { return this.card.hasBeenClaimed(); }
    public String getBonusCardName() { return card.getDisplayName(); }
    public TreasureRequirements[] getRequirements() { return this.requiredTreasures; }
    public int getAnyTreasureCost() { return this.anyAmount; }
    
    public void setCard(BonusCard card) { this.card = card; }
    public void setBonusAvailable(boolean state) { this.bonusAvailable = state; }
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
