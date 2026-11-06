package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
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
 * Displays the 4 Open Counselor cards in play using Graphics2D.drawImage()
 * and observes match state updates via the Observer pattern.
 */
class OpenCounselorFrame extends JFrame implements ObservadorIF {

    private static final long serialVersionUID = 1L;

    private static final String COMMON_PATH = "assets/openCounselors/";
    private static final int COUNSELOR_WIDTH = 160;
    private static final int COUNSELOR_HEIGHT = 280;
    private static final int PADDING = 20;

    private final Controller controller;
    private final Map<String, Image> counselorImages;
    private final CounselorCanvasPanel canvasPanel;

    OpenCounselorFrame() {
        super("Open Counselors");

        this.controller = Controller.getController();
        this.counselorImages = new HashMap<>();

        int frameWidth = (4 * COUNSELOR_WIDTH) + (5 * PADDING) + 20;
        int frameHeight = COUNSELOR_HEIGHT + (2 * PADDING) + 50;

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
            if (!this.counselorImages.containsKey(id)) {
                String path = COMMON_PATH + id + ".jpg";
                Image img = loadOrCreateCardPlaceholder(id, path, COUNSELOR_WIDTH, COUNSELOR_HEIGHT);
                this.counselorImages.put(id, img);
            }
        }
    }

    private Image loadOrCreateCardPlaceholder(String cardId, String path, int width, int height) {
        BufferedImage loaded = BoardPanel.readProjectImage(path);
        if (loaded != null) {
            return loaded;
        }
        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fallback.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(65, 55, 45));
        g2.fillRoundRect(2, 2, width - 4, height - 4, 12, 12);
        g2.setColor(new Color(218, 165, 32));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(2, 2, width - 4, height - 4, 12, 12);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString(cardId, 14, height / 2);
        g2.dispose();
        return fallback;
    }

    /**
     * Custom Java2D canvas rendering all active Open Counselor cards via Graphics2D.drawImage().
     */
    private final class CounselorCanvasPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private CounselorCanvasPanel() {
            setBackground(new Color(230, 218, 195));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            List<String> inGameIds = controller.getGameOpenCounselorsIds();
            int totalCards = inGameIds.size();
            if (totalCards == 0) {
                g2d.dispose();
                return;
            }

            int availableWidth = Math.max(getWidth(), (totalCards * COUNSELOR_WIDTH) + ((totalCards + 1) * PADDING));
            int totalCardsWidth = (totalCards * COUNSELOR_WIDTH) + ((totalCards - 1) * PADDING);
            int startX = Math.max(PADDING, (availableWidth - totalCardsWidth) / 2);
            int startY = Math.max(PADDING, (getHeight() - COUNSELOR_HEIGHT) / 2);

            for (int i = 0; i < totalCards; i++) {
                String id = inGameIds.get(i);
                Image cardImage = counselorImages.get(id);
                int x = startX + i * (COUNSELOR_WIDTH + PADDING);
                if (cardImage != null) {
                    g2d.drawImage(cardImage, x, startY, COUNSELOR_WIDTH, COUNSELOR_HEIGHT, this);
                }
            }

            g2d.dispose();
        }
    }
}
