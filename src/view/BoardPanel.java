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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import controller.Controller;
import model.ObservableIF;
import model.ObserverIF;
import model.PlayerColor;
import model.TreasureType;

/**
 * Primary Java2D canvas responsible for rendering the game board, player pawns,
 * turn path highlights, constructed yurts, province tributes, active cities with
 * their plunderable treasures, and Genghis Khan.
 */
public class BoardPanel extends JPanel implements ObserverIF {

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

    private final HashMap<String, Image> treasureSprites = new HashMap<>();

    private final HashMap<String, Image> citiesImages = new HashMap<>();
    private HashMap<String, int[]> CITIES_COORDINATE = new HashMap<>();
    private final int CITY_SIZE = 70;
    private final int CITY_OFFSET = CITY_SIZE / 2;

    private final Map<PlayerColor, Color> playerColorsMap;
    private final Map<Integer, Image> playerPawnSprites;

    // Vote track, measured on the board image (980 x 760 coordinates)
    private final Point2D.Double firstPlayerScoreCoord = new Point2D.Double(385, 685);  // space 1
    private final double gapBetweenScores = 62.5;
    private final Point2D.Double bottomPlayerScoreCoord = new Point2D.Double(915, 728); // space 11
    private final int scoreSize = 45;
    private final int maxScoreFirstLine = 10;
    private final int lastScoreSpace = 17;
    
    private final HashMap<String, String> bonusCardpathImg;
    private final HashMap<String, Image> bonusCardImg = new HashMap<>();
    private final HashMap<String, int[]> bonusCardCoordinates;
    private final int bonusCardSize = 60;
    
    private String selectedNodeId;
    private String selectedProvinceId;

    public BoardPanel() {
        this.controller = Controller.getController();
        this.playerColorsMap = new HashMap<>();
        this.playerPawnSprites = new HashMap<>();
        this.selectedNodeId = "CENTRAL";
        this.selectedProvinceId = "RUSSIA_YURT1";
        
        this.bonusCardpathImg = controller.getBonusCardImgPath();
        this.bonusCardCoordinates = controller.getBonusCardCoordinates();

        setPreferredSize(new Dimension(CANVAS_WIDTH, 600));
        setBackground(new Color(230, 218, 195));

        loadGameAssets();
        registerMouseClickHandler();
        this.controller.register(this);
    }

    @Override
    public void notify(ObservableIF o) {
        String currentPawnNode = this.controller.getPlayerNodeId(this.controller.getCurrentPlayerIndex());
        List<String> turnPath = this.controller.getTurnPathNodeIds(this.controller.getCurrentPlayerIndex());
        if (this.selectedNodeId == null || !turnPath.contains(this.selectedNodeId)) {
            this.selectedNodeId = currentPawnNode;
        }
        repaint();
    }

