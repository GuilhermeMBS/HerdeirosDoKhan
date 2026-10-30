package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import controller.Controller;


/**
 * Primary Java2D canvas responsible for rendering the game board, player pawns,
 * constructed yurts, province tributes, and Genghis Khan via Graphics2D.drawImage().
 */
public class BoardPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    public static final int CANVAS_WIDTH = 980;
    public static final int CANVAS_HEIGHT = 760;

    private final Controller controller;
    private Runnable onBoardStateChanged;

    private Image boardBackgroundImage;
    private Image yurtSprite;
    private Image khanSprite;
    private Image tributeSprite;
    private final Map<Integer, Image> playerPawnSprites;

    private String selectedNodeId;
    private String selectedProvinceId;

    public BoardPanel() {
        this.controller = Controller.getController();
        this.playerPawnSprites = new HashMap<>();
        this.selectedNodeId = "CENTRAL";
        this.selectedProvinceId = "RUSSIA_YURT1";

        setPreferredSize(new Dimension(CANVAS_WIDTH, 600));
        setBackground(new Color(230, 218, 195));

        loadGameAssets();
        registerMouseClickHandler();
    }


    public void setOnBoardStateChanged(Runnable callback) {
        this.onBoardStateChanged = callback;
    }

    static BufferedImage readProjectImage(String relativePath) {
        String[] candidatePrefixes = new String[] {
            "",
            "src/",
            "bin/",
            "../",
            "../src/",
            "../../"
        };

        for (String prefix : candidatePrefixes) {
            File candidate = new File(prefix + relativePath);
            if (candidate.exists() && candidate.isFile()) {
                try {
                    return ImageIO.read(candidate);
                } catch (IOException ignored) {
                }
            }
        }

        try (InputStream in = BoardPanel.class.getClassLoader().getResourceAsStream(relativePath)) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (IOException ignored) {
        }

        File targetName = new File(relativePath);
        File found = findFileByName(new File(System.getProperty("user.dir")), targetName.getName(), 3);
        if (found != null) {
            try {
                return ImageIO.read(found);
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private static File findFileByName(File dir, String fileName, int maxDepth) {
        if (dir == null || !dir.isDirectory() || maxDepth < 0) {
            return null;
        }
        File[] children = dir.listFiles();
        if (children == null) {
            return null;
        }
        for (File child : children) {
            if (child.isFile() && child.getName().equalsIgnoreCase(fileName)) {
                return child;
            }
        }
        for (File child : children) {
            if (child.isDirectory() && !child.getName().startsWith(".")) {
                File match = findFileByName(child, fileName, maxDepth - 1);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private void loadGameAssets() {
        this.boardBackgroundImage = loadOrCreatePlaceholder("img/tabuleiro.jpg", CANVAS_WIDTH, CANVAS_HEIGHT, new Color(222, 205, 175));
        this.yurtSprite = loadOrCreatePlaceholder("assets/yurt.png", 26, 26, new Color(245, 245, 245));
        this.khanSprite = loadOrCreatePlaceholder("assets/khan.png", 32, 32, new Color(40, 40, 40));
        this.tributeSprite = loadOrCreatePlaceholder("assets/tribute.png", 18, 18, new Color(218, 165, 32));

        this.playerPawnSprites.put(0, loadOrCreatePlaceholder("assets/pawn_blue.png", 28, 28, new Color(30, 90, 200)));
        this.playerPawnSprites.put(1, loadOrCreatePlaceholder("assets/pawn_red.png", 28, 28, new Color(200, 40, 40)));
        this.playerPawnSprites.put(2, loadOrCreatePlaceholder("assets/pawn_green.png", 28, 28, new Color(30, 140, 60)));
        this.playerPawnSprites.put(3, loadOrCreatePlaceholder("assets/pawn_yellow.png", 28, 28, new Color(230, 190, 30)));
        this.playerPawnSprites.put(4, loadOrCreatePlaceholder("assets/pawn_black.png", 28, 28, new Color(40, 40, 40)));
    }

    private Image loadOrCreatePlaceholder(String path, int width, int height, Color fallbackColor) {
        BufferedImage loaded = readProjectImage(path);
        if (loaded != null) {
            return loaded;
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

    private void registerMouseClickHandler() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                int logicalX = (int) Math.round((double) event.getX() * CANVAS_WIDTH / Math.max(1, getWidth()));
                int logicalY = (int) Math.round((double) event.getY() * CANVAS_HEIGHT / Math.max(1, getHeight()));
                handleBoardClick(logicalX, logicalY);
            }
        });
    }

    private void handleBoardClick(int mouseX, int mouseY) {
        String clickedNode = BoardGeometry.findNodeAt(mouseX, mouseY);
        if (clickedNode != null) {
            this.selectedNodeId = clickedNode;
            this.controller.createMoveAction(clickedNode);
            if (this.onBoardStateChanged != null) {
                this.onBoardStateChanged.run();
            }
            repaint();
            return;
        }

        String clickedProvince = BoardGeometry.findProvinceAt(mouseX, mouseY);
        if (clickedProvince != null) {
            this.selectedProvinceId = clickedProvince;
            if (this.onBoardStateChanged != null) {
                this.onBoardStateChanged.run();
            }
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (this.boardBackgroundImage != null) {
            g2d.drawImage(this.boardBackgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        double scaleX = (double) getWidth() / CANVAS_WIDTH;
        double scaleY = (double) getHeight() / CANVAS_HEIGHT;

        drawNodeAndProvinceGuides(g2d, scaleX, scaleY);
        drawProvinceTributes(g2d, scaleX, scaleY);
        drawConstructedYurts(g2d, scaleX, scaleY);
        drawKhanPiece(g2d, scaleX, scaleY);
        drawPlayerPawns(g2d, scaleX, scaleY);

        g2d.dispose();
    }

    private void drawNodeAndProvinceGuides(Graphics2D g2d, double scaleX, double scaleY) {
        Set<String> reachable = new HashSet<>(
            controller.getReachableNodeIds(this.controller.getCurrentPlayerIndex())
        );

        for (Map.Entry<String, Point> entry : BoardGeometry.getAllNodeCoordinates().entrySet()) {
            String nodeId = entry.getKey();
            Point pt = entry.getValue();
            int sx = (int) Math.round(pt.x * scaleX);
            int sy = (int) Math.round(pt.y * scaleY);
            int r = 16;

            if (nodeId.equals(this.selectedNodeId)) {
                g2d.setColor(new Color(255, 215, 0, 230));
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawOval(sx - r, sy - r, r * 2, r * 2);
            } else if (reachable.contains(nodeId)) {
                g2d.setColor(new Color(40, 220, 90, 210));
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.drawOval(sx - r, sy - r, r * 2, r * 2);
            } else {
                g2d.setColor(new Color(255, 255, 255, 110));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawOval(sx - r, sy - r, r * 2, r * 2);
            }
        }

        if (this.selectedProvinceId != null) {
            Point pt = BoardGeometry.getProvinceCoordinates(this.selectedProvinceId);
            if (pt != null) {
                int sx = (int) Math.round(pt.x * scaleX);
                int sy = (int) Math.round(pt.y * scaleY);
                int r = 20;
                g2d.setColor(new Color(50, 205, 50, 220));
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.drawOval(sx - r, sy - r, r * 2, r * 2);
            }
        }
    }

    private void drawProvinceTributes(Graphics2D g2d, double scaleX, double scaleY) {
        for (Map.Entry<String, Point> entry : BoardGeometry.getAllProvinceCoordinates().entrySet()) {
            String provId = entry.getKey();
            Point center = entry.getValue();
            int count = controller.getProvinceResourceCount(provId);
            int sx = (int) Math.round(center.x * scaleX);
            int sy = (int) Math.round(center.y * scaleY);

            for (int i = 0; i < count; i++) {
                int offsetX = sx - 18 + (i * 12);
                int offsetY = sy - 9;
                g2d.drawImage(this.tributeSprite, offsetX, offsetY, 18, 18, this);
            }
        }
    }

    private void drawConstructedYurts(Graphics2D g2d, double scaleX, double scaleY) {
        for (Map.Entry<String, Point> entry : BoardGeometry.getAllNodeCoordinates().entrySet()) {
            String nodeId = entry.getKey();
            if (controller.nodeHasYurt(nodeId)) {
                Point pt = entry.getValue();
                int sx = (int) Math.round(pt.x * scaleX);
                int sy = (int) Math.round(pt.y * scaleY);
                g2d.drawImage(this.yurtSprite, sx - 13, sy - 26, 26, 26, this);
            }
        }
    }

    private void drawKhanPiece(Graphics2D g2d, double scaleX, double scaleY) {
        Point khanPt = null;
        if (controller.isKhanAtUpgradeArea()) {
            khanPt = BoardGeometry.getUpgradesAreaCoordinate();
        } else {
            String khanProv = controller.getKhanProvinceId();
            if (khanProv != null) {
                khanPt = BoardGeometry.getProvinceCoordinates(khanProv);
            }
        }
        if (khanPt != null && this.khanSprite != null) {
            int sx = (int) Math.round(khanPt.x * scaleX);
            int sy = (int) Math.round(khanPt.y * scaleY);
            g2d.drawImage(this.khanSprite, sx - 16, sy + 4, 32, 32, this);
        }
    }

    private void drawPlayerPawns(Graphics2D g2d, double scaleX, double scaleY) {
        int totalPlayers = Math.max(2, this.controller.getTotalPlayers());
        for (int playerIdx = 0; playerIdx < totalPlayers; playerIdx++) {
            String nodeId = controller.getPlayerNodeId(playerIdx);
            Point nodeCenter = BoardGeometry.getNodeCoordinates(nodeId);
            Image pawnImg = this.playerPawnSprites.get(playerIdx);

            if (nodeCenter != null && pawnImg != null) {
                int sx = (int) Math.round(nodeCenter.x * scaleX);
                int sy = (int) Math.round(nodeCenter.y * scaleY);
                int offset = (playerIdx * 10) - 10;
                g2d.drawImage(pawnImg, sx + offset - 14, sy - 14, 28, 28, this);
            }
        }
    }

    public String getSelectedNodeId() { return this.selectedNodeId; }
    public String getSelectedProvinceId() { return this.selectedProvinceId; }

    public String getPawnLocation(int playerIndex) {
        return controller.getPlayerNodeId(playerIndex);
    }

    public void setPawnLocation(int playerIndex, String nodeId) {
        controller.setPlayerNodeId(playerIndex, nodeId);
        repaint();
    }
}
