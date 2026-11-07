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
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import controller.Controller;
import model.ObservadoIF;
import model.ObservadorIF;
import model.PlayerColor;
import model.TreasureType;

/**
 * Primary Java2D canvas responsible for rendering the game board, player pawns,
 * turn path highlights, constructed yurts, province tributes, active cities with
 * their plunderable treasures, and Genghis Khan.
 */
public class BoardPanel extends JPanel implements ObservadorIF {

    private static final long serialVersionUID = 1L;
    public static final int CANVAS_WIDTH = 980;
    public static final int CANVAS_HEIGHT = 760;

    private final Controller controller;

    private Image boardBackgroundImage;
    private Image khanSprite;

    private Image yurtTributeSprite;
    private Image swordTributeSprite;
    private Image coinTributeSprite;

    private final int TRIBUTE_SIZE = 40;
    private final int TREASURE_SIZE = 24;

    private final String IMAGE_PATH_START = "assets/cities/";
    private final HashMap<String, Image> citiesImages = new HashMap<>();
    private final HashMap<String, Image> treasureSprites = new HashMap<>();
    private HashMap<String, int[]> CITIES_COORDINATE = new HashMap<>();
    private final int CITY_SIZE = 70;
    private final int CITY_OFFSET = CITY_SIZE / 2;

    private final Map<PlayerColor, Color> playerColorsMap;
    private final Map<Integer, Image> playerPawnSprites;

    private String selectedNodeId;
    private String selectedProvinceId;

    public BoardPanel() {
        this.controller = Controller.getController();
        this.playerColorsMap = new HashMap<>();
        this.playerPawnSprites = new HashMap<>();
        this.selectedNodeId = "CENTRAL";
        this.selectedProvinceId = "RUSSIA_YURT1";

        setPreferredSize(new Dimension(CANVAS_WIDTH, 600));
        setBackground(new Color(230, 218, 195));

        loadGameAssets();
        registerMouseClickHandler();
        this.controller.registra(this);
    }

    @Override
    public void notify(ObservadoIF o) {
        String currentPawnNode = this.controller.getPlayerNodeId(this.controller.getCurrentPlayerIndex());
        List<String> turnPath = this.controller.getTurnPathNodeIds(this.controller.getCurrentPlayerIndex());
        if (this.selectedNodeId == null || !turnPath.contains(this.selectedNodeId)) {
            this.selectedNodeId = currentPawnNode;
        }
        repaint();
    }

