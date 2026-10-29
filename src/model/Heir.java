package model;

import java.util.Arrays;
import java.util.List;

/**
 * The five heirs. Every player board has the same three kinds of colored
 * column, each heir has them in different colors (see img/cartela*.jpg):
 * <ul>
 *   <li>khan column: horse, 2 hands, Khan</li>
 *   <li>move column: 2 horses, hand</li>
 *   <li>coin column: horse, hand, virtual coin</li>
 * </ul>
 * The white column is always: 2 horses, Khan, recover.
 *
 * @author Vinicius
 */
enum Heir {
    ALTANI(ColumnColor.RED, ColumnColor.GREEN, ColumnColor.YELLOW, 2),
    JOCHI(ColumnColor.GREEN, ColumnColor.YELLOW, ColumnColor.RED, 1),
    CHAGATAI(ColumnColor.RED, ColumnColor.YELLOW, ColumnColor.GREEN, 1),
    TOLUI(ColumnColor.YELLOW, ColumnColor.GREEN, ColumnColor.RED, 1),
    OGEDEI(ColumnColor.YELLOW, ColumnColor.RED, ColumnColor.GREEN, 1);

    private static final List<Icon> KHAN_COLUMN = Arrays.asList(Icon.HORSE, Icon.HAND, Icon.HAND, Icon.KHAN);
    private static final List<Icon> MOVE_COLUMN = Arrays.asList(Icon.HORSE, Icon.HORSE, Icon.HAND);
    private static final List<Icon> COIN_COLUMN = Arrays.asList(Icon.HORSE, Icon.HAND, Icon.VIRTUAL_COIN);
    private static final List<Icon> WHITE_COLUMN = Arrays.asList(Icon.HORSE, Icon.HORSE, Icon.KHAN, Icon.RECOVER);

    private final ColumnColor khanColumn;
    private final ColumnColor moveColumn;
//    private final ColumnColor coinColumn;
    private final int pawns;

    Heir(ColumnColor khanColumn, ColumnColor moveColumn, ColumnColor coinColumn, int pawns) {
        this.khanColumn = khanColumn;
        this.moveColumn = moveColumn;
//        this.coinColumn = coinColumn;
        this.pawns = pawns;
    }

    /** The heir's name as printed on the card, e.g. "Tolui". */
    public String getDisplayName() {
        String n = name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }

    /**
     * Finds the heir with the given name, ignoring upper/lower case.
     * @return the heir, or {@code null} if no heir has that name
     */
    public static Heir fromName(String name) {
        for (Heir h : values()) {
            if (h.name().equalsIgnoreCase(name)) {
                return h;
            }
        }
        return null;
    }

    /** Altani has 2 pawns; everybody else has 1. */
    int getPawnCount() { return pawns; }

    /** Creates a new player board with this heir's base actions. */
    PlayerBoard createBoard() {
        return new PlayerBoard(baseOf(ColumnColor.RED), baseOf(ColumnColor.GREEN),
                baseOf(ColumnColor.YELLOW), WHITE_COLUMN);
    }

    private List<Icon> baseOf(ColumnColor color) {
        if (color == khanColumn) return KHAN_COLUMN;
        if (color == moveColumn) return MOVE_COLUMN;
        return COIN_COLUMN;
    }
}
