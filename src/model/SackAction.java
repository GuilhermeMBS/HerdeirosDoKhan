package model;

public class SackAction implements GameAction {
	private final City city;
	private final int qtySword;
	private final int indTreasure;
	
	
	public SackAction(City city, int qtySword, int indTreasure) {
		this.city = city;
		this.qtySword = qtySword;
		this.indTreasure = indTreasure;
	}

	
	@Override
	public DebugReturn execute(Player p, Node node) {
		if (!p.spendTribute(TributeType.SWORD, qtySword)) return DebugReturn.EMPTY_SWORDS;
		
		if (!city.canBeSacked()) return DebugReturn.CANT_SACK_CITY;
		
		p.addTreasure(city.removeResource(indTreasure), 1);
		
		return DebugReturn.SUCCESS;
	}

}
