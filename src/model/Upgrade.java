package model;

/**
 * Upgrade bought with coins (or earned for free upon completing a row)
 * and placed on a column of the player board.
 * Each upgrade carries its asset code (e.g., "CAV-A", "MOE-R", "BOI-A")
 * so the View can draw the exact image and the Loader can serialize it to plain ASCII text.
 */
class Upgrade {
    private final String code;
    private final Icon icon;
    private final int amount;
    private final ColumnColor color;
    private final TreasureType virtualTreasure;

    Upgrade(Icon icon, int amount, ColumnColor color) {
        this(buildFallbackCode(icon, color), icon, amount, color, null);
    }

    Upgrade(String code, Icon icon, int amount, ColumnColor color) {
        this(code, icon, amount, color, null);
    }

    Upgrade(String code, Icon icon, int amount, ColumnColor color, TreasureType virtualTreasure) {
        this.code = code;
        this.icon = icon;
        this.amount = amount;
        this.color = color;
        this.virtualTreasure = virtualTreasure;
    }

    String getCode() { return code; }
    Icon getIcon() { return icon; }
    int getAmount() { return amount; }
    ColumnColor getColor() { return color; }
    TreasureType getVirtualTreasure() { return virtualTreasure; }

    private static String buildFallbackCode(Icon icon, ColumnColor color) {
        String prefix = "CAV";
        if (icon == Icon.HAND) prefix = "MAO";
        else if (icon == Icon.VIRTUAL_COIN) prefix = "MOE";
        else if (icon == Icon.VIRTUAL_SWORD) prefix = "ESP";
        else if (icon == Icon.VIRTUAL_YURT) prefix = "CAS";
        else if (icon == Icon.VIRTUAL_TREASURE) prefix = "BOI";

        String suffix = "A";
        if (color == ColumnColor.RED) suffix = "R";
        else if (color == ColumnColor.GREEN) suffix = "V";
        return prefix + "-" + suffix;
    }
}
