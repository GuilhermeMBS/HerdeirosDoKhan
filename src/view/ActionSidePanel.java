package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.Controller;
import loader.GameStateLoader;
import model.GameData;
import model.ObservadoIF;
import model.ObservadorIF;
import model.PlayerColor;
import model.TreasureType;

/**
 * Lateral control panel integrating the Controller, observing match state changes,
 * and rendering the active player's full inventory, secret counselor status, and scoreboard.
 */
public class ActionSidePanel extends JPanel implements ObservadorIF {

    private static final long serialVersionUID = 1L;

    private final Controller controller;
    private final BoardPanel boardPanel;
    private final GameStateLoader stateLoader;

    private final JComboBox<String> cityTreasureSlotCombo;
    private final JComboBox<String> treasureTradeCombo;
    private final JLabel statusFeedbackLabel;
    private final InventoryDisplayPanel inventoryPanel;

    private int movementsLeft;

    public ActionSidePanel(BoardPanel boardPanel, GameStateLoader stateLoader) {
        this.controller = Controller.getController();
        this.boardPanel = boardPanel;
        this.stateLoader = stateLoader;

        setPreferredSize(new Dimension(360, 760));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(30, 8, 6, 8));

        JPanel actionsPanel = new JPanel(new GridLayout(8, 1, 3, 3));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Board Node & Province Actions"));

        JButton harvestButton = new JButton("Harvest Province Tribute");
        this.cityTreasureSlotCombo = new JComboBox<>(new String[] {
            "Plunder Slot 1 (Top-Left)",
            "Plunder Slot 2 (Top-Right)",
            "Plunder Slot 3 (Bottom-Left)",
            "Plunder Slot 4 (Bottom-Right)"
        });
        JButton attackCityButton = new JButton("Attack City (Plunder Selected Slot)");
        JButton buildYurtButton = new JButton("Build Yurt on Visited Node");
        JButton moveKhanButton = new JButton("Move Khan");
        JButton openCounselorButton = new JButton("See Counselors (Open & Secret)");

        this.treasureTradeCombo = new JComboBox<>(new String[] {
            "FUR + SILK (Counselor)",
            "FUR + IRON + ANIMAL (Karakorum Default)",
            "IRON + SILK + SPICE (Karakorum Alt)"
        });
        JButton executeTradeButton = new JButton("Execute Trade at Post");

        actionsPanel.add(harvestButton);
        actionsPanel.add(this.cityTreasureSlotCombo);
        actionsPanel.add(attackCityButton);
        actionsPanel.add(buildYurtButton);
        actionsPanel.add(moveKhanButton);
        actionsPanel.add(openCounselorButton);
        actionsPanel.add(this.treasureTradeCombo);
        actionsPanel.add(executeTradeButton);
        add(actionsPanel);

        add(Box.createVerticalStrut(4));

        this.inventoryPanel = new InventoryDisplayPanel();
        add(this.inventoryPanel);

        add(Box.createVerticalStrut(4));

        JPanel systemPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        systemPanel.setBorder(BorderFactory.createTitledBorder("Turn & Match State"));

        JButton endTurnButton = new JButton("Next Player Turn");
        JButton saveGameButton = new JButton("Save Current Match");
        systemPanel.add(endTurnButton);
        systemPanel.add(saveGameButton);
        add(systemPanel);

        this.statusFeedbackLabel = new JLabel("Click a column on the sheet to move.");
        this.statusFeedbackLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        this.statusFeedbackLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(this.statusFeedbackLabel);

        bindActionListeners(
            harvestButton,
            attackCityButton,
            buildYurtButton,
            moveKhanButton,
            openCounselorButton,
            executeTradeButton,
            endTurnButton,
            saveGameButton
        );

