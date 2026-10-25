package model;

/**
 * Specialized representation for the capital city (Karakorum).
 * Accommodates all active players and enforces strict localized construction rules.
 */
class CentralNode extends Node {
    protected TradingPost tradingPost;
    protected final int maxCapacity = 5;
    
    protected Player toluiYurt;

    /**
     * Initializes the central hub with a specialized Trading Post.
     * 
     * @param alternativeTreasureReq Flag to switch between default and randomized setup conditions.
     */
    CentralNode(boolean alternativeTreasureReq) {
        super();
        
        TreasureRequirements[] karakorumRequirements;
        if (alternativeTreasureReq) {
            karakorumRequirements = new TreasureRequirements[] {
                    new TreasureRequirements(TreasureType.IRON, 1),
                    new TreasureRequirements(TreasureType.SILK, 1),
                    new TreasureRequirements(TreasureType.SPICE, 1)
            };
        } else {
            karakorumRequirements = new TreasureRequirements[] {
                    new TreasureRequirements(TreasureType.FUR, 1),
                    new TreasureRequirements(TreasureType.IRON, 1),
                    new TreasureRequirements(TreasureType.ANIMAL, 1)
            };          
        }
        
        this.tradingPost = new TradingPost(2, karakorumRequirements, 0);
        this.toluiYurt = null;
    }

    @Override
    boolean hasYurt() {
        return this.toluiYurt != null;
    }

    @Override
    boolean hasYurtOf(Player player) {
        return this.toluiYurt == player;
    }

    /**
     * All the pawns of the game fit in Karakorum, so a pawn can always enter.
     */
    @Override
    boolean enterNode() {
        return true;
    }

    @Override
    void leaveNode() {
        // Nothing to do: Karakorum does not count its pawns.
    }

    /**
     * Validates yurt placement in the capital. 
     * Exceptionally hardcoded to reject all players except the designated heir "Tolui".
     */
    @Override
    boolean buildYurt(Player player) {
        if (this.toluiYurt == null && player.getName().equals("Tolui")) {
            this.toluiYurt = player;
            return true;
        }
        return false;
    }
    
    
    TradingPost getTradingPost() { return tradingPost; }
}
