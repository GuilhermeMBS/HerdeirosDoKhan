package view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import controller.Controller;
import model.ColumnColor;

/**
 * Handles the clicks on the player board: a click on a free column starts
 * the turn with it. A click on an occupied column, outside the columns or
 * after a column was already chosen in this turn does nothing.
 *
 * @author Vinicius
 */
class ColumnClickListener extends MouseAdapter {
    private final PlayerBoardPanel panel;
    private final Controller controller;

    ColumnClickListener(PlayerBoardPanel panel, Controller controller) {
        this.panel = panel;
        this.controller = controller;
    }

    /**
     * Uses mousePressed instead of mouseClicked: mouseClicked is not called
     * when the mouse moves a little between pressing and releasing the button.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        ColumnColor column = panel.columnAt(e.getX(), e.getY());
        if (column == null) {
            return;
        }
        // Pawn 0 for now; choosing Altani's 2nd pawn will be done on the map
        if (controller.startTurn(column, 0)) {
            panel.repaint();
        }
    }
}
