package model;

class SackAction implements GameAction {
    private final City city;
    private final int qtySword;
    private final int indTreasure;

    SackAction(City city, int qtySword, int indTreasure) {
        this.city = city;
        this.qtySword = qtySword;
        this.indTreasure = indTreasure;
    }

    @Override
    public DebugReturn execute(Player p, Node node) {
        if (this.city == null || !this.city.canBeSacked()) {
            return DebugReturn.CANT_SACK_CITY;
        }
        if (this.indTreasure < 0 || this.indTreasure >= this.city.getResources().size()) {
            return DebugReturn.CANT_SACK_CITY;
        }
        if (!p.spendTribute(TributeType.SWORD, this.qtySword)) {
            return DebugReturn.EMPTY_SWORDS;
        }

        TreasureType looted = this.city.removeResource(this.indTreasure);
        p.addTreasure(looted, 1);

        if (this.city.getResources().isEmpty() && this.city.defineDomination(p)) {
            p.takeYurtFromStock();
            p.registerConqueredCity();
        }

        return DebugReturn.SUCCESS;
    }
}
