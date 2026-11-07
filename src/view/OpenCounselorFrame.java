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
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import controller.Controller;
import model.GameData;
import model.ObservadoIF;
import model.ObservadorIF;

/**
 * Displays the 4 Open Counselor cards in play alongside the active player's
 * Private (Secret) Counselor card using Graphics2D.drawImage().
 * Observes match state changes so the secret card and completion badges update automatically.
 */
class OpenCounselorFrame extends JFrame implements ObservadorIF {

    private static final long serialVersionUID = 1L;

    private static final String OPEN_PATH = "assets/openCounselors/";
    private static final String PRIVATE_PATH = "assets/privateCounselors/";
    private static final int COUNSELOR_WIDTH = 155;
    private static final int COUNSELOR_HEIGHT = 270;
    private static final int PADDING = 18;

    private final Controller controller;
    private final Map<String, Image> openCounselorImages;
    private final Map<String, Image> privateCounselorImages;
    private final List<Rectangle> openCardBounds;
    private final CounselorCanvasPanel canvasPanel;

    OpenCounselorFrame() {
        super("Open & Secret Counselors");

        this.controller = Controller.getController();
        this.openCounselorImages = new HashMap<>();
        this.privateCounselorImages = new HashMap<>();
        this.openCardBounds = new ArrayList<>();

        int frameWidth = (5 * COUNSELOR_WIDTH) + (7 * PADDING) + 30;
        int frameHeight = COUNSELOR_HEIGHT + (2 * PADDING) + 95;

        setSize(new Dimension(frameWidth, frameHeight));
        setResizable(true);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        loadActiveCounselorImages();

        this.canvasPanel = new CounselorCanvasPanel();
        add(this.canvasPanel, BorderLayout.CENTER);

        this.controller.registra(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GameData.getData().remove(OpenCounselorFrame.this);
            }
        });
    }

    @Override
    public void notify(ObservadoIF o) {
        loadActiveCounselorImages();
        this.canvasPanel.repaint();
    }

    private void loadActiveCounselorImages() {
        List<String> inGameIds = this.controller.getGameOpenCounselorsIds();
        for (String id : inGameIds) {
            if (!this.openCounselorImages.containsKey(id)) {
                String path = OPEN_PATH + id + ".jpg";
                Image img = loadOrCreateCardImage(id, "OPEN COUNSELOR", path, COUNSELOR_WIDTH, COUNSELOR_HEIGHT, new Color(65, 55, 45));
                this.openCounselorImages.put(id, img);
            }
        }

        String secretName = this.controller.getCurrentPlayerPrivateCounselorName();
        if (secretName != null && !this.privateCounselorImages.containsKey(secretName)) {
            String path = PRIVATE_PATH + secretName + ".jpg";
            Image img = loadOrCreateCardImage(secretName, "SECRET COUNSELOR", path, COUNSELOR_WIDTH, COUNSELOR_HEIGHT, new Color(45, 35, 70));
            this.privateCounselorImages.put(secretName, img);
        }
    }

    private Image loadOrCreateCardImage(String title, String subtitle, String path, int width, int height, Color bgColor) {
        BufferedImage loaded = ImageUtils.readProjectImage(path);
        if (loaded == null) {
            loaded = ImageUtils.readProjectImage("img/conselheiros/" + title + ".jpg");
        }
        if (loaded != null) {
            return loaded;
        }

        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fallback.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(bgColor);
        g2.fillRoundRect(2, 2, width - 4, height - 4, 14, 14);

        g2.setColor(new Color(218, 165, 32));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(2, 2, width - 4, height - 4, 14, 14);

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString(subtitle, 14, 28);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString(title, 14, height / 2);

        g2.dispose();
        return fallback;
    }

    /**
     * Custom Java2D canvas rendering the 4 Open Counselors and the active player's
     * Private Counselor card exclusively via Graphics2D.drawImage().
     */
    private final class CounselorCanvasPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private CounselorCanvasPanel() {
            setBackground(new Color(230, 218, 195));
//            addMouseListener(new MouseAdapter() {
//                @Override
//                public void mousePressed(MouseEvent e) {
//                    handleCardClick(e.getX(), e.getY());
//                }
//            });
        }

//        private void handleCardClick(int mouseX, int mouseY) {
//            for (int i = 0; i < openCardBounds.size(); i++) {
//                if (openCardBounds.get(i).contains(mouseX, mouseY)) {
//                    controller.claimOpenCounselor(i);
//                    repaint();
//                    return;
//                }
//            }
//        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            openCardBounds.clear();

            List<String> inGameIds = controller.getGameOpenCounselorsIds();
            int startX = PADDING;
            int startY = 38;

            g2d.setColor(new Color(45, 35, 25));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("Open Counselors (Click a card to claim if completed):", startX, 24);

            for (int i = 0; i < inGameIds.size(); i++) {
                String id = inGameIds.get(i);
                Image cardImage = openCounselorImages.get(id);
                int x = startX + i * (COUNSELOR_WIDTH + PADDING);

                Rectangle bounds = new Rectangle(x, startY, COUNSELOR_WIDTH, COUNSELOR_HEIGHT);
                openCardBounds.add(bounds);

                if (cardImage != null) {
                    g2d.drawImage(cardImage, x, startY, COUNSELOR_WIDTH, COUNSELOR_HEIGHT, this);
                }

                String owner = controller.getOpenCounselorOwnerName(i);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                if (owner != null) {
                    g2d.setColor(new Color(0, 0, 0, 140));
                    g2d.fillRoundRect(x, startY + COUNSELOR_HEIGHT - 34, COUNSELOR_WIDTH, 34, 8, 8);
                    g2d.setColor(new Color(120, 255, 140));
                    g2d.drawString("Claimed: " + owner, x + 10, startY + COUNSELOR_HEIGHT - 12);
                }
            }

            // Draw separator and active player's Private Counselor
            int secretX = startX + (4 * (COUNSELOR_WIDTH + PADDING)) + PADDING;
            g2d.setColor(new Color(120, 100, 80));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawLine(secretX - PADDING, 12, secretX - PADDING, startY + COUNSELOR_HEIGHT + 20);

            String activePlayer = controller.getCurrentHeirName();
            String secretName = controller.getCurrentPlayerPrivateCounselorName();
            g2d.setColor(new Color(55, 30, 85));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("Secret (" + activePlayer + "):", secretX, 24);

            Image secretImg = privateCounselorImages.get(secretName);
            if (secretImg != null) {
                g2d.drawImage(secretImg, secretX, startY, COUNSELOR_WIDTH, COUNSELOR_HEIGHT, this);
            }

            g2d.dispose();
        }
    }
}
