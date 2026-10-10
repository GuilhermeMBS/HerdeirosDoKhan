package Model;


enum missionType {
	YURT,
	YURTREGION,
	YURTINSEQUENCE,
	SWORD,
	RESOURCES,
	CITIESDOMINATED,
	POWERUPS_LINE,
	POWERUPS_COL
}


class Mission {
	int target;
	missionType type;
	int bonus;
	
	public Mission(int qtd, missionType type, int bonus) {
		target = qtd;
		this.type = type;
		this.bonus = bonus;
	}
}