        this.movementsLeft = controller.getMovementsLeft();
        this.controller.registra(this);
    }

    @Override
    public void notify(ObservadoIF o) {
        this.movementsLeft = o.get(GameData.MOVEMENTS_LEFT);
        this.inventoryPanel.repaint();
        repaint();
    }

    private Color resolveColor(PlayerColor playerColor) {
        if (playerColor == PlayerColor.RED) return new Color(200, 45, 45);
        if (playerColor == PlayerColor.GREEN) return new Color(30, 140, 60);
        if (playerColor == PlayerColor.YELLOW) return new Color(230, 190, 30);
        if (playerColor == PlayerColor.BLACK) return new Color(40, 40, 40);
        return new Color(45, 95, 200);
    }

    private void bindActionListeners(
        JButton harvestButton,
        JButton attackCityButton,
        JButton buildYurtButton,
        JButton moveKhanButton,
        JButton openCounselorButton,
        JButton executeTradeButton,
        JButton endTurnButton,
        JButton saveGameButton
    ) {
        harvestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String targetProvince = boardPanel.getSelectedProvinceId();
                if (targetProvince == null) {
                    statusFeedbackLabel.setText("Click a province on the map first.");
                    return;
                }
                if (controller.createTributeAction(targetProvince)) {
                    statusFeedbackLabel.setText("Harvested tribute from " + targetProvince);
                } else {
                    statusFeedbackLabel.setText("Cannot harvest from " + targetProvince);
                }
            }
        });

        attackCityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeId = boardPanel.getSelectedNodeId();
                int slotIndex = Math.max(0, cityTreasureSlotCombo.getSelectedIndex());
                if (controller.createSackAction(nodeId, slotIndex)) {
                    statusFeedbackLabel.setText("Plundered treasure (slot " + (slotIndex + 1) + ") at " + nodeId);
                } else {
                    statusFeedbackLabel.setText("Cannot plunder slot " + (slotIndex + 1) + " at " + nodeId);
                }
            }
        });

        buildYurtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeId = boardPanel.getSelectedNodeId();
                if (controller.createYurtAction(nodeId)) {
                    statusFeedbackLabel.setText("Yurt built at " + nodeId);
                } else {
                    statusFeedbackLabel.setText("Yurt construction rejected at " + nodeId);
                }
            }
        });

        moveKhanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> provinces = controller.getKhanProvincesIds();
                KhanFrame khanFrame = new KhanFrame(provinces);
                khanFrame.setVisible(true);
            }
        });

        openCounselorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenCounselorFrame counselors = new OpenCounselorFrame();
                counselors.setVisible(true);
            }
        });

        executeTradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nodeId = boardPanel.getSelectedNodeId();
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
                controller.endTurn();
                if (controller.getGameHasEnded()) {
                    statusFeedbackLabel.setText("MATCH ENDED! Check Scoreboard above.");
                } else {
                    statusFeedbackLabel.setText("Turn passed to " + controller.getCurrentPlayerName());
                }
            }
        });

        saveGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean saved = stateLoader.saveMatchState(
                    "savegame.properties",
                    boardPanel,
                    controller.getCurrentPlayerName()
                );
                statusFeedbackLabel.setText(saved ? "Match saved to savegame.properties" : "Save failed.");
            }
        });
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
            "Current Turn: " + controller.getCurrentPlayerName() + " (Steps: " + movementsLeft + ")",
            14,
            22
        );
    }

    /**
     * Custom Java2D panel displaying the active player's tributes, virtual tributes,
     * plundered treasures, secret counselor status, and live match scoreboard.
     */
    private final class InventoryDisplayPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private InventoryDisplayPanel() {
            setPreferredSize(new Dimension(340, 265));
            setBackground(new Color(245, 240, 230));
            setBorder(BorderFactory.createTitledBorder("Resources, Secret Mission & Scoreboard"));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(35, 30, 25));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));

            int xLeft = 12;
            int xRight = 175;
            int y = 28;
            int rowStep = 17;

            int hands = controller.getHandsLeft();
            boolean khanPending = controller.isKhanPending();
            int totalScore = controller.getCurrentPlayerTotalScore();
            int yurtsStock = controller.getCurrentPlayerYurtsLeft();

            g2.drawString("Hands Left: " + hands, xLeft, y);
            g2.drawString("Khan Action: " + (khanPending ? "READY" : "None"), xRight, y);
            y += rowStep;

            g2.drawString("Total Score: " + totalScore + " pts", xLeft, y);
            g2.drawString("Yurts in Stock: " + yurtsStock, xRight, y);
            y += rowStep;

            int coins = controller.getCurrentPlayerCoins();
            int vCoins = controller.getCurrentPlayerVirtualCoins();
            int swords = controller.getCurrentPlayerSwords();
            int vSwords = controller.getCurrentPlayerVirtualSwords();
            int yurts = controller.getCurrentPlayerYurtTributes();
            int vYurts = controller.getCurrentPlayerVirtualYurts();

            g2.setColor(new Color(145, 95, 10));
            g2.drawString("Coins: " + coins + (vCoins > 0 ? " (+" + vCoins + "v)" : ""), xLeft, y);
            g2.setColor(new Color(165, 35, 35));
            g2.drawString("Swords: " + swords + (vSwords > 0 ? " (+" + vSwords + "v)" : ""), xRight, y);
            y += rowStep;

            g2.setColor(new Color(40, 110, 60));
            g2.drawString("Yurt Tributes: " + yurts + (vYurts > 0 ? " (+" + vYurts + "v)" : ""), xLeft, y);
            g2.setColor(new Color(55, 30, 85));
            g2.drawString("Secret: " + controller.getCurrentPlayerPrivateCounselorName(), xRight, y);
            y += rowStep + 2;

            g2.setColor(new Color(50, 40, 90));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String treasuresLine1 = "FUR: " + controller.getCurrentPlayerTreasure(TreasureType.FUR)
                + " | SILK: " + controller.getCurrentPlayerTreasure(TreasureType.SILK)
                + " | IRON: " + controller.getCurrentPlayerTreasure(TreasureType.IRON)
                + " | ANIMAL: " + controller.getCurrentPlayerTreasure(TreasureType.ANIMAL)
                + " | SPICE: " + controller.getCurrentPlayerTreasure(TreasureType.SPICE);
            g2.drawString(treasuresLine1, xLeft, y);
            y += rowStep + 2;

            // Draw Scoreboard section
            g2.setColor(new Color(160, 145, 125));
            g2.drawLine(xLeft, y - 10, getWidth() - 12, y - 10);

            g2.setColor(new Color(30, 30, 30));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("Match Scoreboard:", xLeft, y);
            y += rowStep - 2;

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            List<String> standings = controller.getScoreboardSummary();
            for (String line : standings) {
                g2.drawString(line, xLeft, y);
                y += 14;
            }

            g2.dispose();
        }
    }
}
