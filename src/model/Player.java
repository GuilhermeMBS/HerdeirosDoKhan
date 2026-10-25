package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * One of the Khan's heirs.
 *
 * <p>Keeps the player's resources (tributes, treasures, yurts, votes),
 * the player board and the pawns. A player may have more than one pawn
 * (Altani), but controls only one per turn: the active pawn.</p>
 *
 * <p>A turn goes like this:</p>
 * <ol>
 *   <li>{@link #startTurn(ColumnColor, int)} activates a column and loads
 *       its movements, hands and virtual tributes;</li>
 *   <li>actions use {@link #useMovement(int)}, {@link #useHand(int)},
 *       {@link #spendTribute(TributeType, int)}, {@link #payForYurt()}...;</li>
 *   <li>{@link #endTurn()} discards whatever was not used.</li>
 * </ol>
 *
 * @author Vinicius
 * @version 1.1
 */
public class Player {
    static final int INITIAL_YURTS = 12;
    static final int UPGRADE_COST = 2;

    private final String name;
    private final PlayerColor color;
    private final Heir heir;
    private final PlayerBoard board;
    private PrivateCounselor secretCard;

    private final Map<TributeType, Integer> tributes = new HashMap<>();
    private final Map<TributeType, Integer> virtualTributes = new HashMap<>();
    private final Map<TreasureType, Integer> treasures = new HashMap<>();
    private int yurtsLeft = INITIAL_YURTS;
    private int amountYurts = 0;
    private int votes = 0;
    private int conqueredCities = 0;
    private int bonuses = 0;

    private final List<Pawn> pawns = new ArrayList<>();
    private Pawn activePawn;

    private int movementsLeft = 0;
    private int handsLeft = 0;
    private boolean khanPending = false;

    /**
     * Default player (blue, Tolui, no starting node, no coins).
     * Useful for tests that only need some player.
     */
    Player() {
        this(PlayerColor.BLUE, Heir.TOLUI, null, 0);
    }

    /**
     * Player identified only by a name (blue, no starting node, no coins).
     * If the name is an heir ("Tolui", "Altani"...), that heir is used;
     * otherwise the default board (Tolui's) is used.
     * Useful for tests that only need some named player.
     */
    Player(String name) {
        this(name, PlayerColor.BLUE, Heir.fromName(name), null, 0);
    }

    /**
     * @param color the pawn color
     * @param heir the chosen heir (defines the board and the number of pawns)
     * @param startNode where the pawns start (Karakorum)
     * @param initialCoins 1 for the first two players, 2 for the others
     */
    public Player(PlayerColor color, Heir heir, Node startNode, int initialCoins) {
        this(heir.getDisplayName(), color, heir, startNode, initialCoins);
    }

    private Player(String name, PlayerColor color, Heir heir, Node startNode, int initialCoins) {
        if (heir == null) {
            heir = Heir.TOLUI;
        }
        this.name = name;
        this.color = color;
        this.heir = heir;
        this.board = heir.createBoard();
        for (int i = 0; i < heir.getPawnCount(); i++) {
            pawns.add(new Pawn(color, startNode));
        }
        this.activePawn = pawns.get(0);
        for (TributeType t : TributeType.values()) {
            tributes.put(t, 0);
            virtualTributes.put(t, 0);
        }
        for (TreasureType t : TreasureType.values()) {
            treasures.put(t, 0);
        }
        tributes.put(TributeType.COIN, initialCoins);
    }

    /** The player's name (the heir's name, e.g. "Tolui"). */
    String getName() { return name; }

    public PlayerColor getColor() { return color; }
    public Heir getHeir() { return heir; }
    PlayerBoard getBoard() { return board; }

    /** The column has no activation token, so it can be chosen this turn. */
    public boolean canActivate(ColumnColor column) { return board.canActivate(column); }

    PrivateCounselor getSecretCard() { return secretCard; }
    void setSecretCard(PrivateCounselor card) { this.secretCard = card; }

    // ---------------------------------------------------------------- turn

    /**
     * Starts the turn: chooses the pawn, activates the column and loads
     * the movements, hands and virtual tributes of that column.
     * @return {@code false} (and nothing changes) if the pawn does not
     * exist or the column is occupied
     */
    public boolean startTurn(ColumnColor column, int pawnIndex) {
        if (pawnIndex < 0 || pawnIndex >= pawns.size()) {
            return false;
        }
        if (!board.activate(column)) {
            return false;
        }
        activePawn = pawns.get(pawnIndex);
        activePawn.startTurn();

        Column c = board.getColumn(column);
        movementsLeft = c.count(Icon.HORSE);
        handsLeft = c.count(Icon.HAND);
        khanPending = c.count(Icon.KHAN) > 0;
        virtualTributes.put(TributeType.COIN, c.count(Icon.VIRTUAL_COIN));
        virtualTributes.put(TributeType.SWORD, c.count(Icon.VIRTUAL_SWORD));
        virtualTributes.put(TributeType.YURT, c.count(Icon.VIRTUAL_YURT));
        return true;
    }

    /** Discards unused movements, hands and virtual tributes. */
    public void endTurn() {
        board.endTurn();
        movementsLeft = 0;
        handsLeft = 0;
        khanPending = false;
        for (TributeType t : TributeType.values()) {
            virtualTributes.put(t, 0);
        }
    }

    public int getMovementsLeft() { return movementsLeft; }
    int getHandsLeft() { return handsLeft; }

    boolean useMovement(int qty) {
        if (qty <= 0 || movementsLeft < qty) {
            return false;
        }
        movementsLeft -= qty;
        return true;
    }

    boolean useHand(int qty) {
        if (qty <= 0 || handsLeft < qty) {
            return false;
        }
        handsLeft -= qty;
        return true;
    }

    /** The column has the Khan icon and the Khan was not moved yet. */
    boolean mustUseKhan() { return khanPending; }
    void useKhan() { khanPending = false; }

    // --------------------------------------------------------- pawns / path

    int getPawnCount() { return pawns.size(); }
    Pawn getPawn(int index) { return pawns.get(index); }
    Pawn getActivePawn() { return activePawn; }

    /** Node of the active pawn. */
    public Node getCurrentNode() { return activePawn.getCurrentNode(); }

    /** Moves the active pawn. Nodes skipped through own yurts must not be passed. */
    void moveTo(Node node) { activePawn.moveTo(node); }

    boolean hasVisited(Node node) { return activePawn.hasVisited(node); }

    List<Node> getTurnPath() { return activePawn.getTurnPath(); }

    /**
     * Moves the active pawn to the destination, following the game rules:
     * <ul>
     *   <li>each horse moves the pawn to an adjacent stop;</li>
     *   <li>the pawn can jump over the player's own yurts;</li>
     *   <li>the pawn can pass through full stops, but must fit in the destination.</li>
     * </ul>
     * Only the horses needed are spent; the others stay for later in the turn.
     * @return {@code false} (and nothing changes) if the move is not allowed
     */
    boolean moveActivePawn(Node destination) {
        List<Node> stops = findPath(destination);
        if (stops == null) {
            return false;
        }
        if (!activePawn.walk(stops)) {
            return false;
        }
        useMovement(stops.size());
        return true;
    }

    /**
     * Finds the shortest way (fewest horses) from the active pawn to the
     * destination, using at most the movements left in this turn.
     * It is a breadth-first search: first it finds every stop reachable with
     * 1 horse, then with 2 horses, and so on.
     * @return the stops on the way, in order and ending at the destination
     * (one stop per horse), or {@code null} if the destination cannot be reached
     */
    List<Node> findPath(Node destination) {
        Node start = getCurrentNode();
        if (start == null || destination == null || destination == start) {
            return null;
        }

        // horsesUsed.get(n): how many horses it takes to reach n
        // previous.get(n): the stop the pawn was at right before n
        Map<Node, Integer> horsesUsed = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        Queue<Node> toVisit = new ArrayDeque<>();

        horsesUsed.put(start, 0);
        toVisit.add(start);
        while (!toVisit.isEmpty()) {
            Node stop = toVisit.poll();
            int used = horsesUsed.get(stop);
            if (used == movementsLeft) {
                continue;
            }
            for (Node next : stopsInOneMove(stop)) {
                if (!horsesUsed.containsKey(next)) {
                    horsesUsed.put(next, used + 1);
                    previous.put(next, stop);
                    toVisit.add(next);
                }
            }
        }

        if (!horsesUsed.containsKey(destination)) {
            return null;
        }
        // Walks backwards from the destination to rebuild the way
        LinkedList<Node> stops = new LinkedList<>();
        for (Node n = destination; n != start; n = previous.get(n)) {
            stops.addFirst(n);
        }
        return stops;
    }

    /**
     * Stops the active pawn can reach from {@code from} using only one horse:
     * its neighbors and, when a neighbor has a yurt of this player, also the
     * stops behind that yurt (the pawn jumps over it).
     */
    private Set<Node> stopsInOneMove(Node from) {
        Set<Node> stops = new HashSet<>();
        addStopsInOneMove(from, stops);
        return stops;
    }

    private void addStopsInOneMove(Node from, Set<Node> stops) {
        for (Node neighbor : from.getNeighbors()) {
            if (stops.contains(neighbor)) {
                continue;
            }
            stops.add(neighbor);
            if (neighbor.hasYurtOf(this)) {
                addStopsInOneMove(neighbor, stops);
            }
        }
    }

    // ------------------------------------------------------------ tributes

    /** Real tributes only (without the virtual ones of the turn). */
    int getTributeAmount(TributeType type) { return tributes.get(type); }

    int getVirtualTributeAmount(TributeType type) { return virtualTributes.get(type); }

    void addTribute(TributeType type, int qty) {
        if (qty > 0) {
            tributes.put(type, tributes.get(type) + qty);
        }
    }

    /**
     * Spends tributes, using the virtual ones of the turn first.
     * @return {@code false} (and nothing changes) if there are not enough
     */
    boolean spendTribute(TributeType type, int qty) {
        int virtual = virtualTributes.get(type);
        int real = tributes.get(type);
        if (qty <= 0 || virtual + real < qty) {
            return false;
        }
        int fromVirtual = Math.min(virtual, qty);
        virtualTributes.put(type, virtual - fromVirtual);
        tributes.put(type, real - (qty - fromVirtual));
        return true;
    }

    // ----------------------------------------------------------- treasures

    int getTreasureAmount(TreasureType type) { return treasures.get(type); }

    void addTreasure(TreasureType type, int qty) {
        if (qty > 0) {
            treasures.put(type, treasures.get(type) + qty);
        }
    }

    boolean spendTreasure(TreasureType type, int qty) {
        if (qty <= 0 || treasures.get(type) < qty) {
            return false;
        }
        treasures.put(type, treasures.get(type) - qty);
        return true;
    }

    /** Used to break ties: most treasures not delivered wins. */
    int getTotalTreasures() {
        int total = 0;
        for (int qty : treasures.values()) {
            total += qty;
        }
        return total;
    }

    // --------------------------------------------------------------- yurts

    int getYurtsLeft() { return yurtsLeft; }
    
    
    public int getAmountYurts() { return amountYurts;}

    
    /**
     * Pays for building a yurt: 1 yurt tribute and 1 yurt piece from the stock.
     * @return {@code false} (and nothing changes) if one of them is missing
     */
    boolean payForYurt() {
        if (yurtsLeft == 0) {
            return false;
        }
        if (!spendTribute(TributeType.YURT, 1)) {
            return false;
        }
        yurtsLeft--;
        amountYurts++;
        return true;
    }

    /** Takes a yurt from the stock without paying (e.g. after conquering a city). */
    boolean takeYurtFromStock() {
        if (yurtsLeft == 0) {
            return false;
        }
        yurtsLeft--;
        return true;
    }

    // ------------------------------------------------------------ upgrades

    /**
     * Buys an upgrade for {@link #UPGRADE_COST} coins and places it
     * on a colored column. The upgrade can only be used next turn.
     * @return {@code false} (and nothing changes) if the column cannot
     * receive it or the player does not have the coins
     */
    boolean buyUpgrade(ColumnColor column, Upgrade upgrade) {
        if (column == ColumnColor.WHITE || !board.getColumn(column).canReceive(upgrade)) {
            return false;
        }
        if (!spendTribute(TributeType.COIN, UPGRADE_COST)) {
            return false;
        }
        return board.addUpgrade(column, upgrade);
    }

    boolean hasFreeUpgradePending() { return board.hasFreeUpgradePending(); }

    boolean receiveFreeUpgrade(Upgrade upgrade) { return board.receiveFreeUpgrade(upgrade); }

    // -------------------------------------------------- votes and conquests

    int getVotes() { return votes; }

    void addVotes(int qty) {
        if (qty > 0) {
            votes += qty;
        }
    }

    int getConqueredCities() { return conqueredCities; }
    void registerConqueredCity() { conqueredCities++; }

    int getBonuses() { return bonuses; }
    void registerBonus() { bonuses++; }
}
