package model;

/**
 * Colors of the action columns on the player board.
 * Upgrades also come in RED, GREEN and YELLOW.
 */
enum ColumnColor {
    RED, GREEN, YELLOW, WHITE;

    /** The three colored columns (all except WHITE). */
    static final ColumnColor[] COLORED = { RED, GREEN, YELLOW };
}