    private void loadGameAssets() {
        this.boardBackgroundImage = loadOrCreatePlaceholder("assets/tabuleiro.jpg", CANVAS_WIDTH, CANVAS_HEIGHT, new Color(222, 205, 175));
        this.khanSprite = loadOrCreatePlaceholder("assets/khan.png", 32, 32, new Color(255, 255, 255));

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
        
        for (Map.Entry<String, String> entry : this.bonusCardpathImg.entrySet()) {
        	String bonusCard = entry.getKey();
        	String path = entry.getValue();
        	this.bonusCardImg.put(bonusCard, loadOrCreatePlaceholder(path, this.bonusCardSize, this.bonusCardSize, new Color(125, 125, 125)));
        }

        this.CITIES_COORDINATE = controller.getCitiesCoordinates();
        for (Map.Entry<String, String> entry : controller.getCitiesImgPath().entrySet()) {
            Image img = loadOrCreatePlaceholder(entry.getValue(), this.CITY_SIZE, this.CITY_SIZE, new Color(40, 40, 40));
            this.citiesImages.put(entry.getKey(), img);
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
            // Clicking on one of the player's own pawns picks it for the turn
            // (only works before the column is confirmed; otherwise the Controller ignores it)
            int current = this.controller.getCurrentPlayerIndex();
            // The search starts at the pawn after the selected one, so that clicking a node
            // with both pawns (like the start) alternates between them
            int count = this.controller.getPawnCount(current);
            for (int step = 1; step <= count; step++) {
                int pawnIdx = (this.controller.getSelectedPawn() + step) % count;
                if (clickedNode.equals(this.controller.getPawnNodeId(current, pawnIdx))
                        && this.controller.selectPawn(pawnIdx)) {
                    repaint();
                    return;
                }
            }
            this.selectedNodeId = clickedNode;
            List<String> visitedInTurn = this.controller.getTurnPathNodeIds(this.controller.getCurrentPlayerIndex());
            if (!visitedInTurn.contains(clickedNode)) {
                // The pawn does not move yet: the node only becomes the selected destination
                // (if it is reachable) and the move happens on "Confirm Move".
                // Clicking an unreachable node does nothing.
                this.controller.selectMoveDestination(clickedNode);
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
        drawBonusCard(g2d, scaleX, scaleY);
        drawKhanPiece(g2d, scaleX, scaleY);
        drawPlayerPawns(g2d, scaleX, scaleY);
        drawPlayersScores(g2d, scaleX, scaleY);

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

        drawPendingMoveDestination(g2d, scaleX, scaleY);

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

    /**
     * Draws a thick orange ring on the destination selected by the player,
     * which is waiting for the "Confirm Move" button.
     */
    private void drawPendingMoveDestination(Graphics2D g2d, double scaleX, double scaleY) {
        String pendingNode = this.controller.getPendingMoveNodeId();
        if (pendingNode == null) {
            return;
        }
        Point pt = BoardGeometry.getNodeCoordinates(pendingNode);
        if (pt == null) {
            return;
        }
        int sx = (int) Math.round(pt.x * scaleX);
        int sy = (int) Math.round(pt.y * scaleY);
        int r = 20;
        g2d.setColor(new Color(255, 140, 0, 240));
        g2d.setStroke(new BasicStroke(4.5f));
        g2d.drawOval(sx - r, sy - r, r * 2, r * 2);
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
    
    private void drawBonusCard(Graphics2D g2d, double scaleX, double scaleY) {
    	for (Map.Entry<String, Point> entry : BoardGeometry.getAllNodeCoordinates().entrySet()) {
    		String card = controller.getBonusCardName(entry.getKey());
    		if (card == null) continue;
    		
    		Image img = this.bonusCardImg.get(card);
    		int[] pos = this.bonusCardCoordinates.get(entry.getKey());
    		
    		if (img == null || pos == null) continue;
    		
    		Point pt = new Point(pos[0], pos[1]);
    		int sx = (int) Math.round(pt.x * scaleX);
    		int sy = (int) Math.round(pt.y * scaleY);
    		
    		g2d.drawImage(img, sx - this.bonusCardSize / 2, sy - this.bonusCardSize / 2, this.bonusCardSize, this.bonusCardSize, this);
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
            Image pawnImg = this.playerPawnSprites.get(playerIdx);
            int pawnCount = controller.getPawnCount(playerIdx);
            for (int pawnIdx = 0; pawnIdx < pawnCount; pawnIdx++) {
                String nodeId = controller.getPawnNodeId(playerIdx, pawnIdx);
                Point nodeCenter = BoardGeometry.getNodeCoordinates(nodeId);

                if (nodeCenter != null && pawnImg != null) {
                    int sx = (int) Math.round(nodeCenter.x * scaleX);
                    int sy = (int) Math.round(nodeCenter.y * scaleY);
                    int offset = (playerIdx * 10) - 10;
                    // The second pawn is drawn a bit lower so it does not hide the first one
                    int offsetY = pawnIdx * 12;
                    g2d.drawImage(pawnImg, sx + offset - 14, sy + offsetY - 14, 28, 28, this);

                    // Ring around the pawn that plays (or will play) this turn
                    if (pawnCount > 1 && playerIdx == controller.getCurrentPlayerIndex()
                            && pawnIdx == controller.getSelectedPawn()) {
                        g2d.setColor(Color.WHITE);
                        g2d.setStroke(new BasicStroke(3f));
                        g2d.drawOval(sx + offset - 17, sy + offsetY - 17, 34, 34);
                    }
                }
            }
        }
    }
    
    /**
     * Finds the center of the vote track space for a number of votes, in board
     * coordinates (980 x 760). The track is an "S": votes 1 to 10 go from left to
     * right on the top row, and votes 11 to 17 come back from right to left on the
     * bottom row (the 2/3/4/5-player icons sit on spaces 10, 14, 16 and 17).
     * @param votes number of votes
     * @return the center of the space, or null for 0 votes (the marker stays off the track)
     */
    private Point2D.Double getScoreSlotCenter(int votes) {
        if (votes <= 0) {
            return null;
        }
        int space = Math.min(votes, this.lastScoreSpace);
        if (space <= this.maxScoreFirstLine) {
            // Top row: space 1 is the first circle, then one gap to the right per vote
            return new Point2D.Double(
                this.firstPlayerScoreCoord.x + (space - 1) * this.gapBetweenScores,
                this.firstPlayerScoreCoord.y);
        }
        // Bottom row: space 11 is the rightmost circle, then one gap to the LEFT per vote
        int stepsOnBottomRow = space - (this.maxScoreFirstLine + 1);
        return new Point2D.Double(
            this.bottomPlayerScoreCoord.x - stepsOnBottomRow * this.gapBetweenScores,
            this.bottomPlayerScoreCoord.y);
    }

    /**
     * Draws one marker (a circle) on the vote track. The position and the size
     * follow the window zoom, so the marker always stays inside its space.
     */
    private void drawScoreMarker(Graphics2D g2d, int votes, Color fill, double scaleX, double scaleY) {
        Point2D.Double center = getScoreSlotCenter(votes);
        if (center == null) {
            return;
        }
        int width = (int) Math.round(this.scoreSize * scaleX);
        int height = (int) Math.round(this.scoreSize * scaleY);
        int x = (int) Math.round(center.x * scaleX) - width / 2;
        int y = (int) Math.round(center.y * scaleY) - height / 2;

        g2d.setColor(fill);
        g2d.fillOval(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval(x, y, width, height);
    }

    /**
     * Draws the vote track: one colored marker per player and a white marker
     * with the sum of all votes (the one that shows when the match will end).
     */
    private void drawPlayersScores(Graphics2D g2d, double scaleX, double scaleY) {
        int totalPlayers = controller.getTotalPlayers();
        int currentPlayer = controller.getCurrentPlayerIndex();

        int totalScore = 0;

        for (int step = 0; step < totalPlayers; step++) {
            int i = (step + currentPlayer + 1) % totalPlayers;

            int score = controller.getPlayerVotes(i);
            totalScore += score;

            Color fill = this.playerColorsMap.get(controller.getPlayerColor(i));
            if (fill == null) fill = Color.GRAY;
            drawScoreMarker(g2d, score, fill, scaleX, scaleY);
        }

        drawScoreMarker(g2d, totalScore, Color.WHITE, scaleX, scaleY);
    }

    public String getSelectedNodeId() { return this.selectedNodeId; }
    public String getSelectedProvinceId() { return this.selectedProvinceId; }
}
