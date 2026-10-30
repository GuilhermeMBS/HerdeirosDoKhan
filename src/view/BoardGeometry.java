package view;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import model.Region;

/**
 * Maps logical board identifiers from model.Map to exact 2D pixel coordinates
 * on the 980x760 board image (img/tabuleiro.jpg).
 */
public final class BoardGeometry {

    public static final int NODE_HIT_RADIUS = 22;
    public static final int PROVINCE_HIT_RADIUS = 24;

    private static final Map<String, Point> NODE_COORDINATES = new LinkedHashMap<>();
    private static final Map<String, Point> PROVINCE_COORDINATES = new LinkedHashMap<>();
    private static final Point UPGRADES_AREA_COORDINATE = new Point(260, 705);

    static {
        // Central Node (Karakorum)
        NODE_COORDINATES.put("CENTRAL", new Point(490, 380));

        // RUSSIA: Sarai (0..2), Moscou (3..5), Kiev (6..8), Trade Posts (9, 10)
        addNodeCoordinates(Region.CHINA,
        		new Point(709, 324),
        		new Point(723, 433),
        		new Point(624, 415),
                new Point(831, 170),
                new Point(924, 228),
                new Point(837, 279),
                new Point(837, 526),
                new Point(862, 620),
                new Point(755, 577),
                new Point(852, 400), // Trade Post 1
                new Point(612, 577)  // Trade Post 2
        );
        
        addNodeCoordinates(Region.RUSSIA,
        		new Point(511, 171),
                new Point(585, 58),
                new Point(630, 149),
                new Point(420, 128),
                new Point(358, 224),
                new Point(287, 143),
                new Point(160, 72),
                new Point(161, 134),
                new Point(42, 137),
                new Point(295, 52), // Trade Post 1
                new Point(742, 129) // Trade Post 2
        );
        
        addNodeCoordinates(Region.PERSIA, 
        		new Point(321, 400),
                new Point(191, 365),
                new Point(283, 312),
                new Point(431, 465),
                new Point(494, 531),
                new Point(395, 577),
                new Point(67, 388),
                new Point(157, 424),
                new Point(63, 492),
                new Point(156, 232), // Trade Post 1
                new Point(243, 490)  // Trade Post 2
        );

        // 12 Provinces matching model.ProvinceId
        PROVINCE_COORDINATES.put("RUSSIA_YURT1", new Point(210, 106));
        PROVINCE_COORDINATES.put("RUSSIA_SWORD1", new Point(289, 100));
        PROVINCE_COORDINATES.put("RUSSIA_YURT2", new Point(413, 240));
        PROVINCE_COORDINATES.put("RUSSIA_COIN1", new Point(655, 194));

        PROVINCE_COORDINATES.put("CHINA_SWORD1", new Point(719, 272));
        PROVINCE_COORDINATES.put("CHINA_COIN1", new Point(797, 407));
        PROVINCE_COORDINATES.put("CHINA_YURT1", new Point(690, 472));
        PROVINCE_COORDINATES.put("CHINA_SWORD2", new Point(562, 502));

        PROVINCE_COORDINATES.put("PERSIA_YURT1", new Point(504, 417));
        PROVINCE_COORDINATES.put("PERSIA_COIN1", new Point(202, 432));
        PROVINCE_COORDINATES.put("PERSIA_SWORD1", new Point(177, 317));
        PROVINCE_COORDINATES.put("PERSIA_COIN2", new Point(268, 243));
    }
    
    
    private static void addNodeCoordinates(Region region, Point... coordinates) {
    	for (int i = 0; i < coordinates.length; i++) NODE_COORDINATES.put(region.name() + "_" + i, coordinates[i]);
    }
    

    private BoardGeometry() {
    }

    public static String findNodeAt(int mouseX, int mouseY) {
        String closestId = null;
        double closestDist = NODE_HIT_RADIUS;
        for (Map.Entry<String, Point> entry : NODE_COORDINATES.entrySet()) {
            double dist = entry.getValue().distance(mouseX, mouseY);
            if (dist <= closestDist) {
                closestDist = dist;
                closestId = entry.getKey();
            }
        }
        return closestId;
    }

    public static String findProvinceAt(int mouseX, int mouseY) {
        String closestId = null;
        double closestDist = PROVINCE_HIT_RADIUS;
        for (Map.Entry<String, Point> entry : PROVINCE_COORDINATES.entrySet()) {
            double dist = entry.getValue().distance(mouseX, mouseY);
            if (dist <= closestDist) {
                closestDist = dist;
                closestId = entry.getKey();
            }
        }
        return closestId;
    }

    public static Point getNodeCoordinates(String nodeId) {
        if ("Karakorum".equalsIgnoreCase(nodeId)) {
            return NODE_COORDINATES.get("CENTRAL");
        }
        return NODE_COORDINATES.get(nodeId);
    }

    public static Point getProvinceCoordinates(String provinceId) {
        return PROVINCE_COORDINATES.get(provinceId);
    }

    public static Point getUpgradesAreaCoordinate() {
        return UPGRADES_AREA_COORDINATE;
    }

    public static Map<String, Point> getAllNodeCoordinates() {
        return Collections.unmodifiableMap(NODE_COORDINATES);
    }

    public static Map<String, Point> getAllProvinceCoordinates() {
        return Collections.unmodifiableMap(PROVINCE_COORDINATES);
    }
}
