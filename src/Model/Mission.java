package model;


enum MissionType {
	YURT,
	YURTREGION,
	YURTINSEQUENCE,
	SWORD,
	EACHRESOURCE,
	TREASURE,
	CITIESDOMINATED,
	POWERUPS_LINE,
	POWERUPS_COL,
	POWERUPS_SQUARE,
	POINTS
}


class Mission {
	int target;
	MissionType type;
	int bonus;
	
	public Mission(int qtd, MissionType type, int bonus) {
		target = qtd;
		this.type = type;
		this.bonus = bonus;
	}
}
