package model;

class CentralNode extends Node {
    protected TradingPost tradingPost;
    protected final int maxCapacity = 5;
    
    // Alocação física restrita à anomalia da capital.
    protected Player toluiYurt; 

    CentralNode() {
        super();
        int[] requisitosKarakorum = {1, 1, 1, 0, 0, 0}; 
        this.tradingPost = new TradingPost(2, requisitosKarakorum);
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
