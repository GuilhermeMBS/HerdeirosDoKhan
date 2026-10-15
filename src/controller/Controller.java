package controller;


import java.io.*;


import model.Heir;
import model.Map;
import model.Player;
import model.PlayerColor;
import model.Region;
import model.DebugReturn;

import model.Action;
import model.KhanAction;
import model.SackAction;
import model.MovementAction;
import model.TradeAction;
import model.TributeAction;
import model.YurtAction;

import loader.GameState;


public class Controller {
	private Map board = null;
	
	private int QTY_PLAYERS = 0;
	private Player[] players;
	private int currentPlayer = 0;
	
	private boolean finalTurn = false;
	private boolean endRoundToFinalTurn = false;
	private boolean ended = false;
	
	private int totalVotes = 0;
	private int maxVotes;
	
	private boolean firstSackCity = true;
	
	Action action;
	
	private GameState state;
	
	
	public boolean setPlayerAmount(int playersQty) {
		if (playersQty < 2 || playersQty > 5) return false;
		
		QTY_PLAYERS = playersQty;
		players = new Player[QTY_PLAYERS];
		
		if (QTY_PLAYERS == 2) maxVotes = 10;
		else if (QTY_PLAYERS == 3) maxVotes = 14;
		else if (QTY_PLAYERS == 4) maxVotes = 16;
		else maxVotes = 17;
		
		return true;
	}
	
	
	public boolean setboard(boolean alternativeKarakorum) {
		board = new Map(alternativeKarakorum);
		return true;
	}
	
	
	public boolean setHeir(int indPlayer, PlayerColor color, String heirName) {
		if (QTY_PLAYERS < 2 || board == null) return false;
		
		// First and second player get just 1 gold, the others get 2
		players[indPlayer] = new Player(color, Heir.fromName(heirName), board.getCentralNode(), (indPlayer > 2) ? 2 : 1);
		return true;
	}
	
	
	/*
	 * 
	 * Methods related to the player's turns
	 * 
	 */
	
	private void nextPlayer() {
		currentPlayer = (currentPlayer + 1) % QTY_PLAYERS;
		firstSackCity = true;
	}
	
	
	private boolean endGameConditionsMet() {
		return board.allCityConquered() || totalVotes >= maxVotes;
	}
	
	
	private void calculatePoints() {
		// Something
	}
	
	
	private boolean setActionParameters() {
		action.setNode(players[currentPlayer].getCurrentNode());
		action.setPlayer(players[currentPlayer]);
		return true;
	}
	
	
	public boolean createMoveAction() {
		if (!setActionParameters()) return false;
		
		action.setAction(new MovementAction(1));
		return true;
	}
	
	
	public boolean createYurtAction() {
		// Mudar o node para o node do Yurt mesmo (não o atual do jogador)
		if (!setActionParameters()) return false;
		
		action.setAction(new YurtAction());
		return true;
	}
	
	
	public boolean createSackAction(int indCity, int indTreasure) {
		// Mudar o node para o node do Yurt mesmo (não o atual do jogador)
		if (!setActionParameters()) return false;
		
		if (indCity < 0 || indCity >= board.getAmountCity()) return false;
		if (indTreasure < 0 || indTreasure > 3) return false;
		
		action.setAction(new SackAction(board.getCity(indCity), (firstSackCity) ? 1 : 2, indTreasure));
		return true;
	}
	
	
	public boolean createKhanAction(int indProvince) {
		if (!setActionParameters()) return false;
		
		if (indProvince >= board.getAmountProvinces()) return false;
		
		
		action.setAction(new KhanAction((indProvince < 0) ? null : board.getProvince(indProvince), board.getKhan()));
		return true;
	}
	
	
	public boolean createTributeAction(int indProvince) {
		if (!setActionParameters()) return false;
		
		if (indProvince < 0 || indProvince >= board.getAmountProvinces()) return false;
		
		action.setAction(new TributeAction(board.getProvince(indProvince)));
		return true;
	}
	
	
	public boolean createTradeAction(Region region, boolean firstTradePost) {
		if (!setActionParameters()) return false;
		
		action.setAction(new TradeAction(board.getTradingPost(region, firstTradePost)));
		return true;
	}
	
	
	public DebugReturn executeAction() {
		return action.doAction();
	}
	
	
	/**
	 * Method to end the current player action and
	 * pass to the next player 
	 * @return {@code true} if could pass to the next player,
	 * else {@code false} if the game has already finished
	 */
	public boolean endTurn() {
		if (ended) return false;
		
		if (endGameConditionsMet() && !endRoundToFinalTurn) endRoundToFinalTurn = true;
		
		nextPlayer();
		
		// You must finish the round, and then when the first player gets
		// a new turn, it starts the last round
		if (!finalTurn && currentPlayer == 0) finalTurn = true;
		else if (finalTurn && currentPlayer == 0) {
			ended = true;
			calculatePoints();
		}
		
		return true;
	}
	
	
	/*
	 * 
	 * Methods related to get info on the private variables
	 * 
	 */
	public int getTotalVotes() { return totalVotes; }
	
	
	public int getTotalPlayers() { return QTY_PLAYERS; }
	
	
	public PlayerColor getCurrentPlayerColor() { return players[currentPlayer].getColor(); }
	
	
	public boolean getGameHasEndend() { return ended; }
	
	
	public boolean getLastRound() { return finalTurn; }
	
	
	/*
	 * 
	 * Methods related to save/load the game state
	 * 
	 */
	
	private void setState(GameState s) {
		state = s;
	}
	
	
	public void loadSavedGame(File file) throws IOException {
		try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			Object o = in.readObject();
			if (!(o instanceof GameState)) {
				throw new IOException("The file does not contain a valid match");
			}
			setState((GameState) o);
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new IOException("The file does not contain a valid match");
		}
	}
	
	
	public void saveGameState(File file) throws IOException {
		if (state == null) throw new IOException("No match has been started");
		try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			out.writeObject(state);
		}
	}
}
