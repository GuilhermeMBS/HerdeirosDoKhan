package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import loader.GameStateLoader;

import model.GameData;

/**
 * Main application window housing the Java2D board canvas, the lateral action controls,
 * and the bottom container reserved for the teammate's player sheet panel (Cartela).
 * Enforces the maximum dimension constraint of 1400x1050 pixels.
 */
public class MainGameFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int MAX_ALLOWED_WIDTH = 1340;
    private static final int MAX_ALLOWED_HEIGHT = 920;

    private final BoardPanel boardPanel;
    private final ActionSidePanel actionSidePanel;

    /**
     * Constructs the main game window.
     *
     * @param modelFacade The initialized domain model facade.
     * @param stateLoader The persistence helper for saving/loading match states.
     */
    public MainGameFrame(GameData modelFacade, GameStateLoader stateLoader) {
        super("Heirs of the Khan - Board View");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(MAX_ALLOWED_WIDTH, MAX_ALLOWED_HEIGHT));
        setResizable(false);
        setLayout(new BorderLayout());

        this.boardPanel = new BoardPanel(modelFacade);
        this.actionSidePanel = new ActionSidePanel(modelFacade, this.boardPanel, stateLoader);

        add(this.boardPanel, BorderLayout.CENTER);
        add(this.actionSidePanel, BorderLayout.EAST);

        // [TEAMMATE INTEGRATION - PLAYER SHEET VIEW (CARTELA)]:
        // Replace this placeholder panel with the PlayerSheetPanel developed by your teammate.
        JPanel playerSheetPlaceholder = new JPanel();
        playerSheetPlaceholder.setPreferredSize(new Dimension(MAX_ALLOWED_WIDTH, 120));
        playerSheetPlaceholder.setBackground(new Color(240, 235, 225));
        playerSheetPlaceholder.setBorder(BorderFactory.createTitledBorder("Player Action Sheet (Teammate Module Integration Slot)"));
        playerSheetPlaceholder.add(new JLabel("Select action column and assign upgrades here."));
        add(playerSheetPlaceholder, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    public BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    /**
     * Application entry point. For Iteration 2, launches directly on the board window
     * as permitted by the specification, or can be invoked from InitialSetupFrame.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GameData facade = GameData.getData();
                GameStateLoader loader = new GameStateLoader();
                MainGameFrame frame = new MainGameFrame(facade, loader);
                frame.setVisible(true);
            }
        });
    }
}
