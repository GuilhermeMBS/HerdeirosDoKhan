package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import controller.Controller;

/**
 * Handles the "end turn" button: passes the turn to the next player
 * and redraws the player board with the new player's board.
 *
 * @author Vinicius
 */
class EndTurnListener implements ActionListener {
    private final Controller controller;
    private final PlayerBoardPanel panel;

    EndTurnListener(Controller controller, PlayerBoardPanel panel) {
        this.controller = controller;
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        controller.endTurn();
        panel.repaint();
    }
}
