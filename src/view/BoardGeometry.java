package view;

import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class that maps logical board identifiers (nodes, provinces, and special zones)
 * to 2D pixel coordinates on the rendered board canvas.
 * Provides hit-detection methods to translate mouse clicks into board elements.
 */
public final class BoardGeometry {

    /** Radius in pixels used to detect clicks on a topological node. */
    public static final int NODE_HIT_RADIUS = 24;

    /** Radius in pixels used to detect clicks on a province tribute area. */
    public static final int PROVINCE_HIT_RADIUS = 28;

    private static final Map<String, Point> NODE_COORDINATES = new HashMap<>();
    private static final Map<String, Point> PROVINCE_COORDINATES = new HashMap<>();
    private static final Point UPGRADES_AREA_COORDINATE = new Point(880, 680);

    static {
        // Pixel coordinates calibrated for a 980x760 board canvas.
        // Adjust these (X, Y) pairs to match the exact circles on your official board image.
        NODE_COORDINATES.put("Karakorum", new Point(490, 380));
        NODE_COORDINATES.put("Kiev", new Point(195, 210));
        NODE_COORDINATES.put("Samarkand", new Point(340, 510));
        NODE_COORDINATES.put("Beijing", new Point(770, 260));
        NODE_COORDINATES.put("PersiaPost", new Point(250, 420));

        PROVINCE_COORDINATES.put("Prov_Russia_1", new Point(290, 240));
        PROVINCE_COORDINATES.put("Prov_Persia_1", new Point(370, 450));
        PROVINCE_COORDINATES.put("Prov_China_1", new Point(660, 310));
    }

    private BoardGeometry() {
        // Prevents instantiation of utility class.
    }

    /**
     * Resolves a screen click (mouseX, mouseY) to a topological node identifier.
     *
     * @param mouseX The horizontal pixel coordinate of the mouse click.
     * @param mouseY The vertical pixel coordinate of the mouse click.
     * @return The clicked node ID, or null if no node lies within the hit radius.
     */
    public static String findNodeAt(int mouseX, int mouseY) {
        for (Map.Entry<String, Point> entry : NODE_COORDINATES.entrySet()) {
            Point center = entry.getValue();
            if (center.distance(mouseX, mouseY) <= NODE_HIT_RADIUS) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Resolves a screen click (mouseX, mouseY) to a province identifier.
     *
     * @param mouseX The horizontal pixel coordinate of the mouse click.
     * @param mouseY The vertical pixel coordinate of the mouse click.
     * @return The clicked province ID, or null if no province lies within the hit radius.
     */
    public static String findProvinceAt(int mouseX, int mouseY) {
        for (Map.Entry<String, Point> entry : PROVINCE_COORDINATES.entrySet()) {
            Point center = entry.getValue();
            if (center.distance(mouseX, mouseY) <= PROVINCE_HIT_RADIUS) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Retrieves the pixel coordinates for a given node ID.
     *
     * @param nodeId The logical identifier of the node.
     * @return The 2D Point on the canvas, or null if unregistered.
     */
    public static Point getNodeCoordinates(String nodeId) {
        return NODE_COORDINATES.get(nodeId);
    }

    /**
     * Retrieves the pixel coordinates for a given province ID.
     *
     * @param provinceId The logical identifier of the province.
     * @return The 2D Point on the canvas, or null if unregistered.
     */
    public static Point getProvinceCoordinates(String provinceId) {
        return PROVINCE_COORDINATES.get(provinceId);
    }

    /**
     * Retrieves the pixel coordinates for the Upgrades Area where the Khan can be placed.
     *
     * @return The 2D Point of the upgrades market.
     */
    public static Point getUpgradesAreaCoordinate() {
        return UPGRADES_AREA_COORDINATE;
    }

    /**
     * Returns an unmodifiable view of all registered node coordinates.
     *
     * @return Map of node IDs to pixel coordinates.
     */
    public static Map<String, Point> getAllNodeCoordinates() {
        return Collections.unmodifiableMap(NODE_COORDINATES);
    }

    /**
     * Returns an unmodifiable view of all registered province coordinates.
     *
     * @return Map of province IDs to pixel coordinates.
     */
    public static Map<String, Point> getAllProvinceCoordinates() {
        return Collections.unmodifiableMap(PROVINCE_COORDINATES);
    }
}
