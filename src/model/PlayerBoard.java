package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The individual player board: three colored columns and the white column.
 */
class PlayerBoard {
    static final int ACTIVATION_TOKENS = 4;

    private final Map<ColumnColor, Column> columns = new HashMap<>();
    private int rewardedRows = 0;

    PlayerBoard(List<Icon> red, List<Icon> green, List<Icon> yellow, List<Icon> white) {
        columns.put(ColumnColor.RED, new Column(ColumnColor.RED, red));
        columns.put(ColumnColor.GREEN, new Column(ColumnColor.GREEN, green));
        columns.put(ColumnColor.YELLOW, new Column(ColumnColor.YELLOW, yellow));
        columns.put(ColumnColor.WHITE, new Column(ColumnColor.WHITE, white));
    }

    Column getColumn(ColumnColor color) { return columns.get(color); }

    List<Upgrade> getAllUpgrades() {
        List<Upgrade> all = new ArrayList<>();
        for (ColumnColor c : ColumnColor.values()) {
            all.addAll(columns.get(c).getUpgrades());
        }
        return all;
    }

    int getTotalUpgradeCount() {
        int total = 0;
        for (ColumnColor c : ColumnColor.values()) {
            total += columns.get(c).getUpgradeCount();
        }
        return total;
    }

    boolean canActivate(ColumnColor color) {
        return !columns.get(color).isOccupied();
    }

    boolean activate(ColumnColor color) {
        if (!canActivate(color)) {
            return false;
        }
        if (color == ColumnColor.WHITE) {
            for (Column c : columns.values()) {
                c.free();
            }
        } else {
            columns.get(color).occupy();
        }
        return true;
    }

    /**
     * Forces a column to be occupied or free. Used only when loading a saved match.
     *
     * @param color the column to change
     * @param occupied true to occupy it, false to free it
     */
    void setOccupied(ColumnColor color, boolean occupied) {
        if (occupied) {
            columns.get(color).occupy();
        } else {
            columns.get(color).free();
        }
    }

    int availableTokens() {
        int occupied = 0;
        for (Column c : columns.values()) {
            if (c.isOccupied()) {
                occupied++;
            }
        }
        return ACTIVATION_TOKENS - occupied;
    }

    boolean addUpgrade(ColumnColor color, Upgrade upgrade) {
        if (color == ColumnColor.WHITE) {
            return false;
        }
        return columns.get(color).addUpgrade(upgrade);
    }

    int completeRows() {
        int smallest = Column.MAX_UPGRADES;
        for (ColumnColor c : ColumnColor.COLORED) {
            smallest = Math.min(smallest, columns.get(c).getUpgradeCount());
        }
        return smallest;
    }

    boolean hasCompletedSquare() {
        ColumnColor[] colors = ColumnColor.COLORED;

        for (int i = 0; i < colors.length - 1; i++) {
            int c1 = columns.get(colors[i]).getUpgradeCount();
            int c2 = columns.get(colors[i + 1]).getUpgradeCount();
            if (c1 > 1 && c2 > 1) {
                return true;
            }
        }

        int c1 = columns.get(colors[colors.length - 1]).getUpgradeCount();
        int c2 = columns.get(ColumnColor.WHITE).getUpgradeCount();
        return c1 > 1 && c2 > 1;
    }

    boolean hasFreeUpgradePending() {
        return completeRows() > rewardedRows;
    }

    boolean receiveFreeUpgrade(Upgrade upgrade) {
        if (!hasFreeUpgradePending()) {
            return false;
        }
        if (!columns.get(ColumnColor.WHITE).addUpgrade(upgrade)) {
            return false;
        }
        rewardedRows++;
        return true;
    }

    void endTurn() {
        for (Column c : columns.values()) {
            c.endTurn();
        }
    }
}