    private void loadGameAssets() {
        this.boardBackgroundImage = loadOrCreatePlaceholder("assets/tabuleiro.jpg", CANVAS_WIDTH, CANVAS_HEIGHT, new Color(222, 205, 175));
        this.khanSprite = loadOrCreatePlaceholder("assets/khan.png", 32, 32, new Color(40, 40, 40));

        this.yurtTributeSprite = loadOrCreatePlaceholder("assets/tributeType/YURT.jpg", TRIBUTE_SIZE, TRIBUTE_SIZE, new Color(218, 165, 32));
        this.coinTributeSprite = loadOrCreatePlaceholder("assets/tributeType/COIN.jpg", TRIBUTE_SIZE, TRIBUTE_SIZE, new Color(218, 165, 32));
        this.swordTributeSprite = loadOrCreatePlaceholder("assets/tributeType/SWORD.jpg", TRIBUTE_SIZE, TRIBUTE_SIZE, new Color(218, 165, 32));
        
        this.playerColorsMap.put(PlayerColor.RED, Color.RED);
        this.playerColorsMap.put(PlayerColor.BLACK, Color.BLACK);
        this.playerColorsMap.put(PlayerColor.BLUE, Color.BLUE);
        this.playerColorsMap.put(PlayerColor.GREEN, Color.GREEN);
        this.playerColorsMap.put(PlayerColor.YELLOW, Color.YELLOW);

        this.playerPawnSprites.put(0, loadOrCreatePlaceholder("assets/pawn_blue.png", 28, 28, this.playerColorsMap.get(PlayerColor.BLUE)));
        this.playerPawnSprites.put(1, loadOrCreatePlaceholder("assets/pawn_red.png", 28, 28, this.playerColorsMap.get(PlayerColor.RED)));
        this.playerPawnSprites.put(2, loadOrCreatePlaceholder("assets/pawn_green.png", 28, 28, this.playerColorsMap.get(PlayerColor.GREEN)));
        this.playerPawnSprites.put(3, loadOrCreatePlaceholder("assets/pawn_yellow.png", 28, 28, this.playerColorsMap.get(PlayerColor.YELLOW)));
        this.playerPawnSprites.put(4, loadOrCreatePlaceholder("assets/pawn_black.png", 28, 28, this.playerColorsMap.get(PlayerColor.BLACK)));

        this.CITIES_COORDINATE = controller.getCitiesCoordinates();
        for (Map.Entry<String, int[]> entry : CITIES_COORDINATE.entrySet()) {
            String name = entry.getKey();
            String path = this.IMAGE_PATH_START + name + ".jpg";
            Image img = loadOrCreatePlaceholder(path, this.CITY_SIZE, this.CITY_SIZE, new Color(40, 40, 40));
            this.citiesImages.put(name, img);
        }

        for (TreasureType type : TreasureType.values()) {
            String key = type.name();
            BufferedImage img = ImageUtils.readProjectImage("assets/treasureType/" + key + ".jpg");
            
            if (img != null) this.treasureSprites.put(key, img);
            else this.treasureSprites.put(key, createTreasureBadge(key));
        }
    }

