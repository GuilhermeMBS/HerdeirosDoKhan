package view;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;

import controller.Controller;
import model.PlayerColor;

/**
 * Window only for testing the player board screen while the map screen
 * is not ready. It is not part of the game.
 *
 * @author Vinicius
 */
public class PlayerBoardDemo {

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.startGame(false);
        controller.setPlayerAmount(3);
        controller.setHeirPlayer(0, PlayerColor.BLUE, "Tolui");
        controller.setHeirPlayer(1, PlayerColor.RED, "Altani");
        controller.setHeirPlayer(2, PlayerColor.YELLOW, "Ogedei");

        PlayerBoardPanel boardPanel = new PlayerBoardPanel(controller);
        JButton endTurnButton = new JButton("Passar a vez");
        endTurnButton.addActionListener(new EndTurnListener(controller, boardPanel));

        JFrame frame = new JFrame("Teste da cartela");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(endTurnButton, BorderLayout.SOUTH);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
