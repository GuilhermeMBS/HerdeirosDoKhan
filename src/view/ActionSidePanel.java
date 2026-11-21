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
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.Controller;
import loader.GameStateLoader;
import model.ObservableIF;
import model.ObserverIF;
import model.PlayerColor;

/**
 * Lateral control panel integrating the Controller, observing match state changes,
 * enforcing turn-boundary save button rules via JFileChooser, and providing access
 * to board actions, counselors, and the Upgrades Area.
 */
public class ActionSidePanel extends JPanel implements ObserverIF {

    private static final long serialVersionUID = 1L;

    private final Controller controller;
    private final BoardPanel boardPanel;
    private final GameStateLoader stateLoader;

    private final JComboBox<String> cityTreasureSlotCombo;
    private final JComboBox<String> treasureTradeCombo;
    private final JButton saveGameButton;
    private final JLabel statusFeedbackLabel;
    private final PlayerInventoryPanel inventoryPanel;
    private PrivateCounselorSelectorFrame selectorFrame;
    private UpgradeMarketFrame upgradeMarketFrame;
    private WinnerFrame winnerFrame;

    public ActionSidePanel(BoardPanel boardPanel, GameStateLoader stateLoader) {
        this.controller = Controller.getController();
        this.boardPanel = boardPanel;
        this.stateLoader = stateLoader;

        setPreferredSize(new Dimension(360, 760));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(30, 8, 6, 8));

        JPanel actionsPanel = new JPanel(new GridLayout(9, 1, 3, 3));
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
        JButton openUpgradesButton = new JButton("Upgrades Area (Buy / Free Row)");
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
        actionsPanel.add(openUpgradesButton);
        actionsPanel.add(openCounselorButton);
        actionsPanel.add(this.treasureTradeCombo);
        actionsPanel.add(executeTradeButton);
        add(actionsPanel);

        add(Box.createVerticalStrut(4));

        this.inventoryPanel = new PlayerInventoryPanel();
        add(this.inventoryPanel);

        add(Box.createVerticalStrut(4));

        JPanel systemPanel = new JPanel(new GridLayout(3, 1, 3, 3));
        systemPanel.setBorder(BorderFactory.createTitledBorder("Turn & Match State"));

        JButton endTurnButton = new JButton("Next Player Turn");
        this.saveGameButton = new JButton("Save Match (.txt)");
        systemPanel.add(new ConfirmMoveButton());
        systemPanel.add(endTurnButton);
        systemPanel.add(this.saveGameButton);
        add(systemPanel);

        this.statusFeedbackLabel = new JLabel("Save is available before starting your turn.");
        this.statusFeedbackLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        this.statusFeedbackLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(this.statusFeedbackLabel);

        bindActionListeners(
            harvestButton,
            attackCityButton,
            buildYurtButton,
            moveKhanButton,
            openUpgradesButton,
            openCounselorButton,
            executeTradeButton,
            endTurnButton
        );

        updateSaveButtonState();
        this.controller.register(this);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                checkPrivateCounselorSelection();
            }
        });
    }

    @Override
    public void notify(ObservableIF o) {
        this.inventoryPanel.repaint();
        updateSaveButtonState();
        checkPrivateCounselorSelection();
        if (this.controller.hasCurrentPlayerFreeUpgradePending()) {
            this.statusFeedbackLabel.setText("Row completed! Open Upgrades Area to claim a FREE White upgrade.");
        }
        repaint();
    }

    private void updateSaveButtonState() {
        this.saveGameButton.setEnabled(this.controller.canSaveMatch());
    }

    private void checkPrivateCounselorSelection() {
        String currentSecret = this.controller.getCurrentPlayerPrivateCounselorName();
        if (currentSecret != null && !currentSecret.equalsIgnoreCase("none")) {
            return;
        }
        if (this.selectorFrame != null && this.selectorFrame.isShowing()) {
            return;
        }
        this.selectorFrame = new PrivateCounselorSelectorFrame();
        this.selectorFrame.setVisible(true);
    }

    private void handleSaveMatch() {
        if (!this.controller.canSaveMatch()) {
            updateSaveButtonState();
            this.statusFeedbackLabel.setText("Saving is only allowed before starting your turn.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Match State (.txt)");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int decision = fileChooser.showSaveDialog(this);
        if (decision != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File targetFile = fileChooser.getSelectedFile();
        if (targetFile == null) {
            return;
        }
        if (!targetFile.getName().toLowerCase().endsWith(".txt")) {
            targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".txt");
        }

        boolean saved = this.stateLoader.saveMatchState(targetFile);
        this.statusFeedbackLabel.setText(
            saved ? "Match saved to " + targetFile.getName() : "Failed to save match file."
        );
    }

    /** Opens the end of match screen (only once). */
    private void showWinnerScreen() {
        if (this.winnerFrame != null) {
            return;
        }
        this.winnerFrame = new WinnerFrame();
        this.winnerFrame.setVisible(true);
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
        JButton openUpgradesButton,
        JButton openCounselorButton,
        JButton executeTradeButton,
        JButton endTurnButton
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
                updateSaveButtonState();
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
                updateSaveButtonState();
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
                updateSaveButtonState();
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

        openUpgradesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (upgradeMarketFrame != null && upgradeMarketFrame.isShowing()) {
                    upgradeMarketFrame.toFront();
                    return;
                }
                upgradeMarketFrame = new UpgradeMarketFrame();
                upgradeMarketFrame.setVisible(true);
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
                updateSaveButtonState();
            }
        });

        endTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.endTurn();
                if (controller.getGameHasEnded()) {
                    statusFeedbackLabel.setText("MATCH ENDED!");
                    showWinnerScreen();
                } else {
                    statusFeedbackLabel.setText("Turn passed to " + controller.getCurrentHeirName());
                }
                updateSaveButtonState();
            }
        });

        this.saveGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSaveMatch();
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
            "Current Turn: " + controller.getCurrentHeirName(),
            14,
            22
        );
    }
}
