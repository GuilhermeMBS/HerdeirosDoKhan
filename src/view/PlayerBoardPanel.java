package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.JPanel;

import controller.Controller;
import model.ColumnColor;
import model.PlayerColor;

/**
 * Shows the player board (cartela) of the active player using Graphics2D.drawImage().
 */
public class PlayerBoardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    static final int BOARD_WIDTH = 350;
    static final int BOARD_HEIGHT = 190;
    static final int HEADER = 26;

    private static final HashMap<String, String> FILES = new HashMap<>();
    static {
        FILES.put("Altani", "img/cartelas/cartela1.jpg");
        FILES.put("Jochi", "img/cartelas/cartela2.jpg");
        FILES.put("Chagatai", "img/cartelas/cartela3.jpg");
        FILES.put("Tolui", "img/cartelas/cartela4.jpg");
        FILES.put("Ogedei", "img/cartelas/cartela5.jpg");
    }

    private static final HashMap<ColumnColor, Rectangle2D.Double> COLUMNS = new HashMap<>();
    static {
        COLUMNS.put(ColumnColor.RED, new Rectangle2D.Double(0.255, 0.085, 0.160, 0.825));
        COLUMNS.put(ColumnColor.GREEN, new Rectangle2D.Double(0.432, 0.085, 0.160, 0.825));
        COLUMNS.put(ColumnColor.YELLOW, new Rectangle2D.Double(0.608, 0.085, 0.160, 0.825));
        COLUMNS.put(ColumnColor.WHITE, new Rectangle2D.Double(0.802, 0.085, 0.164, 0.825));
    }

    private static final HashMap<PlayerColor, Color> COLORS = new HashMap<>();
    static {
        COLORS.put(PlayerColor.BLUE, new Color(30, 90, 200));
        COLORS.put(PlayerColor.RED, new Color(200, 40, 40));
        COLORS.put(PlayerColor.GREEN, new Color(30, 140, 60));
        COLORS.put(PlayerColor.YELLOW, new Color(230, 190, 30));
        COLORS.put(PlayerColor.BLACK, new Color(40, 40, 40));
    }

    private final Controller controller;
    private final HashMap<String, Image> boardImages = new HashMap<>();

    public PlayerBoardPanel(Controller controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(BOARD_WIDTH, HEADER + BOARD_HEIGHT));
        addMouseListener(new ColumnClickListener(this, controller));
    }

    ColumnColor columnAt(int x, int y) {
        for (ColumnColor column : ColumnColor.values()) {
            if (areaOf(column).contains(x, y)) {
                return column;
            }
        }
        return null;
    }

    private Rectangle areaOf(ColumnColor column) {
        Rectangle2D.Double f = COLUMNS.get(column);
        return new Rectangle(
            (int) (f.x * BOARD_WIDTH),
            HEADER + (int) (f.y * BOARD_HEIGHT),
            (int) (f.width * BOARD_WIDTH),
            (int) (f.height * BOARD_HEIGHT)
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        PlayerColor playerColor = controller.getCurrentPlayerColor();
        Color color = COLORS.getOrDefault(playerColor, new Color(30, 90, 200));

        drawHeader(g2, playerColor, color);

        Image board = getBoardImage(controller.getCurrentHeirName());
        if (board != null) {
            g2.drawImage(board, 0, HEADER, null);
        }

        for (ColumnColor column : ColumnColor.values()) {
            Rectangle area = areaOf(column);
            if (column == controller.getChosenColumn()) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(3.5f));
                g2.drawRoundRect(area.x, area.y, area.width, area.height, 8, 8);
                drawToken(g2, area, color);
            } else if (!controller.isColumnFree(column)) {
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRoundRect(area.x, area.y, area.width, area.height, 8, 8);
                drawToken(g2, area, color);
            }
        }
    }

    private void drawHeader(Graphics2D g2, PlayerColor playerColor, Color color) {
        g2.setColor(color);
        g2.fillRect(0, 0, BOARD_WIDTH, HEADER);

        String text = "Vez de " + controller.getCurrentHeirName();
        if (controller.getChosenColumn() == null) {
            text += " - escolha uma coluna";
        } else {
            text += " - cavalos: " + controller.getMovementsLeft();
        }
        g2.setColor(playerColor == PlayerColor.YELLOW ? Color.BLACK : Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g2.drawString(text, 10, 18);
    }

    private void drawToken(Graphics2D g2, Rectangle area, Color color) {
        int size = 18;
        int x = area.x + area.width / 2 - size / 2;
        int y = area.y - size + 6;
        g2.setColor(color);
        g2.fillOval(x, y, size, size);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x, y, size, size);
    }

    private Image getBoardImage(String heirName) {
        if (!boardImages.containsKey(heirName)) {
            boardImages.put(heirName, loadBoardImage(FILES.get(heirName)));
        }
        return boardImages.get(heirName);
    }

    private Image loadBoardImage(String file) {
        BufferedImage image = BoardPanel.readProjectImage(file);
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        while (width / 2 >= BOARD_WIDTH) {
            width /= 2;
            height /= 2;
            image = resize(image, width, height);
        }
        return resize(image, BOARD_WIDTH, BOARD_HEIGHT);
    }

    private BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
}
