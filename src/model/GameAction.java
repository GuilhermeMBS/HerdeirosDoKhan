package model;

public interface GameAction {
	abstract DebugReturn execute(Player p, Node node);
}
