package model;

/**
 * Specialized representation for the capital city (Karakorum).
 * Accommodates all active players and enforces Tolui's exclusive construction rule.
 */
class CentralNode extends Node {
    protected TradingPost tradingPost;
    protected final int maxCapacity = 5;
    protected Player toluiYurt;

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

    @Override
    boolean enterNode() {
        return true;
    }

    @Override
    void leaveNode() {
        // Karakorum has unlimited capacity for all active pawns.
    }

    @Override
    boolean canBuildYurt(Player player) {
        if (player == null || this.toluiYurt != null) {
            return false;
        }
        return player.getHeir() == Heir.TOLUI && "Tolui".equalsIgnoreCase(player.getName());
    }

    @Override
    boolean buildYurt(Player player) {
        if (canBuildYurt(player)) {
            this.toluiYurt = player;
            return true;
        }
        return false;
    }

    TradingPost getTradingPost() {
        return this.tradingPost;
    }
}
