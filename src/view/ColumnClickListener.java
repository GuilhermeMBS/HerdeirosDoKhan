package view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import controller.Controller;
import model.ColumnColor;

/**
 * Handles the clicks on the player board: a click on a free column only SELECTS it
 * (the turn starts when the player presses "Confirm Column"). A click on an occupied
 * column, outside the columns or after a column was already confirmed does nothing.
 * Clicking another free column changes the selection.
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
        // If the column can be selected, the observers are notified and the panel
        // redraws itself showing the selection; otherwise nothing happens.
        controller.selectColumn(column);
    }
}
