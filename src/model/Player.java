package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Represents one of the Khan's heirs, holding resources, pawns, and the player board.
 * Package-private to enforce information hiding outside the model package.
 */
class Player {
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

    Player() {
        this(PlayerColor.BLUE, Heir.TOLUI, null, 0);
    }

    Player(String name) {
        this(name, PlayerColor.BLUE, Heir.fromName(name), null, 0);
    }

    Player(PlayerColor color, Heir heir, Node startNode, int initialCoins) {
        this(heir != null ? heir.getDisplayName() : Heir.TOLUI.getDisplayName(), color, heir, startNode, initialCoins);
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

    String getName() { return name; }
    PlayerColor getColor() { return color; }
    Heir getHeir() { return heir; }
    PlayerBoard getBoard() { return board; }

    boolean canActivate(ColumnColor column) { return board.canActivate(column); }

    PrivateCounselor getSecretCard() { return secretCard; }
    void setSecretCard(PrivateCounselor card) { this.secretCard = card; }

    boolean startTurn(ColumnColor column, int pawnIndex) {
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

    void endTurn() {
        board.endTurn();
        movementsLeft = 0;
        handsLeft = 0;
        khanPending = false;
        for (TributeType t : TributeType.values()) {
            virtualTributes.put(t, 0);
        }
    }

    int getMovementsLeft() { return movementsLeft; }
    void setMovementsLeft(int steps) { this.movementsLeft = Math.max(0, steps); }

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

    boolean mustUseKhan() { return khanPending; }
    void useKhan() { khanPending = false; }

    int getPawnCount() { return pawns.size(); }
    Pawn getPawn(int index) { return pawns.get(index); }
    Pawn getActivePawn() { return activePawn; }
    Node getCurrentNode() { return activePawn.getCurrentNode(); }

    void moveTo(Node node) { activePawn.moveTo(node); }
    boolean hasVisited(Node node) { return activePawn.hasVisited(node); }
    List<Node> getTurnPath() { return activePawn.getTurnPath(); }

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

    List<Node> findPath(Node destination) {
        Node start = getCurrentNode();
        if (start == null || destination == null || destination == start) {
            return null;
        }

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
        LinkedList<Node> stops = new LinkedList<>();
        for (Node n = destination; n != start; n = previous.get(n)) {
            stops.addFirst(n);
        }
        return stops;
    }

    private Set<Node> stopsInOneMove(Node from) {
        if (from == null) {
            return Collections.emptySet();
        }
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

    int getTributeAmount(TributeType type) { return tributes.get(type); }
    int getVirtualTributeAmount(TributeType type) { return virtualTributes.get(type); }

    void addTribute(TributeType type, int qty) {
        if (qty > 0) {
            tributes.put(type, tributes.get(type) + qty);
        }
    }

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

    int getTreasureAmount(TreasureType type) { return treasures.get(type); }

    void addTreasure(TreasureType type, int qty) {
        if (type != null && qty > 0) {
            treasures.put(type, treasures.get(type) + qty);
        }
    }

    boolean spendTreasure(TreasureType type, int qty) {
        if (type == null || qty <= 0 || treasures.get(type) < qty) {
            return false;
        }
        treasures.put(type, treasures.get(type) - qty);
        return true;
    }

    int getTotalTreasures() {
        int total = 0;
        for (int qty : treasures.values()) {
            total += qty;
        }
        return total;
    }

    int getYurtsLeft() { return yurtsLeft; }
    int getAmountYurts() { return amountYurts; }

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

    boolean takeYurtFromStock() {
        if (yurtsLeft == 0) {
            return false;
        }
        yurtsLeft--;
        amountYurts++;
        return true;
    }

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
