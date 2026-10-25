package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import controller.Controller;
import loader.GameStateLoader;
import model.GameData;

/**
 * Main application window preserving the full 980x760 board aspect ratio
 * and full height for ActionSidePanel, while providing non-modal access
 * to the PlayerBoardPanel (Cartela).
 */
public class MainGameFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int WINDOW_WIDTH = 1340;
    private static final int WINDOW_HEIGHT = 860;

    private final BoardPanel boardPanel;
    private final PlayerBoardPanel playerBoardPanel;
    private final ActionSidePanel actionSidePanel;
    private final JDialog playerSheetDialog;

    public MainGameFrame(GameData modelFacade, GameStateLoader stateLoader) {
        super("Heirs of the Khan - Board View");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setResizable(true);
        setLayout(new BorderLayout());

        Controller controller = Controller.getController();
        if (controller.getTotalPlayers() == 0) {
            controller.setupMatch(Arrays.asList("Tolui", "Altani", "Ogedei"), false);
            modelFacade = controller.getGameData();
        }

        this.boardPanel = new BoardPanel(modelFacade);
        this.playerBoardPanel = new PlayerBoardPanel(controller);
        this.actionSidePanel = new ActionSidePanel(modelFacade, this.boardPanel, this.playerBoardPanel, stateLoader);

        this.boardPanel.setOnBoardStateChanged(new Runnable() {
            @Override
            public void run() {
                actionSidePanel.refreshDisplay();
            }
        });

        add(this.boardPanel, BorderLayout.CENTER);
        add(this.actionSidePanel, BorderLayout.EAST);

        // Non-modal floating window for the full-resolution PlayerBoardPanel (Cartela)
        this.playerSheetDialog = new JDialog(this, "Player Action Sheet (Cartela)", false);
        this.playerSheetDialog.setLayout(new BorderLayout());
        this.playerSheetDialog.add(this.playerBoardPanel, BorderLayout.CENTER);
        this.playerSheetDialog.pack();
        this.playerSheetDialog.setLocationRelativeTo(this);

        // Compact bottom bar identical in height to the original layout
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        bottomBar.setPreferredSize(new Dimension(WINDOW_WIDTH, 68));
        bottomBar.setBackground(new Color(240, 235, 225));
        bottomBar.setBorder(BorderFactory.createTitledBorder("Player Action Sheet (Cartela)"));

        JLabel hintLabel = new JLabel("Open the player board to activate an action column (RED, GREEN, YELLOW, WHITE):");
        JButton toggleSheetButton = new JButton("Open / Hide Player Sheet (Cartela)");
        toggleSheetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerSheetDialog.setVisible(!playerSheetDialog.isVisible());
                playerBoardPanel.repaint();
            }
        });

        bottomBar.add(hintLabel);
        bottomBar.add(toggleSheetButton);
        add(bottomBar, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    public BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Controller controller = Controller.getController();
                controller.setupMatch(Arrays.asList("Tolui", "Altani", "Ogedei"), false);
                GameStateLoader loader = new GameStateLoader();
                MainGameFrame frame = new MainGameFrame(controller.getGameData(), loader);
                frame.setVisible(true);
            }
        });
    }
}
