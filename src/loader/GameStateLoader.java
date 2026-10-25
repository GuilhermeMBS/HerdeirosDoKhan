package loader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import model.GameData;
import view.BoardPanel;

/**
 * Persistence manager responsible for serializing and restoring the current match state
 * to and from a key-value properties file.
 */
public class GameStateLoader {

    /**
     * Saves the current topological and turn state of the match into a file.
     *
     * @param filePath         The target file path on disk.
     * @param modelFacade      The active domain model facade.
     * @param boardPanel       The active board view storing pawn coordinates.
     * @param activePlayerName The name of the player whose turn is currently active.
     * @return True if the file was written successfully; false on I/O failure.
     */
    public boolean saveMatchState(
        String filePath,
        GameData modelFacade,
        BoardPanel boardPanel,
        String activePlayerName
    ) {
        Properties props = new Properties();
        props.setProperty("activePlayer", activePlayerName);
        props.setProperty("alternativeKarakorum", String.valueOf(modelFacade.isAlternativeKarakorum()));
        props.setProperty("khanAtUpgrades", String.valueOf(modelFacade.isKhanAtUpgradeArea()));
        props.setProperty("pawn.0.node", boardPanel.getPawnLocation(0));
        props.setProperty("pawn.1.node", boardPanel.getPawnLocation(1));

        for (String provId : modelFacade.getAllProvinceIds()) {
            int count = modelFacade.getProvinceResourceCount(provId);
            props.setProperty("province." + provId, String.valueOf(count));
        }

        try (FileOutputStream out = new FileOutputStream(filePath)) {
            props.store(out, "Heirs of the Khan - Saved Match State");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Restores a previously saved match state from disk into the Model and View.
     *
     * @param filePath    The source file path on disk.
     * @param modelFacade The domain model facade to populate.
     * @param boardPanel  The board panel to update and repaint.
     * @return True if loaded successfully; false if the file does not exist or fails to parse.
     */
    public boolean loadMatchState(String filePath, GameData modelFacade, BoardPanel boardPanel) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(filePath)) {
            props.load(in);
        } catch (IOException e) {
            return false;
        }

        String pawn0Node = props.getProperty("pawn.0.node", "Karakorum");
        String pawn1Node = props.getProperty("pawn.1.node", "Karakorum");
        boardPanel.setPawnLocation(0, pawn0Node);
        boardPanel.setPawnLocation(1, pawn1Node);

        for (String provId : modelFacade.getAllProvinceIds()) {
            String rawCount = props.getProperty("province." + provId);
            if (rawCount != null) {
                try {
                    int count = Integer.parseInt(rawCount);
                    modelFacade.setProvinceResourceCount(provId, count);
                } catch (NumberFormatException ignored) {
                    // Keeps default resource count if property is malformed.
                }
            }
        }

        boardPanel.repaint();
        return true;
    }
}
