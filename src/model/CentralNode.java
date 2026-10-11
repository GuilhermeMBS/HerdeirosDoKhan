package model;

class CentralNode extends Node {
    protected TradingPost tradingPost;
    protected final int maxCapacity = 5;
    
    // Alocação física restrita à anomalia da capital.
    protected Player toluiYurt; 

    CentralNode(boolean alternativeTreasureReq) {
        super();
        
        TreasureRequirements[] requisitosKarakorum;
        if (alternativeTreasureReq) {
        	requisitosKarakorum = new TreasureRequirements[] {
        			new TreasureRequirements(TreasureType.IRON, 1),
        			new TreasureRequirements(TreasureType.SILK, 1),
        			new TreasureRequirements(TreasureType.SPICE, 1)
        	};
        } else {
        	// Default
        	requisitosKarakorum = new TreasureRequirements[] {
        			new TreasureRequirements(TreasureType.FUR, 1),
        			new TreasureRequirements(TreasureType.IRON, 1),
        			new TreasureRequirements(TreasureType.ANIMAL, 1)
        	};        	
        }
        this.tradingPost = new TradingPost(2, requisitosKarakorum, 0);
        this.toluiYurt = null;
    }

    @Override
    boolean hasYurt() {
        return this.toluiYurt != null;
    }

    @Override
    boolean buildYurt(Player player) {
        // A validação encapsula a verificação da identidade do herdeiro.
        // A constante "Tolui" deve ser gerenciada pelas propriedades da classe Player.
        if (this.toluiYurt == null && player.getName().equals("Tolui")) {
            this.toluiYurt = player;
            return true;
        }
        return false;
    }
}
