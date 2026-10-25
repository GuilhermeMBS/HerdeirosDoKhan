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
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.Controller;
import loader.GameStateLoader;
import model.GameData;
import model.PlayerColor;

/**
 * Lateral control panel preserving the exact layout of Iteration 2
 * while integrating the PlayerBoardPanel and Controller.
 */
public class ActionSidePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final GameData modelFacade;
    private final Controller controller;
    private final BoardPanel boardPanel;
    private final PlayerBoardPanel playerBoardPanel;
    private final GameStateLoader stateLoader;
    private final Random randomGenerator;

    private final JComboBox<Integer> dieOneCombo;
    private final JComboBox<Integer> dieTwoCombo;
    private final JComboBox<String> treasureTradeCombo;
    private final JLabel statusFeedbackLabel;
    private final DiceDisplayCanvas diceCanvas;

    private int dieOneValue;
    private int dieTwoValue;

    public ActionSidePanel(
        GameData modelFacade,
        BoardPanel boardPanel,
        PlayerBoardPanel playerBoardPanel,
        GameStateLoader stateLoader
    ) {
        this.modelFacade = modelFacade;
        this.controller = Controller.getController();
        this.boardPanel = boardPanel;
        this.playerBoardPanel = playerBoardPanel;
        this.stateLoader = stateLoader;
        this.randomGenerator = new Random();
        this.dieOneValue = 1;
        this.dieTwoValue = 1;

        setPreferredSize(new Dimension(360, 760));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        this.diceCanvas = new DiceDisplayCanvas();
        add(this.diceCanvas);

        JPanel diceControlPanel = new JPanel(new GridLayout(3, 2, 4, 4));
        diceControlPanel.setBorder(BorderFactory.createTitledBorder("Movement / Dice Controls"));

        Integer[] diceFaces = new Integer[] { 1, 2, 3, 4, 5, 6 };
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

        JPanel actionsPanel = new JPanel(new GridLayout(7, 1, 4, 4));
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

        JPanel systemPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        systemPanel.setBorder(BorderFactory.createTitledBorder("Turn & Match State"));

        JButton endTurnButton = new JButton("Next Player Turn");
        JButton saveGameButton = new JButton("Save Current Match");
        systemPanel.add(endTurnButton);
        systemPanel.add(saveGameButton);
        add(systemPanel);

        this.statusFeedbackLabel = new JLabel("Click a column on the sheet or roll dice to move.");
        this.statusFeedbackLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        this.statusFeedbackLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
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

    public void refreshDisplay() {
        this.diceCanvas.repaint();
        if (this.playerBoardPanel != null) {
            this.playerBoardPanel.repaint();
        }
        this.boardPanel.repaint();
    }

    private Color resolveColor(PlayerColor playerColor) {
        if (playerColor == PlayerColor.RED) return new Color(200, 45, 45);
        if (playerColor == PlayerColor.GREEN) return new Color(30, 140, 60);
        if (playerColor == PlayerColor.YELLOW) return new Color(230, 190, 30);
        if (playerColor == PlayerColor.BLACK) return new Color(40, 40, 40);
        return new Color(45, 95, 200);
    }

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
                int steps = dieOneValue + dieTwoValue;
                controller.setTesterMovements(steps);
                refreshDisplay();
                statusFeedbackLabel.setText("Rolled: " + steps + " steps.");
            }
        });

        setTesterDiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer d1 = (Integer) dieOneCombo.getSelectedItem();
                Integer d2 = (Integer) dieTwoCombo.getSelectedItem();
                dieOneValue = d1 != null ? d1.intValue() : 1;
                dieTwoValue = d2 != null ? d2.intValue() : 1;
                int steps = dieOneValue + dieTwoValue;
                controller.setTesterMovements(steps);
                refreshDisplay();
                statusFeedbackLabel.setText("Tester Dice set: " + steps + " steps.");
            }
        });

        harvestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentNode = modelFacade.getPlayerNodeId(controller.getCurrentPlayerIndex());
                String targetProvince = boardPanel.getSelectedProvinceId();
                if (targetProvince == null) {
                    statusFeedbackLabel.setText("Click a province on the map first.");
                    return;
                }
                if (controller.createTributeAction(currentNode, targetProvince)) {
                    statusFeedbackLabel.setText("Harvested tribute from " + targetProvince);
                    refreshDisplay();
                } else {
                    statusFeedbackLabel.setText("Cannot harvest from " + targetProvince);
                }
            }
        });

        attackCityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeId = boardPanel.getSelectedNodeId();
                if (controller.createSackAction(nodeId, 0)) {
                    statusFeedbackLabel.setText("Plundered treasure from " + nodeId);
                    refreshDisplay();
                } else {
                    statusFeedbackLabel.setText("No plunderable treasure at " + nodeId);
                }
            }
        });

        buildYurtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeId = boardPanel.getSelectedNodeId();
                if (controller.createYurtAction(nodeId)) {
                    statusFeedbackLabel.setText("Yurt built at " + nodeId);
                    refreshDisplay();
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
                    statusFeedbackLabel.setText("Khan moved to " + provinceId);
                    refreshDisplay();
                } else {
                    statusFeedbackLabel.setText("Select a valid province for the Khan.");
                }
            }
        });

        moveKhanUpgradesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.createKhanAction("UPGRADES")) {
                    statusFeedbackLabel.setText("Khan moved to Upgrades Area.");
                    refreshDisplay();
                } else {
                    statusFeedbackLabel.setText("Khan move to Upgrades rejected.");
                }
            }
        });

        executeTradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeId = boardPanel.getSelectedNodeId();
                if (controller.createTradeAction(nodeId)) {
                    statusFeedbackLabel.setText("Trade completed at " + nodeId);
                    refreshDisplay();
                } else {
                    statusFeedbackLabel.setText("Trade rejected at " + nodeId);
                }
            }
        });

        endTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.endTurn();
                refreshDisplay();
                statusFeedbackLabel.setText("Turn passed to " + controller.getCurrentPlayerName());
            }
        });

        saveGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean saved = stateLoader.saveMatchState(
                    "savegame.properties",
                    modelFacade,
                    boardPanel,
                    controller.getCurrentPlayerName()
                );
                statusFeedbackLabel.setText(saved ? "Match saved to savegame.properties" : "Save failed.");
            }
        });
    }

    private final class DiceDisplayCanvas extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Image[] diceSprites = new Image[7];

        DiceDisplayCanvas() {
            setPreferredSize(new Dimension(320, 100));
            setMinimumSize(new Dimension(300, 95));
            for (int face = 1; face <= 6; face++) {
                this.diceSprites[face] = loadDieFaceImage(face);
            }
        }

        private Image loadDieFaceImage(int face) {
            BufferedImage loaded = BoardPanel.readProjectImage("assets/die_" + face + ".png");
            if (loaded != null) {
                return loaded;
            }
            BufferedImage img = new BufferedImage(58, 58, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(2, 2, 54, 54, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(2, 2, 54, 54, 12, 12);
            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2.drawString(String.valueOf(face), 22, 37);
            g2.dispose();
            return img;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            PlayerColor pColor = controller.getCurrentPlayerColor();
            g2d.setColor(resolveColor(pColor));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(pColor == PlayerColor.YELLOW ? Color.BLACK : Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString(
                "Current Turn: " + controller.getCurrentPlayerName() + " (Steps: " + controller.getMovementsLeft() + ")",
                14,
                22
            );

            g2d.drawImage(this.diceSprites[dieOneValue], 70, 30, 58, 58, this);
            g2d.drawImage(this.diceSprites[dieTwoValue], 160, 30, 58, 58, this);
        }
    }
}
