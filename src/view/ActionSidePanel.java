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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.List;

import controller.Controller;
import loader.GameStateLoader;
import model.PlayerColor;

/**
 * Lateral control panel preserving the exact layout of Iteration 2
 * while integrating the PlayerBoardPanel and Controller.
 */
public class ActionSidePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Controller controller;
    private final BoardPanel boardPanel;
    private final PlayerBoardPanel playerBoardPanel;
    private final GameStateLoader stateLoader;

    private final JComboBox<String> treasureTradeCombo;
    private final JLabel statusFeedbackLabel;
//    private final DiceDisplayCanvas diceCanvas;

    public ActionSidePanel(
        BoardPanel boardPanel,
        PlayerBoardPanel playerBoardPanel,
        GameStateLoader stateLoader
    ) {
        this.controller = Controller.getController();
        this.boardPanel = boardPanel;
        this.playerBoardPanel = playerBoardPanel;
        this.stateLoader = stateLoader;

        setPreferredSize(new Dimension(360, 760));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JPanel actionsPanel = new JPanel(new GridLayout(7, 1, 4, 4));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Board Node & Province Actions"));

        JButton harvestButton = new JButton("Harvest Province Tribute");
        JButton attackCityButton = new JButton("Attack City (Plunder Treasure)");
        JButton buildYurtButton = new JButton("Build Yurt on Current Node");
//        JButton moveKhanProvinceButton = new JButton("Move Khan to Selected Province");
//        JButton moveKhanUpgradesButton = new JButton("Move Khan to Upgrades Area");
        JButton moveKhanButton = new JButton("Move Khan");
        
        this.treasureTradeCombo = new JComboBox<>(new String[] {
            "FUR + SILK (Counselor)",
            "FUR + IRON + ANIMAL (Karakorum Default)",
            "IRON + SILK + SPICE (Karakorum Alt)"
        });
        JButton executeTradeButton = new JButton("Execute Trade at Post");

        actionsPanel.add(harvestButton);
        actionsPanel.add(attackCityButton);
        actionsPanel.add(buildYurtButton);
        actionsPanel.add(moveKhanButton);
//        actionsPanel.add(moveKhanProvinceButton);
//        actionsPanel.add(moveKhanUpgradesButton);
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
            harvestButton,
            attackCityButton,
            buildYurtButton,
            moveKhanButton,
//            moveKhanProvinceButton,
//            moveKhanUpgradesButton,
            executeTradeButton,
            endTurnButton,
            saveGameButton
        );
    }

    public void refreshDisplay() {
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
        JButton harvestButton,
        JButton attackCityButton,
        JButton buildYurtButton,
        JButton moveKhanButton,
//        JButton moveKhanProvinceButton,
//        JButton moveKhanUpgradesButton,
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
        
        moveKhanButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		List<String> provinces = controller.getKhanProvincesIds();
        		
        		KhanFrame khanFrame = new KhanFrame(provinces);
        		khanFrame.setVisible(true);
        	}
        });

//        moveKhanProvinceButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String provinceId = boardPanel.getSelectedProvinceId();
//                if (provinceId != null && controller.createKhanAction(provinceId)) {
//                    statusFeedbackLabel.setText("Khan moved to " + provinceId);
//                    refreshDisplay();
//                } else {
//                    statusFeedbackLabel.setText("Select a valid province for the Khan.");
//                }
//            }
//        });
//
//        moveKhanUpgradesButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (controller.createKhanAction("UPGRADES")) {
//                    statusFeedbackLabel.setText("Khan moved to Upgrades Area.");
//                    refreshDisplay();
//                } else {
//                    statusFeedbackLabel.setText("Khan move to Upgrades rejected.");
//                }
//            }
//        });

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
                "Current Turn: " + controller.getCurrentPlayerName() + " (Steps: " + controller.getMovementsLeft() + ")",
                14,
                22
            );
    }
}
