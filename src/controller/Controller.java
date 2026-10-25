package controller;


import java.io.*;


import model.ColumnColor;
import model.GameData;
import model.Heir;
import model.PlayerColor;

import loader.GameStateLoader;


public class Controller {	
	private int QTY_PLAYERS = 0;
	private int currentPlayer = 0;
	
	private boolean finalTurn = false;
	private boolean endRoundToFinalTurn = false;
	private boolean ended = false;
	
	private int totalVotes = 0;
	private int maxVotes;
	
	private boolean firstSackCity = true;

	// Column chosen on the player board in this turn (null = not chosen yet)
	private ColumnColor chosenColumn = null;

	private GameData data;
	
	private GameStateLoader state;
	
	private static Controller controller = null;
	
	
	private Controller() {
		
	}
	
	
	public static Controller getController() {
		if (controller == null) 
			controller = new Controller();
		return controller;
	}
	
	
	public boolean startGame(boolean alternativeKarakorum) {
		data = GameData.createData(alternativeKarakorum);
		return true;
	}
	
	
	public boolean setPlayerAmount(int playersQty) {
		if (playersQty < 2 || playersQty > 5) return false;
		
		QTY_PLAYERS = playersQty;
		data.createArrayPlayers(QTY_PLAYERS);
		
		if (QTY_PLAYERS == 2) maxVotes = 10;
		else if (QTY_PLAYERS == 3) maxVotes = 14;
		else if (QTY_PLAYERS == 4) maxVotes = 16;
		else maxVotes = 17;
		
		return true;
	}
	
	
	public boolean setHeirPlayer(int indPlayer, PlayerColor color, String heirName) {
		if (QTY_PLAYERS < 2 || data == null) return false;
		
		// First and second player get just 1 gold, the others get 2
		return data.assignPlayer(indPlayer, color, heirName, (indPlayer > 2) ? 2 : 1);
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
		return data.areAllCityConquered() || totalVotes >= maxVotes;
	}
	
	
	private void calculatePoints() {
		// Something
	}
	
	
	public boolean createMoveAction(String nodeId) {
		return data.MovePawn(currentPlayer, nodeId);
	}
	
	
	public boolean createYurtAction(String nodeId) {
		return data.placeYurtOnNode(currentPlayer, nodeId);
	}
	
	
	public boolean createSackAction(String cityName, int indTreasure) {
		if (indTreasure < 0 || indTreasure > 3) return false;
		
		boolean ret = data.attackCityForTreasure(currentPlayer, cityName, indTreasure, (firstSackCity) ? 1 : 2);
		firstSackCity = false;
		return ret;
	}
	
	
	public boolean createKhanAction(String provinceId) {
		return data.moveKhan(currentPlayer, provinceId);
	}
	
	
	public boolean createTributeAction(String nodeId, String provinceId) {
		return data.harvestProvinceResource(currentPlayer, nodeId, provinceId);
	}
	
	
	public boolean createTradeAction(String nodeId) {
		data.executeTrade(currentPlayer, nodeId);
		return true;
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

		data.endTurn(currentPlayer);
		chosenColumn = null;
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
	
	
	public PlayerColor getCurrentPlayerColor() { return data.getPlayerColor(currentPlayer); }


	/*
	 *
	 * Methods used by the player board screen (view.PlayerBoardPanel)
	 *
	 */

	/**
	 * Starts the turn of the current player with the column clicked on the player board.
	 * Only one column can be chosen per turn.
	 * @return {@code false} (and nothing changes) if a column was already chosen
	 * in this turn, the column is occupied or the pawn does not exist
	 */
	public boolean startTurn(ColumnColor column, int pawnIndex) {
		if (chosenColumn != null) return false;
		if (!data.startTurn(currentPlayer, column, pawnIndex)) return false;

		chosenColumn = column;
		return true;
	}


	public ColumnColor getChosenColumn() { return chosenColumn; }


	public boolean isColumnFree(ColumnColor column) { return data.isColumnFree(currentPlayer, column); }


	public Heir getCurrentHeir() { return data.getPlayerHeir(currentPlayer); }


	public int getMovementsLeft() { return data.getMovementsLeft(currentPlayer); }
	
	
	public boolean getGameHasEndend() { return ended; }
	
	
	public boolean getLastRound() { return finalTurn; }
	
	
	/*
	 * 
	 * Methods related to save/load the game state
	 * 
	 */
	
	private void setState(GameStateLoader s) {
		state = s;
	}
	
	
	public void loadSavedGame(File file) throws IOException {
		try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			Object o = in.readObject();
			if (!(o instanceof GameStateLoader)) {
				throw new IOException("The file does not contain a valid match");
			}
			setState((GameStateLoader) o);
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
