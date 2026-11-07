package loader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import controller.Controller;
import model.ColumnColor;
import model.GameData;
import view.BoardPanel;

/**
 * Persistence manager responsible for serializing and restoring the current match state
 * (including Khan location, player pawns, and active turn path) to and from a properties file.
 */
public class GameStateLoader {

    public boolean saveMatchState(
        String filePath,
        BoardPanel boardPanel,
        String activePlayerName
    ) {
        GameData data = GameData.getData();
        Controller controller = Controller.getController();
        Properties props = new Properties();

        int currentPlayerInd = controller.getCurrentPlayerIndex();
        props.setProperty("activePlayer", activePlayerName);
        props.setProperty("activePlayerIndex", String.valueOf(currentPlayerInd));
        props.setProperty("alternativeKarakorum", String.valueOf(data.isAlternativeKarakorum()));

        // Save Khan state
        props.setProperty("khanAtUpgrades", String.valueOf(data.isKhanAtUpgradeArea()));
        String khanProvince = data.getKhanProvinceId();
        props.setProperty("khanProvince", khanProvince != null ? khanProvince : "NONE");

        // Save active turn state
        ColumnColor chosenCol = controller.getChosenColumn();
        props.setProperty("chosenColumn", chosenCol != null ? chosenCol.name() : "NONE");
        props.setProperty("movementsLeft", String.valueOf(data.getMovementsLeft(currentPlayerInd)));
        props.setProperty("handsLeft", String.valueOf(data.getHandsLeft(currentPlayerInd)));
        props.setProperty("khanPending", String.valueOf(data.isKhanPending(currentPlayerInd)));

        // Save all players' pawn locations and turn paths
        int totalPlayers = Math.max(2, data.getPlayerCount());
        props.setProperty("playerCount", String.valueOf(totalPlayers));
        for (int i = 0; i < totalPlayers; i++) {
            props.setProperty("pawn." + i + ".node", controller.getPlayerNodeId(i));
            List<String> turnPath = data.getTurnPathNodeIds(i);
            props.setProperty("pawn." + i + ".turnPath", String.join(",", turnPath));
        }

        // Save province resource counts
        for (String provId : data.getAllProvinceIds()) {
            int count = data.getProvinceResourceCount(provId);
            props.setProperty("province." + provId, String.valueOf(count));
        }

        try (FileOutputStream out = new FileOutputStream(filePath)) {
            props.store(out, "Heirs of the Khan - Saved Match State");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean loadMatchState(String filePath, BoardPanel boardPanel) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(filePath)) {
            props.load(in);
        } catch (IOException e) {
            return false;
        }

        GameData data = GameData.getData();
        Controller controller = Controller.getController();

        int totalPlayers = data.getPlayerCount();
        for (int i = 0; i < totalPlayers; i++) {
            String pawnNode = props.getProperty("pawn." + i + ".node", "CENTRAL");
            controller.setPlayerNodeId(i, pawnNode);

            String rawPath = props.getProperty("pawn." + i + ".turnPath", "");
            if (!rawPath.trim().isEmpty()) {
                List<String> pathNodes = new ArrayList<>(Arrays.asList(rawPath.split(",")));
                data.setTurnPathNodeIds(i, pathNodes);
            }
        }

        // Restore Khan state
        boolean khanAtUpgrades = Boolean.parseBoolean(props.getProperty("khanAtUpgrades", "false"));
        String khanProvince = props.getProperty("khanProvince", "NONE");
        if ("NONE".equalsIgnoreCase(khanProvince)) {
            khanProvince = null;
        }
        data.restoreKhanState(khanAtUpgrades, khanProvince);

        // Restore province resources
        for (String provId : data.getAllProvinceIds()) {
            String rawCount = props.getProperty("province." + provId);
            if (rawCount != null) {
                try {
                    int count = Integer.parseInt(rawCount);
                    data.setProvinceResourceCount(provId, count);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Restore active turn state
        try {
            int activeInd = Integer.parseInt(props.getProperty("activePlayerIndex", "0"));
            String rawColumn = props.getProperty("chosenColumn", "NONE");
            ColumnColor col = null;
            if (!"NONE".equalsIgnoreCase(rawColumn)) {
                col = ColumnColor.valueOf(rawColumn);
            }
            controller.restoreControllerTurnState(activeInd, col);

            int moves = Integer.parseInt(props.getProperty("movementsLeft", "0"));
            int hands = Integer.parseInt(props.getProperty("handsLeft", "0"));
            boolean khanPend = Boolean.parseBoolean(props.getProperty("khanPending", "false"));
            data.setMovementsLeft(activeInd, moves);
            data.setHandsLeft(activeInd, hands);
            data.setKhanPending(activeInd, khanPend);
        } catch (IllegalArgumentException ignored) {
        }

        boardPanel.repaint();
        return true;
    }
}
