package view;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
        NODE_COORDINATES.put("RUSSIA_0", new Point(523, 61));
        NODE_COORDINATES.put("RUSSIA_1", new Point(645, 162));
        NODE_COORDINATES.put("RUSSIA_2", new Point(521, 167));

        NODE_COORDINATES.put("RUSSIA_3", new Point(400, 110));
        NODE_COORDINATES.put("RUSSIA_4", new Point(361, 247));
        NODE_COORDINATES.put("RUSSIA_5", new Point(278, 167));

        NODE_COORDINATES.put("RUSSIA_6", new Point(189, 56));
        NODE_COORDINATES.put("RUSSIA_7", new Point(189, 185));
        NODE_COORDINATES.put("RUSSIA_8", new Point(61, 141));

        NODE_COORDINATES.put("RUSSIA_9", new Point(308, 35));
        NODE_COORDINATES.put("RUSSIA_10", new Point(757, 158));

        // CHINA: Pequim (0..2), Kaesong (3..5), Cantao (6..8), Trade Posts (9, 10)
        NODE_COORDINATES.put("CHINA_0", new Point(699, 371));
        NODE_COORDINATES.put("CHINA_1", new Point(792, 473));
        NODE_COORDINATES.put("CHINA_2", new Point(624, 517));

        NODE_COORDINATES.put("CHINA_3", new Point(797, 225));
        NODE_COORDINATES.put("CHINA_4", new Point(930, 198));
        NODE_COORDINATES.put("CHINA_5", new Point(862, 344));

        NODE_COORDINATES.put("CHINA_6", new Point(875, 557));
        NODE_COORDINATES.put("CHINA_7", new Point(915, 656));
        NODE_COORDINATES.put("CHINA_8", new Point(738, 650));

        NODE_COORDINATES.put("CHINA_9", new Point(866, 450));
        NODE_COORDINATES.put("CHINA_10", new Point(592, 636));

        // PERSIA: Samarcanda (0..2), Cabul (3..5), Bagda (6..8), Trade Posts (9, 10)
        NODE_COORDINATES.put("PERSIA_0", new Point(278, 335));
        NODE_COORDINATES.put("PERSIA_1", new Point(335, 450));
        NODE_COORDINATES.put("PERSIA_2", new Point(185, 433));

        NODE_COORDINATES.put("PERSIA_3", new Point(424, 517));
        NODE_COORDINATES.put("PERSIA_4", new Point(490, 610));
        NODE_COORDINATES.put("PERSIA_5", new Point(357, 654));

        NODE_COORDINATES.put("PERSIA_6", new Point(141, 460));
        NODE_COORDINATES.put("PERSIA_7", new Point(211, 553));
        NODE_COORDINATES.put("PERSIA_8", new Point(56, 561));

        NODE_COORDINATES.put("PERSIA_9", new Point(138, 300));
        NODE_COORDINATES.put("PERSIA_10", new Point(255, 557));

        // 12 Provinces matching model.ProvinceId
        PROVINCE_COORDINATES.put("RUSSIA_YURT1", new Point(210, 235));
        PROVINCE_COORDINATES.put("RUSSIA_SWORD1", new Point(305, 110));
        PROVINCE_COORDINATES.put("RUSSIA_YURT2", new Point(425, 225));
        PROVINCE_COORDINATES.put("RUSSIA_COIN1", new Point(645, 225));

        PROVINCE_COORDINATES.put("CHINA_SWORD1", new Point(620, 310));
        PROVINCE_COORDINATES.put("CHINA_COIN1", new Point(775, 325));
        PROVINCE_COORDINATES.put("CHINA_YURT1", new Point(700, 485));
        PROVINCE_COORDINATES.put("CHINA_SWORD2", new Point(575, 515));

        PROVINCE_COORDINATES.put("PERSIA_YURT1", new Point(475, 425));
        PROVINCE_COORDINATES.put("PERSIA_COIN1", new Point(330, 505));
        PROVINCE_COORDINATES.put("PERSIA_SWORD1", new Point(195, 315));
        PROVINCE_COORDINATES.put("PERSIA_COIN2", new Point(205, 415));
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
