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

    List<String> getUpgradeCodes() {
        List<String> codes = new ArrayList<>();
        for (Upgrade u : upgrades) {
            codes.add(u.getCode());
        }
        return codes;
    }

    boolean canReceive(Upgrade upgrade) {
        if (upgrade == null || upgrades.size() >= MAX_UPGRADES) {
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

    /**
     * Restores an upgrade when loading a saved match so it is immediately usable.
     */
    boolean restoreUpgrade(Upgrade upgrade) {
        if (!canReceive(upgrade)) {
            return false;
        }
        upgrades.add(upgrade);
        return true;
    }

    void clearUpgrades() {
        upgrades.clear();
        boughtThisTurn.clear();
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

    int countVirtualTreasure(TreasureType type) {
        if (type == null) {
            return 0;
        }
        int total = 0;
        for (Upgrade u : upgrades) {
            if (u.getIcon() == Icon.VIRTUAL_TREASURE
                && u.getVirtualTreasure() == type
                && !boughtThisTurn.contains(u)) {
                total += u.getAmount();
            }
        }
        return total;
    }

    void endTurn() {
        boughtThisTurn.clear();
    }
}
