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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import controller.Controller;
import model.ColumnColor;
import model.Heir;
import model.PlayerColor;

/**
 * Shows the player board (cartela) of the player whose turn it is.
 * Clicking on a free column starts the turn with it (see {@link ColumnClickListener}).
 *
 * <p>The board is drawn with {@link Graphics2D#drawImage}. Each image is
 * loaded and resized only the first time its heir is shown.</p>
 *
 * @author Vinicius
 */
public class PlayerBoardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Size of the board on the screen (same proportion as the images, 5949 x 3237). */
    static final int BOARD_WIDTH = 700;
    static final int BOARD_HEIGHT = 381;
    /** Space above the board for the colored bar with the player of the turn. */
    static final int HEADER = 40;

    /** Image file of each heir's board. */
    private static final HashMap<Heir, String> FILES = new HashMap<>();
    static {
        FILES.put(Heir.ALTANI, "img/cartelas/cartela1.jpg");
        FILES.put(Heir.JOCHI, "img/cartelas/cartela2.jpg");
        FILES.put(Heir.CHAGATAI, "img/cartelas/cartela3.jpg");
        FILES.put(Heir.TOLUI, "img/cartelas/cartela4.jpg");
        FILES.put(Heir.OGEDEI, "img/cartelas/cartela5.jpg");
    }

    /**
     * Where each column is on the image, as a fraction of the image's width
     * and height (x, y, width, height). The columns are always red, green,
     * yellow and white, from left to right, on every heir's board.
     */
    private static final HashMap<ColumnColor, Rectangle2D.Double> COLUMNS = new HashMap<>();
    static {
        COLUMNS.put(ColumnColor.RED, new Rectangle2D.Double(0.255, 0.085, 0.160, 0.825));
        COLUMNS.put(ColumnColor.GREEN, new Rectangle2D.Double(0.432, 0.085, 0.160, 0.825));
        COLUMNS.put(ColumnColor.YELLOW, new Rectangle2D.Double(0.608, 0.085, 0.160, 0.825));
        COLUMNS.put(ColumnColor.WHITE, new Rectangle2D.Double(0.802, 0.085, 0.164, 0.825));
    }

    /** Color used on the screen for each player. */
    private static final HashMap<PlayerColor, Color> COLORS = new HashMap<>();
    static {
        COLORS.put(PlayerColor.BLUE, new Color(30, 90, 200));
        COLORS.put(PlayerColor.RED, new Color(200, 40, 40));
        COLORS.put(PlayerColor.GREEN, new Color(30, 140, 60));
        COLORS.put(PlayerColor.YELLOW, new Color(230, 190, 30));
        COLORS.put(PlayerColor.BLACK, new Color(40, 40, 40));
    }

    private final Controller controller;
    /** Board images already loaded and resized, so each file is read only once. */
    private final HashMap<Heir, Image> boardImages = new HashMap<>();

    public PlayerBoardPanel(Controller controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(BOARD_WIDTH, HEADER + BOARD_HEIGHT));
        addMouseListener(new ColumnClickListener(this, controller));
    }

    /**
     * Finds the column at a point of the panel.
     * @return the column, or {@code null} if the point is outside every column
     */
    ColumnColor columnAt(int x, int y) {
        for (ColumnColor column : ColumnColor.values()) {
            if (areaOf(column).contains(x, y)) {
                return column;
            }
        }
        return null;
    }

    /** Area of the column on the panel, in pixels. */
    private Rectangle areaOf(ColumnColor column) {
        Rectangle2D.Double f = COLUMNS.get(column);
        return new Rectangle((int) (f.x * BOARD_WIDTH), HEADER + (int) (f.y * BOARD_HEIGHT),
                (int) (f.width * BOARD_WIDTH), (int) (f.height * BOARD_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        PlayerColor playerColor = controller.getCurrentPlayerColor();
        Color color = COLORS.get(playerColor);

        drawHeader(g2, playerColor, color);

        Image board = getBoardImage(controller.getCurrentHeir());
        if (board != null) {
            g2.drawImage(board, 0, HEADER, null);
        }

        for (ColumnColor column : ColumnColor.values()) {
            Rectangle area = areaOf(column);
            if (column == controller.getChosenColumn()) {
                // Column of this turn: outlined with the player's color
                g2.setColor(color);
                g2.setStroke(new BasicStroke(5));
                g2.drawRoundRect(area.x, area.y, area.width, area.height, 12, 12);
                drawToken(g2, area, color);
            } else if (!controller.isColumnFree(column)) {
                // Column used in a previous turn: darker and with the token
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRoundRect(area.x, area.y, area.width, area.height, 12, 12);
                drawToken(g2, area, color);
            }
        }
    }

    /** Bar with the player's color, the heir's name and the horses left. */
    private void drawHeader(Graphics2D g2, PlayerColor playerColor, Color color) {
        g2.setColor(color);
        g2.fillRect(0, 0, BOARD_WIDTH, HEADER);

        String text = "Vez de " + controller.getCurrentHeir().getDisplayName();
        if (controller.getChosenColumn() == null) {
            text += "  -  escolha uma coluna";
        } else {
            text += "  -  cavalos: " + controller.getMovementsLeft();
        }
        g2.setColor(playerColor == PlayerColor.YELLOW ? Color.BLACK : Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.drawString(text, 14, 26);
    }

    /** Activation token: a circle with the player's color above the column. */
    private void drawToken(Graphics2D g2, Rectangle area, Color color) {
        int size = 26;
        int x = area.x + area.width / 2 - size / 2;
        int y = area.y - size + 8;
        g2.setColor(color);
        g2.fillOval(x, y, size, size);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x, y, size, size);
    }

    private Image getBoardImage(Heir heir) {
        if (!boardImages.containsKey(heir)) {
            boardImages.put(heir, loadBoardImage(FILES.get(heir)));
        }
        return boardImages.get(heir);
    }

    /**
     * Reads the image file and resizes it to the size of the board on the screen.
     * The files are very big (about 6000 pixels wide), so the image is halved
     * a few times before the final resize: this keeps it sharp.
     * @return the resized image, or {@code null} if the file could not be read
     */
    private Image loadBoardImage(String file) {
        try {
            BufferedImage image = ImageIO.read(new File(file));
            int width = image.getWidth();
            int height = image.getHeight();
            while (width / 2 >= BOARD_WIDTH) {
                width /= 2;
                height /= 2;
                image = resize(image, width, height);
            }
            return resize(image, BOARD_WIDTH, BOARD_HEIGHT);
        } catch (IOException e) {
            System.err.println("Could not read the image " + file);
            return null;
        }
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
