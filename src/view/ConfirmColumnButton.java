package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import controller.Controller;
import model.ObservableIF;
import model.ObserverIF;

/**
 * "Confirm Column" button shown under the player board.
 * Choosing a column takes two steps: the player clicks a column (it only becomes
 * selected) and then presses this button, which really starts the turn.
 * The button observes the match, so it is only enabled while a column is selected.
 *
 * @author Vinicius
 */
class ConfirmColumnButton extends JButton implements ObserverIF {
    private static final long serialVersionUID = 1L;

    private final Controller controller = Controller.getController();

    ConfirmColumnButton() {
        super("Confirm Column");
        addActionListener(new ConfirmColumnListener());
        controller.register(this);
        updateState();
    }

    @Override
    public void notify(ObservableIF o) {
        updateState();
    }

    /** Enabled only when there is a selected column waiting for confirmation. */
    private void updateState() {
        setEnabled(controller.getPendingColumn() != null);
    }

    /** Starts the turn with the selected column when the button is pressed. */
    private class ConfirmColumnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            controller.confirmColumn();
        }
    }
}
