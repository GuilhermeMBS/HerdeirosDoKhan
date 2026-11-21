package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controller.Controller;
import model.GameData;
import model.ObservadoIF;
import model.ObservadorIF;
import model.TributeType;

/**
 * Non-modal enlarged view of the bottom-left Upgrades Area (4 fixed slots + closed stack).
 * Empty slots expose the Khan refill bonuses (+1 Coin at 2 empty slots, +1 Sword at 4 empty slots).
 */
class UpgradeMarketFrame extends JFrame implements ObservadorIF {

    private static final long serialVersionUID = 1L;

    private static final String UPGRADE_IMAGE_DIR = "assets/cartoes/cartoes1/";
    private static final String EMPTY_CODE = "EMPTY";
    private static final int TILE_WIDTH = 104;
    private static final int TILE_HEIGHT = 104;
    private static final int PADDING = 18;

    private final Controller controller;
    private final Map<String, Image> tileImages;
    private final List<Rectangle> marketSlotBounds;
    private Rectangle deckBounds;

    private final MarketCanvasPanel canvasPanel;
    private final JLabel feedbackLabel;
    private final JButton drawFreeStackButton;

    UpgradeMarketFrame() {
        super("Upgrades Area (Buy / Free White Column Bonus)");

        this.controller = Controller.getController();
        this.tileImages = new HashMap<>();
        this.marketSlotBounds = new ArrayList<>();
        this.deckBounds = new Rectangle();

        int frameWidth = (5 * TILE_WIDTH) + (7 * PADDING) + 30;
        int frameHeight = TILE_HEIGHT + 210;

        setSize(new Dimension(frameWidth, frameHeight));
        setResizable(true);
        setLayout(new BorderLayout(8, 8));
        setLocationRelativeTo(null);

        this.canvasPanel = new MarketCanvasPanel();
        add(this.canvasPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(8, 6));
        southPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 10, 12));

        this.feedbackLabel = new JLabel("Click an upgrade to buy for 2 coins. Moving Khan here refills slots and awards exposed bonuses.");
        this.feedbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel buttonsRow = new JPanel();
        this.drawFreeStackButton = new JButton("Claim Free White Upgrade from Stack");
        JButton closeButton = new JButton("Close");

        this.drawFreeStackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.claimFreeUpgradeFromDeck()) {
                    feedbackLabel.setForeground(new Color(20, 120, 45));
                    feedbackLabel.setText("Free upgrade drawn from stack and added to the White column.");
                } else {
                    feedbackLabel.setForeground(new Color(180, 30, 30));
                    feedbackLabel.setText("No free row-completion upgrade pending or stack is empty.");
                }
                refreshControls();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameData.getData().remove(UpgradeMarketFrame.this);
                dispose();
            }
        });

        buttonsRow.add(this.drawFreeStackButton);
        buttonsRow.add(closeButton);

        southPanel.add(this.feedbackLabel, BorderLayout.NORTH);
        southPanel.add(buttonsRow, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        this.controller.registra(this);
        refreshControls();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GameData.getData().remove(UpgradeMarketFrame.this);
            }
        });
    }

    @Override
    public void notify(ObservadoIF o) {
        refreshControls();
    }

    private void refreshControls() {
        boolean freePending = this.controller.hasCurrentPlayerFreeUpgradePending();
        this.drawFreeStackButton.setEnabled(freePending && this.controller.getUpgradeDeckSize() > 0);
        this.canvasPanel.repaint();
    }

    private Image getUpgradeImage(String code) {
        if (!this.tileImages.containsKey(code)) {
            BufferedImage loaded = ImageUtils.readProjectImage(UPGRADE_IMAGE_DIR + code + ".jpg");
            if (loaded != null) {
                this.tileImages.put(code, loaded);
            } else {
                this.tileImages.put(code, createFallbackTile(code));
            }
        }
        return this.tileImages.get(code);
    }

    private Image createFallbackTile(String code) {
        BufferedImage fallback = new BufferedImage(TILE_WIDTH, TILE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fallback.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = new Color(210, 175, 45);
        if (code.endsWith("-R")) fill = new Color(185, 50, 50);
        else if (code.endsWith("-V")) fill = new Color(45, 145, 70);

        g2.setColor(fill);
        g2.fillRoundRect(2, 2, TILE_WIDTH - 4, TILE_HEIGHT - 4, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(2, 2, TILE_WIDTH - 4, TILE_HEIGHT - 4, 10, 10);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(code, 22, TILE_HEIGHT / 2 + 6);
        g2.dispose();
        return fallback;
    }

    private void handleSlotClick(int slotIndex) {
        List<String> codes = this.controller.getAvailableUpgradeCodes();
        if (slotIndex < 0 || slotIndex >= codes.size() || EMPTY_CODE.equals(codes.get(slotIndex))) {
            this.feedbackLabel.setForeground(new Color(140, 90, 20));
            this.feedbackLabel.setText("Slot " + (slotIndex + 1) + " is empty. Move the Khan to Upgrades Area to refill.");
            return;
        }

        if (this.controller.hasCurrentPlayerFreeUpgradePending()) {
            if (this.controller.claimFreeUpgradeFromMarket(slotIndex)) {
                this.feedbackLabel.setForeground(new Color(20, 120, 45));
                this.feedbackLabel.setText("Free row-completion upgrade placed into the White column.");
            } else {
                this.feedbackLabel.setForeground(new Color(180, 30, 30));
                this.feedbackLabel.setText("Unable to place free upgrade into the White column.");
            }
        } else {
            if (this.controller.buyUpgradeFromMarket(slotIndex)) {
                if (this.controller.hasCurrentPlayerFreeUpgradePending()) {
                    this.feedbackLabel.setForeground(new Color(180, 110, 0));
                    this.feedbackLabel.setText("Row completed! Choose ANY upgrade (or stack) for FREE into the White column.");
                } else {
                    this.feedbackLabel.setForeground(new Color(20, 120, 45));
                    this.feedbackLabel.setText("Upgrade purchased for 2 coins and added to your board.");
                }
            } else {
                this.feedbackLabel.setForeground(new Color(180, 30, 30));
                this.feedbackLabel.setText("Purchase rejected: need 2 coins and space in the matching color column.");
            }
        }
        refreshControls();
    }

    private void handleDeckClick() {
        if (!this.controller.hasCurrentPlayerFreeUpgradePending()) {
            this.feedbackLabel.setForeground(new Color(180, 30, 30));
            this.feedbackLabel.setText("Drawing from the closed stack is only available for a free row-completion bonus.");
            return;
        }
        if (this.controller.claimFreeUpgradeFromDeck()) {
            this.feedbackLabel.setForeground(new Color(20, 120, 45));
            this.feedbackLabel.setText("Free upgrade drawn from stack and added to the White column.");
        } else {
            this.feedbackLabel.setForeground(new Color(180, 30, 30));
            this.feedbackLabel.setText("Cannot draw from stack right now.");
        }
        refreshControls();
    }

    private final class MarketCanvasPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private MarketCanvasPanel() {
            setBackground(new Color(235, 226, 208));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    for (int i = 0; i < marketSlotBounds.size(); i++) {
                        if (marketSlotBounds.get(i).contains(x, y)) {
                            handleSlotClick(i);
                            return;
                        }
                    }
                    if (deckBounds != null && deckBounds.contains(x, y)) {
                        handleDeckClick();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            marketSlotBounds.clear();

            int[] coinsArr = controller.getCurrentPlayerTributeAmount(TributeType.COIN);
            int realCoins = (coinsArr != null && coinsArr.length > 0) ? coinsArr[0] : 0;
            int virtualCoins = (coinsArr != null && coinsArr.length > 1) ? coinsArr[1] : 0;
            int freePendingCount = controller.getCurrentPlayerFreeUpgradesPendingCount();

            List<String> availableCodes = controller.getAvailableUpgradeCodes();
            int emptyCount = 0;
            for (String code : availableCodes) {
                if (EMPTY_CODE.equals(code)) {
                    emptyCount++;
                }
            }

            String khanBonusText = "None";
            if (emptyCount >= 4) khanBonusText = "+1 Coin & +1 Sword";
            else if (emptyCount >= 2) khanBonusText = "+1 Coin";

            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            if (freePendingCount > 0) {
                g2d.setColor(new Color(175, 105, 0));
                g2d.drawString(
                    "FREE WHITE COLUMN BONUS (" + freePendingCount + " pending) - "
                        + controller.getCurrentHeirName() + ": click any tile or the stack!",
                    PADDING,
                    22
                );
            } else {
                g2d.setColor(new Color(45, 35, 25));
                g2d.drawString(
                    "Player: " + controller.getCurrentHeirName()
                        + " | Coins: " + realCoins + (virtualCoins > 0 ? " (+" + virtualCoins + "v)" : "")
                        + " | Empty Slots: " + emptyCount + " -> Khan Refill Bonus: " + khanBonusText,
                    PADDING,
                    22
                );
            }

            int startY = 44;

            for (int i = 0; i < availableCodes.size(); i++) {
                String code = availableCodes.get(i);
                int x = PADDING + i * (TILE_WIDTH + PADDING);
                Rectangle bounds = new Rectangle(x, startY, TILE_WIDTH, TILE_HEIGHT);
                marketSlotBounds.add(bounds);

                if (EMPTY_CODE.equals(code)) {
                    g2d.setColor(new Color(85, 68, 52));
                    g2d.fillRoundRect(x, startY, TILE_WIDTH, TILE_HEIGHT, 8, 8);
                    g2d.setColor(new Color(195, 175, 140));
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRoundRect(x, startY, TILE_WIDTH, TILE_HEIGHT, 8, 8);

                    g2d.setColor(new Color(230, 220, 200));
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2d.drawString("EMPTY", x + 31, startY + 42);

                    if (i == 1) {
                        g2d.setColor(new Color(245, 205, 60));
                        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
                        g2d.drawString("2+ empty: +1 Coin", x + 4, startY + 68);
                    } else if (i == 3) {
                        g2d.setColor(new Color(240, 110, 95));
                        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
                        g2d.drawString("4 empty: +1 Sword", x + 3, startY + 68);
                    }
                    g2d.setColor(new Color(90, 75, 60));
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    g2d.drawString("Slot " + (i + 1), x + 36, startY + TILE_HEIGHT + 16);
                } else {
                    Image img = getUpgradeImage(code);
                    if (img != null) {
                        g2d.drawImage(img, x, startY, TILE_WIDTH, TILE_HEIGHT, this);
                    }
                    g2d.setColor(freePendingCount > 0 ? new Color(235, 175, 20) : new Color(60, 45, 30));
                    g2d.setStroke(new BasicStroke(freePendingCount > 0 ? 3f : 1.5f));
                    g2d.drawRect(x, startY, TILE_WIDTH, TILE_HEIGHT);

                    g2d.setColor(new Color(45, 35, 25));
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
                    g2d.drawString(code, x + 32, startY + TILE_HEIGHT + 16);
                }
            }

            // Closed stack on the right
            int deckX = PADDING + Math.max(4, availableCodes.size()) * (TILE_WIDTH + PADDING) + 12;
            deckBounds = new Rectangle(deckX, startY, TILE_WIDTH, TILE_HEIGHT);

            g2d.setColor(new Color(75, 55, 40));
            g2d.fillRoundRect(deckX, startY, TILE_WIDTH, TILE_HEIGHT, 10, 10);
            g2d.setColor(freePendingCount > 0 ? new Color(255, 215, 0) : new Color(200, 180, 140));
            g2d.setStroke(new BasicStroke(freePendingCount > 0 ? 3f : 2f));
            g2d.drawRoundRect(deckX, startY, TILE_WIDTH, TILE_HEIGHT, 10, 10);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2d.drawString("STACK", deckX + 30, startY + 42);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2d.drawString(String.valueOf(controller.getUpgradeDeckSize()), deckX + 42, startY + 66);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2d.drawString(freePendingCount > 0 ? "Free Draw" : "Closed Deck", deckX + 20, startY + TILE_HEIGHT + 16);

            g2d.dispose();
        }
    }
}
