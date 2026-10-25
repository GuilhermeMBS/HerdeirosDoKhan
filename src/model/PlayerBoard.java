package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The individual player board: three colored columns and the white column.
 *
 * @author Vinicius
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

    boolean canActivate(ColumnColor color) {
        return !columns.get(color).isOccupied();
    }

    /**
     * Places an activation token on the column.
     * Activating the white column takes back all the tokens.
     * @return {@code false} if the column is already occupied
     */
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

    int availableTokens() {
        int occupied = 0;
        for (Column c : columns.values()) {
            if (c.isOccupied()) {
                occupied++;
            }
        }
        return ACTIVATION_TOKENS - occupied;
    }

    /** Places a bought upgrade on one of the colored columns. */
    boolean addUpgrade(ColumnColor color, Upgrade upgrade) {
        if (color == ColumnColor.WHITE) {
            return false;
        }
        return columns.get(color).addUpgrade(upgrade);
    }

    /** Complete rows = smallest number of upgrades among the colored columns. */
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
    		int c2 = columns.get(colors[i+1]).getUpgradeCount();
    		
    		if ((c1 > 1 && c2 > 1)) return true;
    	}
    	
    	int c1 = columns.get(colors[colors.length - 1]).getUpgradeCount();
    	int c2 = columns.get(ColumnColor.WHITE).getUpgradeCount();
    	
    	if (c1 > 1 && c2 > 1) return true;
    	
    	return false;
    }

    /** Is there a complete row that has not given its free upgrade yet? */
    boolean hasFreeUpgradePending() {
        return completeRows() > rewardedRows;
    }

    /** Places the free upgrade on the white column (any color is accepted). */
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
