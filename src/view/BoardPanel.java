package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import model.HerdeirosFacade;

/**
 * Primary Java2D canvas responsible for rendering the game board, player pawns,
 * constructed yurts, province tributes, and Genghis Khan.
 * Strictly uses Graphics2D.drawImage() for all game elements and handles mouse clicks
 * without lambda expressions, in compliance with Iteration 2 specifications.
 */
public class BoardPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int CANVAS_WIDTH = 980;
    private static final int CANVAS_HEIGHT = 760;

    private final HerdeirosFacade modelFacade;
    private Image boardBackgroundImage;
    private Image yurtSprite;
    private Image khanSprite;
    private Image tributeSprite;
    private final Map<Integer, Image> playerPawnSprites;

    // Tracks each player's current node ID on the board for rendering.
    private final Map<Integer, String> pawnLocations;
    private String selectedNodeId;
    private String selectedProvinceId;
    private String activeKhanProvinceId;

    /**
     * Constructs the Java2D board panel and binds the mouse click listener.
     *
     * @param modelFacade Reference to the domain model facade.
     */
    public BoardPanel(HerdeirosFacade modelFacade) {
        this.modelFacade = modelFacade;
        this.playerPawnSprites = new HashMap<>();
        this.pawnLocations = new HashMap<>();
        this.selectedNodeId = "Karakorum";
        this.selectedProvinceId = null;
        this.activeKhanProvinceId = "Prov_Russia_1";

        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        setBackground(new Color(230, 218, 195));

        loadGameAssets();
        initializeDefaultPawnPositions();
        registerMouseClickHandler();
    }

    /**
     * Loads sprite images from the assets directory. If image files are not yet present
     * during local testing, generates fallback BufferedImages so drawImage() always works.
     */
    private void loadGameAssets() {
        this.boardBackgroundImage = loadOrCreatePlaceholder("assets/board.png", CANVAS_WIDTH, CANVAS_HEIGHT, new Color(222, 205, 175));
        this.yurtSprite = loadOrCreatePlaceholder("assets/yurt.png", 26, 26, new Color(245, 245, 245));
        this.khanSprite = loadOrCreatePlaceholder("assets/khan.png", 34, 34, new Color(40, 40, 40));
        this.tributeSprite = loadOrCreatePlaceholder("assets/tribute.png", 20, 20, new Color(218, 165, 32));

        this.playerPawnSprites.put(0, loadOrCreatePlaceholder("assets/pawn_red.png", 30, 30, new Color(200, 45, 45)));
        this.playerPawnSprites.put(1, loadOrCreatePlaceholder("assets/pawn_blue.png", 30, 30, new Color(45, 95, 200)));
        this.playerPawnSprites.put(2, loadOrCreatePlaceholder("assets/pawn_green.png", 30, 30, new Color(45, 160, 75)));
        this.playerPawnSprites.put(3, loadOrCreatePlaceholder("assets/pawn_yellow.png", 30, 30, new Color(220, 180, 40)));
        this.playerPawnSprites.put(4, loadOrCreatePlaceholder("assets/pawn_black.png", 30, 30, new Color(60, 60, 60)));
    }

    /**
     * Attempts to read an image from disk; generates an in-memory BufferedImage if missing.
     */
    private Image loadOrCreatePlaceholder(String path, int width, int height, Color fallbackColor) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (IOException ignored) {
            // Falls back to programmatic BufferedImage so Graphics2D.drawImage() never fails.
        }
        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fallback.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fallbackColor);
        if (width > 100) {
            g2.fillRect(0, 0, width, height);
        } else {
            g2.fillOval(2, 2, width - 4, height - 4);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(2, 2, width - 4, height - 4);
        }
        g2.dispose();
        return fallback;
    }

    /**
     * Places all pawns initially at Karakorum.
     */
    private void initializeDefaultPawnPositions() {
        this.pawnLocations.put(0, "Karakorum");
        this.pawnLocations.put(1, "Karakorum");
    }

    /**
     * Registers a classic anonymous MouseAdapter (strictly NO lambda expressions)
     * to handle node movement and province selection clicks on the board.
     */
    private void registerMouseClickHandler() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                handleBoardClick(event.getX(), event.getY());
            }
        });
    }

    /**
     * Processes a click at (mouseX, mouseY). If a valid node is clicked, attempts to move
     * the current player's pawn. Invalid moves are silently ignored as required.
     *
     * @param mouseX Horizontal click coordinate.
     * @param mouseY Vertical click coordinate.
     */
    private void handleBoardClick(int mouseX, int mouseY) {
        String clickedNode = BoardGeometry.findNodeAt(mouseX, mouseY);
        if (clickedNode != null) {
            this.selectedNodeId = clickedNode;

            // [TEAMMATE INTEGRATION - CONTROLLER & MOVEMENT]:
            // Replace the boolean check below with your teammates' controller/movement validation method,
            // e.g.: boolean moveAllowed = gameController.tryMoveActivePawnTo(clickedNode);
            boolean moveAllowed = validateAndMoveActivePawn(clickedNode);

            if (moveAllowed) {
                repaint();
            }
            // As required by the specification: if the move is invalid, nothing happens,
            // no popup is shown, and the current turn does not change.
            return;
        }

        String clickedProvince = BoardGeometry.findProvinceAt(mouseX, mouseY);
        if (clickedProvince != null) {
            this.selectedProvinceId = clickedProvince;
            repaint();
        }
    }

    /**
     * Local movement handler that integrates with the turn controller and movement rules.
     *
     * @param targetNodeId The destination node identifier clicked by the user.
     * @return True if the active pawn was moved; false otherwise.
     */
    private boolean validateAndMoveActivePawn(String targetNodeId) {
        // [TEAMMATE INTEGRATION - CONTROLLER]: Query active player index from the Controller.
        int activePlayerIndex = 0;

        // [TEAMMATE INTEGRATION - MOVEMENT]: Call your teammate's movement rule validator here.
        boolean canEnter = true;
        if (canEnter) {
            this.pawnLocations.put(activePlayerIndex, targetNodeId);
            return true;
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Mandatory rendering of the board background using Graphics2D.drawImage()
        if (this.boardBackgroundImage != null) {
            g2d.drawImage(this.boardBackgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        drawTopologicalGuideOverlays(g2d);
        drawProvinceTributes(g2d);
        drawConstructedYurts(g2d);
        drawKhanPiece(g2d);
        drawPlayerPawns(g2d);
    }

    /**
     * Draws visual node rings and labels so nodes and selection states are clearly visible.
     */
    private void drawTopologicalGuideOverlays(Graphics2D g2d) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (Map.Entry<String, Point> entry : BoardGeometry.getAllNodeCoordinates().entrySet()) {
            String nodeId = entry.getKey();
            Point pt = entry.getValue();
            int r = BoardGeometry.NODE_HIT_RADIUS;

            g2d.setColor(new Color(80, 60, 40, 180));
            g2d.fillOval(pt.x - r, pt.y - r, r * 2, r * 2);

            if (nodeId.equals(this.selectedNodeId)) {
                g2d.setColor(new Color(255, 215, 0));
                g2d.setStroke(new BasicStroke(3f));
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
            }
            g2d.drawOval(pt.x - r, pt.y - r, r * 2, r * 2);
            g2d.drawString(nodeId, pt.x - r, pt.y + r + 15);
        }

        for (Map.Entry<String, Point> entry : BoardGeometry.getAllProvinceCoordinates().entrySet()) {
            String provId = entry.getKey();
            Point pt = entry.getValue();
            int r = BoardGeometry.PROVINCE_HIT_RADIUS;

            if (provId.equals(this.selectedProvinceId)) {
                g2d.setColor(new Color(50, 205, 50));
                g2d.setStroke(new BasicStroke(2.5f));
            } else {
                g2d.setColor(new Color(120, 100, 70));
                g2d.setStroke(new BasicStroke(1.2f));
            }
            g2d.drawRect(pt.x - r, pt.y - r, r * 2, r * 2);
            g2d.drawString(provId, pt.x - r, pt.y - r - 6);
        }
    }

    /**
     * Renders tribute tokens on each province via Graphics2D.drawImage().
     */
    private void drawProvinceTributes(Graphics2D g2d) {
        for (Map.Entry<String, Point> entry : BoardGeometry.getAllProvinceCoordinates().entrySet()) {
            String provId = entry.getKey();
            Point center = entry.getValue();
            int count = this.modelFacade.getProvinceResourceCount(provId);

            for (int i = 0; i < count; i++) {
                int offsetX = center.x - 22 + (i * 15);
                int offsetY = center.y - 10;
                g2d.drawImage(this.tributeSprite, offsetX, offsetY, 20, 20, this);
            }
        }
    }

    /**
     * Renders constructed Yurts on nodes via Graphics2D.drawImage().
     */
    private void drawConstructedYurts(Graphics2D g2d) {
        for (Map.Entry<String, Point> entry : BoardGeometry.getAllNodeCoordinates().entrySet()) {
            String nodeId = entry.getKey();
            if (this.modelFacade.nodeHasYurt(nodeId)) {
                Point pt = entry.getValue();
                g2d.drawImage(this.yurtSprite, pt.x - 13, pt.y - 34, 26, 26, this);
            }
        }
    }

    /**
     * Renders the Genghis Khan piece at its active location via Graphics2D.drawImage().
     */
    private void drawKhanPiece(Graphics2D g2d) {
        Point khanPt;
        if (this.modelFacade.isKhanAtUpgradesArea()) {
            khanPt = BoardGeometry.getUpgradesAreaCoordinate();
        } else {
            khanPt = BoardGeometry.getProvinceCoordinates(this.activeKhanProvinceId);
        }
        if (khanPt != null && this.khanSprite != null) {
            g2d.drawImage(this.khanSprite, khanPt.x - 17, khanPt.y + 8, 34, 34, this);
        }
    }

    /**
     * Renders all player pawns at their current topological node via Graphics2D.drawImage().
     */
    private void drawPlayerPawns(Graphics2D g2d) {
        for (Map.Entry<Integer, String> entry : this.pawnLocations.entrySet()) {
            int playerIdx = entry.getKey();
            String nodeId = entry.getValue();
            Point nodeCenter = BoardGeometry.getNodeCoordinates(nodeId);
            Image pawnImg = this.playerPawnSprites.get(playerIdx);

            if (nodeCenter != null && pawnImg != null) {
                int offset = (playerIdx * 14) - 10;
                g2d.drawImage(pawnImg, nodeCenter.x + offset - 15, nodeCenter.y - 15, 30, 30, this);
            }
        }
    }

    public String getSelectedNodeId() {
        return this.selectedNodeId;
    }

    public String getSelectedProvinceId() {
        return this.selectedProvinceId;
    }

    public void setActiveKhanProvinceId(String provinceId) {
        this.activeKhanProvinceId = provinceId;
        repaint();
    }

    public String getPawnLocation(int playerIndex) {
        return this.pawnLocations.getOrDefault(playerIndex, "Karakorum");
    }

    public void setPawnLocation(int playerIndex, String nodeId) {
        this.pawnLocations.put(playerIndex, nodeId);
        repaint();
    }
}