    private Image createTreasureBadge(String treasureName) {
        BufferedImage badge = new BufferedImage(TREASURE_SIZE, TREASURE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = badge.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = new Color(140, 90, 45);
        if ("SILK".equals(treasureName)) fill = new Color(155, 50, 165);
        else if ("IRON".equals(treasureName)) fill = new Color(95, 105, 115);
        else if ("SPICE".equals(treasureName)) fill = new Color(210, 95, 30);
        else if ("ANIMAL".equals(treasureName)) fill = new Color(50, 140, 70);

        g2.setColor(fill);
        g2.fillRoundRect(1, 1, TREASURE_SIZE - 2, TREASURE_SIZE - 2, 6, 6);
        g2.setColor(new Color(255, 235, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, TREASURE_SIZE - 2, TREASURE_SIZE - 2, 6, 6);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        String shortLabel = treasureName.substring(0, Math.min(3, treasureName.length()));
        g2.drawString(shortLabel, 3, 15);
        g2.dispose();
        return badge;
    }

    private Image loadOrCreatePlaceholder(String path, int width, int height, Color fallbackColor) {
        BufferedImage loaded = ImageUtils.readProjectImage(path);
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
            List<String> visitedInTurn = this.controller.getTurnPathNodeIds(this.controller.getCurrentPlayerIndex());
            if (!visitedInTurn.contains(clickedNode)) {
                this.controller.createMoveAction(clickedNode);
            }
            repaint();
            return;
        }

        String clickedProvince = BoardGeometry.findProvinceAt(mouseX, mouseY);
        if (clickedProvince != null) {
            this.selectedProvinceId = clickedProvince;
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
        drawCitiesForSack(g2d, scaleX, scaleY);
        drawConstructedYurts(g2d, scaleX, scaleY);
        drawKhanPiece(g2d, scaleX, scaleY);
        drawPlayerPawns(g2d, scaleX, scaleY);

        g2d.dispose();
    }

    private void drawNodeAndProvinceGuides(Graphics2D g2d, double scaleX, double scaleY) {
        int currentPlayer = this.controller.getCurrentPlayerIndex();
        Set<String> reachable = new HashSet<>(controller.getReachableNodeIds(currentPlayer));
        List<String> turnPath = controller.getTurnPathNodeIds(currentPlayer);
        Set<String> visitedSet = new HashSet<>(turnPath);

        if (turnPath.size() > 1) {
            Stroke previousStroke = g2d.getStroke();
            g2d.setColor(new Color(0, 195, 255, 200));
            g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] {8f, 6f}, 0));
            for (int i = 0; i < turnPath.size() - 1; i++) {
                Point p1 = BoardGeometry.getNodeCoordinates(turnPath.get(i));
                Point p2 = BoardGeometry.getNodeCoordinates(turnPath.get(i + 1));
                if (p1 != null && p2 != null) {
                    int x1 = (int) Math.round(p1.x * scaleX);
                    int y1 = (int) Math.round(p1.y * scaleY);
                    int x2 = (int) Math.round(p2.x * scaleX);
                    int y2 = (int) Math.round(p2.y * scaleY);
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
            g2d.setStroke(previousStroke);
        }

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
            } else if (visitedSet.contains(nodeId)) {
                g2d.setColor(new Color(0, 195, 255, 230));
                g2d.setStroke(new BasicStroke(2.8f));
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
                Image tmp = this.swordTributeSprite;
                if (provId.contains("COIN")) tmp = this.coinTributeSprite;
                else if (provId.contains("YURT")) tmp = this.yurtTributeSprite;
                g2d.drawImage(tmp, offsetX, offsetY, TRIBUTE_SIZE, TRIBUTE_SIZE, this);
            }
        }
    }

    private void drawCitiesForSack(Graphics2D g2d, double scaleX, double scaleY) {
        List<String> cities = controller.getCitiesToSack();
        int[][] cornerOffsets = new int[][] {
            {-CITY_OFFSET - 4, -CITY_OFFSET - 4},
            {CITY_OFFSET - TREASURE_SIZE + 4, -CITY_OFFSET - 4},
            {-CITY_OFFSET - 4, CITY_OFFSET - TREASURE_SIZE + 4},
            {CITY_OFFSET - TREASURE_SIZE + 4, CITY_OFFSET - TREASURE_SIZE + 4}
        };

        for (String name : cities) {
            int[] pos = this.CITIES_COORDINATE.get(name);
            if (pos != null) {
                int centerX = (int) Math.round(pos[0] * scaleX);
                int centerY = (int) Math.round(pos[1] * scaleY);
                int sx = centerX - this.CITY_OFFSET;
                int sy = centerY - this.CITY_OFFSET;

                g2d.drawImage(this.citiesImages.get(name), sx, sy, this.CITY_SIZE, this.CITY_SIZE, this);

                List<String> treasures = controller.getCityTreasures(name);
                for (int i = 0; i < treasures.size() && i < cornerOffsets.length; i++) {
                    String tName = treasures.get(i);
                    Image tImg = this.treasureSprites.get(tName);
                    if (tImg != null) {
                        int tx = centerX + cornerOffsets[i][0];
                        int ty = centerY + cornerOffsets[i][1];
                        g2d.drawImage(tImg, tx, ty, TREASURE_SIZE, TREASURE_SIZE, this);
                    }
                }
            }
        }
    }

    private void drawConstructedYurts(Graphics2D g2d, double scaleX, double scaleY) {
        for (Map.Entry<String, Point> entry : BoardGeometry.getAllNodeCoordinates().entrySet()) {
            String nodeId = entry.getKey();
            PlayerColor color = controller.getYurtColor(nodeId);
            if (color != null) {
                Point pt = entry.getValue();
                int sx = (int) Math.round(pt.x * scaleX);
                int sy = (int) Math.round(pt.y * scaleY);
                int r = 12;
                
                g2d.setColor(this.playerColorsMap.get(color));
                g2d.fillOval(sx - r, sy - r, r * 2, r * 2);
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
                khanPt = BoardGeometry.getKhanProvinceCoordinates(khanProv);
            }
        }
        if (khanPt != null && this.khanSprite != null) {
            int sx = (int) Math.round(khanPt.x * scaleX);
            int sy = (int) Math.round(khanPt.y * scaleY);
            g2d.drawImage(this.khanSprite, sx - 16, sy - 16, 32, 32, this);
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
}
