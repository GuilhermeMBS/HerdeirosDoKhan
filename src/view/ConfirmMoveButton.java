package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import controller.Controller;
import model.ObservableIF;
import model.ObserverIF;

/**
 * "Confirm Move" button of the side panel.
 * Moving takes two steps: the player clicks a reachable node on the map (it only
 * becomes the selected destination) and then presses this button, which really
 * moves the pawn. The button observes the match, so it is only enabled while
 * there is a selected destination.
 *
 * @author Vinicius
 */
class ConfirmMoveButton extends JButton implements ObserverIF {
    private static final long serialVersionUID = 1L;

    private final Controller controller = Controller.getController();

    ConfirmMoveButton() {
        super("Confirm Move");
        addActionListener(new ConfirmMoveListener());
        controller.register(this);
        updateState();
    }

    @Override
    public void notify(ObservableIF o) {
        updateState();
    }

    /** Enabled only when there is a selected destination waiting for confirmation. */
    private void updateState() {
        setEnabled(controller.getPendingMoveNodeId() != null);
    }

    /** Moves the pawn to the selected destination when the button is pressed. */
    private class ConfirmMoveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            controller.confirmMove();
        }
    }
}
