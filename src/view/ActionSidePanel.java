package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import loader.GameStateLoader;

import model.GameData;
import controller.Controller;

/**
 * Lateral control panel providing board actions (harvest tribute, attack city,
 * build yurt, move Khan, trade at counselor, save game), dice simulation with
 * tester JComboBoxes (1-6), and visual indication of the active player's color.
 * Strictly avoids lambda expressions and modal dialog popups.
 */
public class ActionSidePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final GameData modelFacade;
    private Controller controller = Controller.getController();
    private final BoardPanel boardPanel;
    private final GameStateLoader stateLoader;
    private final Random randomGenerator;

    private final JComboBox<Integer> dieOneCombo;
    private final JComboBox<Integer> dieTwoCombo;
    private final JComboBox<String> treasureTradeCombo;
    private final JLabel statusFeedbackLabel;
    private final DiceDisplayCanvas diceCanvas;

    private int dieOneValue;
    private int dieTwoValue;
    private Color activePlayerColor;
    private String activePlayerName;

    /**
     * Constructs the lateral action panel.
     *
     * @param modelFacade Reference to the Model facade.
     * @param boardPanel  Reference to the Java2D board panel.
     * @param stateLoader Reference to the game state loader.
     */
    public ActionSidePanel(GameData modelFacade, BoardPanel boardPanel, GameStateLoader stateLoader) {
        this.modelFacade = modelFacade;
        this.boardPanel = boardPanel;
        this.stateLoader = stateLoader;
        this.randomGenerator = new Random();
        this.dieOneValue = 1;
        this.dieTwoValue = 1;
        this.activePlayerColor = new Color(200, 45, 45);
        this.activePlayerName = "Tolui";

        setPreferredSize(new Dimension(340, 760));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Turn indicator & Java2D Dice Display (painted with active player's color as in Fig. 4)
        this.diceCanvas = new DiceDisplayCanvas();
        add(this.diceCanvas);

        // 2. Tester Dice Selection (Two JComboBoxes 1..6 as recommended in the specification)
        JPanel diceControlPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        diceControlPanel.setBorder(BorderFactory.createTitledBorder("Movement / Dice Controls"));

        Integer[] diceFaces = new Integer[] {1, 2, 3, 4, 5, 6};
        this.dieOneCombo = new JComboBox<>(diceFaces);
        this.dieTwoCombo = new JComboBox<>(diceFaces);

        JButton rollRandomButton = new JButton("Roll Dice");
        JButton setTesterDiceButton = new JButton("Choose Dice");

        diceControlPanel.add(new JLabel("Die 1 (Test):"));
        diceControlPanel.add(this.dieOneCombo);
        diceControlPanel.add(new JLabel("Die 2 (Test):"));
        diceControlPanel.add(this.dieTwoCombo);
        diceControlPanel.add(rollRandomButton);
        diceControlPanel.add(setTesterDiceButton);
        add(diceControlPanel);

        // 3. Board Contextual Actions Panel
        JPanel actionsPanel = new JPanel(new GridLayout(7, 1, 6, 6));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Board Node & Province Actions"));

        JButton harvestButton = new JButton("Harvest Province Tribute");
        JButton attackCityButton = new JButton("Attack City (Plunder Treasure)");
        JButton buildYurtButton = new JButton("Build Yurt on Current Node");
        JButton moveKhanProvinceButton = new JButton("Move Khan to Selected Province");
        JButton moveKhanUpgradesButton = new JButton("Move Khan to Upgrades Area");

        this.treasureTradeCombo = new JComboBox<>(new String[] {
            "FUR + SILK (Counselor)",
            "FUR + IRON + ANIMAL (Karakorum Default)",
            "IRON + SILK + SPICE (Karakorum Alt)"
        });
        JButton executeTradeButton = new JButton("Execute Trade at Post");

        actionsPanel.add(harvestButton);
        actionsPanel.add(attackCityButton);
        actionsPanel.add(buildYurtButton);
        actionsPanel.add(moveKhanProvinceButton);
        actionsPanel.add(moveKhanUpgradesButton);
        actionsPanel.add(this.treasureTradeCombo);
        actionsPanel.add(executeTradeButton);
        add(actionsPanel);

        // 4. Turn & Persistence Controls
        JPanel systemPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        systemPanel.setBorder(BorderFactory.createTitledBorder("Turn & Match State"));

        JButton endTurnButton = new JButton("Next Player Turn");
        JButton saveGameButton = new JButton("Save Current Match");
        systemPanel.add(endTurnButton);
        systemPanel.add(saveGameButton);
        add(systemPanel);

        // 5. Non-intrusive Status Label (avoids JOptionPane popups)
        this.statusFeedbackLabel = new JLabel("Click a node on the map to move.");
        this.statusFeedbackLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        this.statusFeedbackLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        add(this.statusFeedbackLabel);

        bindActionListeners(
            rollRandomButton,
            setTesterDiceButton,
            harvestButton,
            attackCityButton,
            buildYurtButton,
            moveKhanProvinceButton,
            moveKhanUpgradesButton,
            executeTradeButton,
            endTurnButton,
            saveGameButton
        );
    }

    /**
     * Binds all button events using classic anonymous inner classes (strictly NO lambdas).
     */
    private void bindActionListeners(
        JButton rollRandomButton,
        JButton setTesterDiceButton,
        JButton harvestButton,
        JButton attackCityButton,
        JButton buildYurtButton,
        JButton moveKhanProvinceButton,
        JButton moveKhanUpgradesButton,
        JButton executeTradeButton,
        JButton endTurnButton,
        JButton saveGameButton
    ) {
        rollRandomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dieOneValue = randomGenerator.nextInt(6) + 1;
                dieTwoValue = randomGenerator.nextInt(6) + 1;
                dieOneCombo.setSelectedItem(Integer.valueOf(dieOneValue));
                dieTwoCombo.setSelectedItem(Integer.valueOf(dieTwoValue));
                diceCanvas.repaint();

                // [TEAMMATE INTEGRATION - CONTROLLER]: Notify controller of rolled movement points.
                statusFeedbackLabel.setText("Rolled: " + (dieOneValue + dieTwoValue) + " steps.");
            }
        });

        setTesterDiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer d1 = (Integer) dieOneCombo.getSelectedItem();
                Integer d2 = (Integer) dieTwoCombo.getSelectedItem();
                dieOneValue = d1 != null ? d1.intValue() : 1;
                dieTwoValue = d2 != null ? d2.intValue() : 1;
                diceCanvas.repaint();

                // [TEAMMATE INTEGRATION - CONTROLLER]: Set fixed tester movement steps in Controller.
                statusFeedbackLabel.setText("Tester Dice set: " + (dieOneValue + dieTwoValue) + " steps.");
            }
        });

        harvestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentNode = boardPanel.getSelectedNodeId();
                String targetProvince = boardPanel.getSelectedProvinceId();
                if (targetProvince == null) {
                    statusFeedbackLabel.setText("Click a province on the map first.");
                    return;
                }
                boolean success = controller.createTributeAction(currentNode, targetProvince);
                if (success) {
                    statusFeedbackLabel.setText("Harvested tribute from " + targetProvince);
                    boardPanel.repaint();
                } else {
                    statusFeedbackLabel.setText("Cannot harvest from " + targetProvince);
                }
            }
        });

        attackCityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeId = boardPanel.getSelectedNodeId();
                boolean loot = controller.createSackAction(nodeId, 0);
                if (loot) {
                    statusFeedbackLabel.setText("Plundered " + " from " + nodeId);
                    boardPanel.repaint();
                } else {
                    statusFeedbackLabel.setText("No plunderable treasure at " + nodeId);
                }
            }
        });

        buildYurtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeId = boardPanel.getSelectedNodeId();
                boolean built = modelFacade.placeYurtOnNode(modelFacade.getPlayerInd(activePlayerName), nodeId);
                if (built) {
                    statusFeedbackLabel.setText("Yurt built at " + nodeId + " by " + activePlayerName);
                    boardPanel.repaint();
                } else {
                    statusFeedbackLabel.setText("Yurt construction rejected at " + nodeId);
                }
            }
        });

        moveKhanProvinceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String provinceId = boardPanel.getSelectedProvinceId();
                if (provinceId != null && controller.createKhanAction(provinceId)) {
                    boardPanel.setActiveKhanProvinceId(provinceId);
                    statusFeedbackLabel.setText("Khan moved to " + provinceId + " (+1 cascade).");
                } else {
                    statusFeedbackLabel.setText("Select a valid province for the Khan.");
                }
            }
        });

        moveKhanUpgradesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	controller.createKhanAction("UPGRADES");
                boardPanel.repaint();
                statusFeedbackLabel.setText("Khan moved to Upgrades Area.");
            }
        });

        executeTradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeId = boardPanel.getSelectedNodeId();
                Map<String, Integer> offer = new HashMap<>();
                int selection = treasureTradeCombo.getSelectedIndex();
                if (selection == 0) {
                    offer.put("FUR", Integer.valueOf(1));
                    offer.put("SILK", Integer.valueOf(1));
                } else if (selection == 1) {
                    offer.put("FUR", Integer.valueOf(1));
                    offer.put("IRON", Integer.valueOf(1));
                    offer.put("ANIMAL", Integer.valueOf(1));
                } else {
                    offer.put("IRON", Integer.valueOf(1));
                    offer.put("SILK", Integer.valueOf(1));
                    offer.put("SPICE", Integer.valueOf(1));
                }

                // if (modelFacade.executeTrade(nodeId, offer)) {
                if (controller.createTradeAction(nodeId)) {
                    statusFeedbackLabel.setText("Trade completed at " + nodeId);
                } else {
                    statusFeedbackLabel.setText("Trade rejected at " + nodeId);
                }
            }
        });

        endTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // [TEAMMATE INTEGRATION - CONTROLLER]: Call controller.advanceToNextTurn()
                toggleDemoActivePlayer();
                diceCanvas.repaint();
                statusFeedbackLabel.setText("Turn passed to " + activePlayerName);
            }
        });

        saveGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean saved = stateLoader.saveMatchState("savegame.properties", modelFacade, boardPanel, activePlayerName);
                statusFeedbackLabel.setText(saved ? "Match saved to savegame.properties" : "Save failed.");
            }
        });
    }

    /**
     * Cycles active player color and name for visual turn demonstration.
     */
    private void toggleDemoActivePlayer() {
        if ("Tolui".equals(this.activePlayerName)) {
            this.activePlayerName = "Batu";
            this.activePlayerColor = new Color(45, 95, 200);
        } else {
            this.activePlayerName = "Tolui";
            this.activePlayerColor = new Color(200, 45, 45);
        }
    }

    /**
     * Inner Java2D canvas that paints the current player's color around the dice face images,
     * fulfilling Figure 4 of the Iteration 2 instructions.
     */
    private final class DiceDisplayCanvas extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Image[] diceSprites = new Image[7];

        DiceDisplayCanvas() {
            setPreferredSize(new Dimension(300, 115));
            for (int face = 1; face <= 6; face++) {
                this.diceSprites[face] = loadDieFaceImage(face);
            }
        }

        private Image loadDieFaceImage(int face) {
            try {
                File f = new File("assets/die_" + face + ".png");
                if (f.exists()) {
                    return ImageIO.read(f);
                }
            } catch (IOException ignored) {
                // Fallback generated below.
            }
            BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(2, 2, 60, 60, 14, 14);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(2, 2, 60, 60, 14, 14);
            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            g2.drawString(String.valueOf(face), 25, 40);
            g2.dispose();
            return img;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Paints background with active player's color (Iteration 2 requirement - Fig. 4)
            g2d.setColor(activePlayerColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("Current Turn: " + activePlayerName, 14, 22);

            // Draws the two dice images using Graphics2D.drawImage()
            g2d.drawImage(this.diceSprites[dieOneValue], 65, 34, 64, 64, this);
            g2d.drawImage(this.diceSprites[dieTwoValue], 155, 34, 64, 64, this);
        }
    }
}
