package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One action column of the player board: the base actions printed
 * at the bottom plus up to 4 upgrades.
 */
class Column {
    static final int MAX_UPGRADES = 4;

    private final ColumnColor color;
    private final List<Icon> baseActions;
    private final List<Upgrade> upgrades = new ArrayList<>();
    private final List<Upgrade> boughtThisTurn = new ArrayList<>();
    private boolean occupied = false;

    Column(ColumnColor color, List<Icon> baseActions) {
        this.color = color;
        this.baseActions = new ArrayList<>(baseActions);
    }

    ColumnColor getColor() { return color; }
    boolean isWhite() { return color == ColumnColor.WHITE; }

    boolean isOccupied() { return occupied; }
    void occupy() { occupied = true; }
    void free() { occupied = false; }

    int getUpgradeCount() { return upgrades.size(); }

    List<Upgrade> getUpgrades() {
        return Collections.unmodifiableList(upgrades);
    }

    boolean canReceive(Upgrade upgrade) {
        if (upgrades.size() >= MAX_UPGRADES) {
            return false;
        }
        return isWhite() || upgrade.getColor() == color;
    }

    boolean addUpgrade(Upgrade upgrade) {
        if (!canReceive(upgrade)) {
            return false;
        }
        upgrades.add(upgrade);
        boughtThisTurn.add(upgrade);
        return true;
    }

    int count(Icon icon) {
        int total = 0;
        for (Icon i : baseActions) {
            if (i == icon) {
                total++;
            }
        }
        for (Upgrade u : upgrades) {
            if (u.getIcon() == icon && !boughtThisTurn.contains(u)) {
                total += u.getAmount();
            }
        }
        return total;
    }

    void endTurn() {
        boughtThisTurn.clear();
    }
}
