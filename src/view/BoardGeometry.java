package view;

import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import controller.Controller;

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
    	HashMap<String, int[]> nodeCoordinates = Controller.getController().getNodeCoordinates();
    	for (Map.Entry<String, int[]> entry : nodeCoordinates.entrySet()) {
    		String id = entry.getKey();
    		int[] pos = entry.getValue();
    		NODE_COORDINATES.put(id, new Point(pos[0], pos[1]));
    	}
    	
    	HashMap<String, int[]> provinceCoord = Controller.getController().getProvinceCoordinates();
    	for (Map.Entry<String, int[]> entry : provinceCoord.entrySet()) {
    		String id = entry.getKey();
    		int[] pos = entry.getValue();
    		PROVINCE_COORDINATES.put(id, new Point(pos[0], pos[1]));
    	}
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
