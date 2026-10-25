package view;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps logical board identifiers from model.Map to 2D pixel coordinates
 * calibrated for the 980x760 board image (img/tabuleiro.jpg).
 */
public final class BoardGeometry {

    public static final int NODE_HIT_RADIUS = 18;
    public static final int PROVINCE_HIT_RADIUS = 22;

    private static final Map<String, Point> NODE_COORDINATES = new LinkedHashMap<>();
    private static final Map<String, Point> PROVINCE_COORDINATES = new LinkedHashMap<>();
    private static final Point UPGRADES_AREA_COORDINATE = new Point(260, 705);

    static {
        // Central Node (Karakorum)
        NODE_COORDINATES.put("CENTRAL", new Point(490, 380));

        // RUSSIA: Sarai (0..2), Moscou (3..5), Kiev (6..8), Trade Posts (9, 10)
        NODE_COORDINATES.put("RUSSIA_0", new Point(517, 73));
        NODE_COORDINATES.put("RUSSIA_1", new Point(653, 151));
        NODE_COORDINATES.put("RUSSIA_2", new Point(512, 151));

        NODE_COORDINATES.put("RUSSIA_3", new Point(384, 139));
        NODE_COORDINATES.put("RUSSIA_4", new Point(356, 316));
        NODE_COORDINATES.put("RUSSIA_5", new Point(275, 199));

        NODE_COORDINATES.put("RUSSIA_6", new Point(190, 55));
        NODE_COORDINATES.put("RUSSIA_7", new Point(76, 195));
        NODE_COORDINATES.put("RUSSIA_8", new Point(148, 275));

        NODE_COORDINATES.put("RUSSIA_9", new Point(315, 35));
        NODE_COORDINATES.put("RUSSIA_10", new Point(751, 162));

        // CHINA: Pequim (0..2), Kaesong (3..5), Cantao (6..8), Trade Posts (9, 10)
        NODE_COORDINATES.put("CHINA_0", new Point(701, 386));
        NODE_COORDINATES.put("CHINA_1", new Point(805, 503));
        NODE_COORDINATES.put("CHINA_2", new Point(624, 542));

        NODE_COORDINATES.put("CHINA_3", new Point(775, 244));
        NODE_COORDINATES.put("CHINA_4", new Point(919, 212));
        NODE_COORDINATES.put("CHINA_5", new Point(858, 373));

        NODE_COORDINATES.put("CHINA_6", new Point(879, 578));
        NODE_COORDINATES.put("CHINA_7", new Point(919, 680));
        NODE_COORDINATES.put("CHINA_8", new Point(738, 666));

        NODE_COORDINATES.put("CHINA_9", new Point(866, 471));
        NODE_COORDINATES.put("CHINA_10", new Point(596, 656));

        // PERSIA: Samarcanda (0..2), Cabul (3..5), Bagda (6..8), Trade Posts (9, 10)
        NODE_COORDINATES.put("PERSIA_0", new Point(275, 351));
        NODE_COORDINATES.put("PERSIA_1", new Point(336, 469));
        NODE_COORDINATES.put("PERSIA_2", new Point(185, 445));

        NODE_COORDINATES.put("PERSIA_3", new Point(423, 536));
        NODE_COORDINATES.put("PERSIA_4", new Point(491, 624));
        NODE_COORDINATES.put("PERSIA_5", new Point(356, 672));

        NODE_COORDINATES.put("PERSIA_6", new Point(140, 471));
        NODE_COORDINATES.put("PERSIA_7", new Point(214, 562));
        NODE_COORDINATES.put("PERSIA_8", new Point(55, 569));

        NODE_COORDINATES.put("PERSIA_9", new Point(140, 316));
        NODE_COORDINATES.put("PERSIA_10", new Point(257, 565));

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
        for (Map.Entry<String, Point> entry : NODE_COORDINATES.entrySet()) {
            if (entry.getValue().distance(mouseX, mouseY) <= NODE_HIT_RADIUS) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String findProvinceAt(int mouseX, int mouseY) {
        for (Map.Entry<String, Point> entry : PROVINCE_COORDINATES.entrySet()) {
            if (entry.getValue().distance(mouseX, mouseY) <= PROVINCE_HIT_RADIUS) {
                return entry.getKey();
            }
        }
        return null;
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
