package model;

/**
 * Upgrade bought with coins and placed on a column of the player board.
 * Example: "two horses" is {@code new Upgrade(Icon.HORSE, 2, ColumnColor.GREEN)}.
 *
 * @author Vinicius
 */
class Upgrade {
    private final Icon icon;
    private final int amount;
    private final ColumnColor color;

    Upgrade(Icon icon, int amount, ColumnColor color) {
        this.icon = icon;
        this.amount = amount;
        this.color = color;
    }

    Icon getIcon() { return icon; }
    int getAmount() { return amount; }
    ColumnColor getColor() { return color; }
}
