package loader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.Controller;
import model.ColumnColor;
import model.GameData;
import model.TreasureType;
import model.TributeType;
import view.BoardPanel;

/**
 * Persistence manager responsible for saving and restoring the complete match state
 * using plain US-ASCII text (.txt) files selected via JFileChooser.
 */
public class GameStateLoader {

    private static final String FORMAT_HEADER = "HEIRS_OF_THE_KHAN_SAVE_V1";

    /**
     * Saves the complete match state to a plain ASCII text file.
     *
     * @param targetFile destination .txt file chosen by the user
     * @return true if saved successfully, false on I/O or state error
     */
    public boolean saveMatchState(File targetFile) {
        if (targetFile == null) {
            return false;
        }

        GameData data = GameData.getData();
        Controller controller = Controller.getController();
        int totalPlayers = data.getPlayerCount();
        if (totalPlayers < 2) {
            return false;
        }

        int currentPlayerInd = controller.getCurrentPlayerIndex();

        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.US_ASCII)
        )) {
            writeLine(writer, FORMAT_HEADER);
            writeKeyValue(writer, "alternativeKarakorum", String.valueOf(data.isAlternativeKarakorum()));
            writeKeyValue(writer, "playerCount", String.valueOf(totalPlayers));
            writeKeyValue(writer, "activePlayerIndex", String.valueOf(currentPlayerInd));
            writeKeyValue(writer, "totalVotes", String.valueOf(controller.getTotalVotes()));
            writeKeyValue(writer, "endRoundToFinalTurn", String.valueOf(controller.isEndRoundToFinalTurn()));
            writeKeyValue(writer, "finalTurn", String.valueOf(controller.getLastRound()));

            // Save active turn state
            ColumnColor chosenCol = controller.getChosenColumn();
            writeKeyValue(writer, "chosenColumn", chosenCol != null ? chosenCol.name() : "NONE");
            writeKeyValue(writer, "movementsLeft", String.valueOf(data.getMovementsLeft(currentPlayerInd)));
            writeKeyValue(writer, "handsLeft", String.valueOf(data.getHandsLeft(currentPlayerInd)));
            writeKeyValue(writer, "khanPending", String.valueOf(data.isKhanPending(currentPlayerInd)));

            // Save Khan state
            writeKeyValue(writer, "khanAtUpgrades", String.valueOf(data.isKhanAtUpgradeArea()));
            String khanProvince = data.getKhanProvinceId();
            writeKeyValue(writer, "khanProvince", khanProvince != null ? khanProvince : "NONE");

            // Save Upgrades Area (4 fixed slots)
            writeKeyValue(writer, "availableUpgrades", joinStrings(data.getAvailableUpgradeCodes()));

            // Save active cities, remaining deck, and each active city's remaining treasures
            List<String> activeCities = data.getCitiesToSack();
            List<String> asciiActiveCities = new ArrayList<>();
            for (String cityName : activeCities) {
                asciiActiveCities.add(toPureAscii(cityName));
            }
            writeKeyValue(writer, "activeCities", joinStrings(asciiActiveCities));

            List<String> asciiRemainingCities = new ArrayList<>();
            for (String cityName : data.getRemainingCityDeck()) {
                asciiRemainingCities.add(toPureAscii(cityName));
            }
            writeKeyValue(writer, "remainingCities", joinStrings(asciiRemainingCities));

            for (String cityName : activeCities) {
                String asciiName = toPureAscii(cityName);
                writeKeyValue(writer, "city." + asciiName + ".treasures", joinStrings(data.getCityTreasures(cityName)));
            }

            // Save province resource counts
            for (String provId : data.getAllProvinceIds()) {
                writeKeyValue(writer, "province." + provId, String.valueOf(data.getProvinceResourceCount(provId)));
            }

            // Save constructed yurts on board nodes
            for (String nodeId : data.getAllNodeIds()) {
                List<Integer> owners = data.getNodeYurtOwnerIndices(nodeId);
                if (!owners.isEmpty()) {
                    List<String> ownerStrs = new ArrayList<>();
                    for (Integer idx : owners) {
                        ownerStrs.add(String.valueOf(idx));
                    }
                    writeKeyValue(writer, "yurt." + nodeId, joinStrings(ownerStrs));
                }
            }
            
            for (String nodeId : data.getAllNodeIds()) {
            	String card = data.getPostBonusCardName(nodeId);
            	if (card != null) {
            		writeKeyValue(writer, "postBonus." + nodeId, card);
            		writeKeyValue(writer, "postBonusAvailable." + nodeId, String.valueOf(data.getPostBonusState(nodeId)));
            	}
            }

            // Save each player's complete state
            for (int i = 0; i < totalPlayers; i++) {
                String prefix = "player." + i + ".";
                writeKeyValue(writer, prefix + "heir", toPureAscii(data.getPlayerHeirName(i)));

                String secretName = data.getPlayerPrivateCounselorName(i);
                writeKeyValue(writer, prefix + "secretCounselor", secretName != null ? toPureAscii(secretName) : "None");

                writeKeyValue(writer, prefix + "node", data.getPlayerNodeId(i));
                writeKeyValue(writer, prefix + "turnPath", joinStrings(data.getTurnPathNodeIds(i)));

                for (TributeType trib : TributeType.values()) {
                    writeKeyValue(writer, prefix + "tribute." + trib.name(), String.valueOf(data.getPlayerTributeRaw(i, trib)));
                }
                for (TreasureType treas : TreasureType.values()) {
                    writeKeyValue(writer, prefix + "treasure." + treas.name(), String.valueOf(data.getPlayerTreasureRaw(i, treas)));
                }

                writeKeyValue(writer, prefix + "yurtsLeft", String.valueOf(data.getPlayerYurtsLeft(i)));
                writeKeyValue(writer, prefix + "amountYurts", String.valueOf(data.getPlayerAmountYurts(i)));
                writeKeyValue(writer, prefix + "votes", String.valueOf(data.getPlayerVotes(i)));
                writeKeyValue(writer, prefix + "conqueredCities", String.valueOf(data.getPlayerConqueredCities(i)));
                writeKeyValue(writer, prefix + "bonusCard", joinStrings(data.getPlayerBonusCardNames(i)));
                writeKeyValue(writer, prefix + "bonusCardUsed", joinStrings(data.getPlayerUsedBonusCardNames(i)));
                writeKeyValue(writer, prefix + "rewardedRows", String.valueOf(data.getPlayerRewardedRows(i)));

                for (ColumnColor color : ColumnColor.values()) {
                    boolean occupied = !data.isColumnFree(i, color);
                    writeKeyValue(writer, prefix + "column." + color.name() + ".occupied", String.valueOf(occupied));
                    writeKeyValue(writer, prefix + "column." + color.name() + ".upgrades", joinStrings(data.getPlayerColumnUpgrades(i, color)));
                }
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Convenience overload preserving compatibility with path-based callers.
     */
    public boolean saveMatchState(String filePath, BoardPanel boardPanel, String activePlayerName) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        return saveMatchState(new File(filePath));
    }

    /**
     * Loads and restores a complete match from a plain ASCII text (.txt) file.
     * Re-initializes the match with the saved players and Karakorum variant before
     * restoring every entity so no stale state remains.
     *
     * @param sourceFile the saved .txt file selected by the user
     * @return true if loaded successfully, false if invalid or unreadable
     */
    public boolean loadMatchState(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            return false;
        }

        Map<String, String> values = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.US_ASCII)
        )) {
            String firstLine = reader.readLine();
            if (firstLine == null || !FORMAT_HEADER.equals(firstLine.trim())) {
                return false;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int sep = trimmed.indexOf('=');
                if (sep > 0) {
                    String key = trimmed.substring(0, sep).trim();
                    String val = trimmed.substring(sep + 1).trim();
                    values.put(key, val);
                }
            }
        } catch (IOException e) {
            return false;
        }

        int totalPlayers = parseInt(values.get("playerCount"), 0);
        if (totalPlayers < 2 || totalPlayers > 5) {
            return false;
        }

        boolean altKarakorum = Boolean.parseBoolean(values.getOrDefault("alternativeKarakorum", "false"));
        List<String> heirs = new ArrayList<>();
        for (int i = 0; i < totalPlayers; i++) {
            String heirName = values.get("player." + i + ".heir");
            if (heirName == null || heirName.isEmpty()) {
                return false;
            }
            heirs.add(heirName);
        }

        Controller controller = Controller.getController();
        if (!controller.setupMatch(heirs, altKarakorum)) {
            return false;
        }

        GameData data = GameData.getData();
        
        for (String nodeId : data.getAllNodeIds()) {
        	data.restorePostBonusCard(nodeId, values.get("postBonus." + nodeId));
        	
        	boolean available = Boolean.parseBoolean(values.getOrDefault("postBonusAvailable." + nodeId, "false"));
        	data.restorePostBonusAvailable(nodeId, available);
        }

        // Restore player states (including Secret Counselors BEFORE any UI observer checks them)
        for (int i = 0; i < totalPlayers; i++) {
            String prefix = "player." + i + ".";

            String secretName = values.getOrDefault(prefix + "secretCounselor", "None");
            data.restorePrivateCounselor(i, secretName);

            String pawnNode = values.getOrDefault(prefix + "node", "CENTRAL");
            controller.setPlayerNodeId(i, pawnNode);

            List<String> turnPath = splitStrings(values.get(prefix + "turnPath"));
            if (turnPath.isEmpty()) {
                turnPath.add(pawnNode);
            }
            data.setTurnPathNodeIds(i, turnPath);

            for (TributeType trib : TributeType.values()) {
                int qty = parseInt(values.get(prefix + "tribute." + trib.name()), 0);
                data.setPlayerTributeRaw(i, trib, qty);
            }
            for (TreasureType treas : TreasureType.values()) {
                int qty = parseInt(values.get(prefix + "treasure." + treas.name()), 0);
                data.setPlayerTreasureRaw(i, treas, qty);
            }

            int yurtsLeft = parseInt(values.get(prefix + "yurtsLeft"), 12);
            int amountYurts = parseInt(values.get(prefix + "amountYurts"), 0);
            data.setPlayerYurtsState(i, yurtsLeft, amountYurts);

            data.setPlayerVotes(i, parseInt(values.get(prefix + "votes"), 0));
            data.setPlayerConqueredCities(i, parseInt(values.get(prefix + "conqueredCities"), 0));
            data.restorePlayerBonusCard(i, 
            		splitStrings(values.get(prefix + "bonusCard")),
            		splitStrings(values.get(prefix + "bonusCardUsed")));

            for (ColumnColor color : ColumnColor.values()) {
                boolean occupied = Boolean.parseBoolean(values.getOrDefault(prefix + "column." + color.name() + ".occupied", "false"));
                data.setColumnOccupied(i, color, occupied);

                List<String> colUpgrades = splitStrings(values.get(prefix + "column." + color.name() + ".upgrades"));
                data.restorePlayerColumnUpgrades(i, color, colUpgrades);
            }

            int rewardedRows = parseInt(values.get(prefix + "rewardedRows"), 0);
            data.restorePlayerRewardedRows(i, rewardedRows);
        }

        // Restore constructed yurts on board nodes
        for (String nodeId : data.getAllNodeIds()) {
            List<String> ownerList = splitStrings(values.get("yurt." + nodeId));
            for (String ownerStr : ownerList) {
                int ownerIdx = parseInt(ownerStr, -1);
                if (ownerIdx >= 0 && ownerIdx < totalPlayers) {
                    data.restoreNodeYurt(nodeId, ownerIdx);
                }
            }
        }

        // Restore province resource counts
        for (String provId : data.getAllProvinceIds()) {
            String rawCount = values.get("province." + provId);
            if (rawCount != null) {
                data.setProvinceResourceCount(provId, parseInt(rawCount, 1));
            }
        }

        // Restore Khan state
        boolean khanAtUpgrades = Boolean.parseBoolean(values.getOrDefault("khanAtUpgrades", "false"));
        String khanProvince = values.getOrDefault("khanProvince", "NONE");
        if ("NONE".equalsIgnoreCase(khanProvince)) {
            khanProvince = null;
        }
        data.restoreKhanState(khanAtUpgrades, khanProvince);

        // Restore Upgrades Area
        List<String> availableUpgrades = splitStrings(values.get("availableUpgrades"));
        if (!availableUpgrades.isEmpty()) {
            data.restoreAvailableUpgrades(availableUpgrades);
        }

        // Restore active cities, remaining deck, and city treasures
        List<String> activeCities = splitStrings(values.get("activeCities"));
        List<String> remainingCities = splitStrings(values.get("remainingCities"));
        if (!activeCities.isEmpty() || !remainingCities.isEmpty()) {
            Map<String, List<String>> cityTreasures = new HashMap<>();
            for (String asciiCity : activeCities) {
                cityTreasures.put(asciiCity, splitStrings(values.get("city." + asciiCity + ".treasures")));
            }
            data.restoreCitiesState(activeCities, remainingCities, cityTreasures);
        }

        // Restore controller lifecycle and active turn state
        int savedTotalVotes = parseInt(values.get("totalVotes"), 0);
        boolean savedEndRound = Boolean.parseBoolean(values.getOrDefault("endRoundToFinalTurn", "false"));
        boolean savedFinalTurn = Boolean.parseBoolean(values.getOrDefault("finalTurn", "false"));
        controller.restoreMatchLifecycle(savedTotalVotes, savedEndRound, savedFinalTurn);

        int activeInd = parseInt(values.get("activePlayerIndex"), 0);
        if (activeInd < 0 || activeInd >= totalPlayers) {
            activeInd = 0;
        }
        String rawColumn = values.getOrDefault("chosenColumn", "NONE");
        ColumnColor chosenCol = null;
        if (!"NONE".equalsIgnoreCase(rawColumn)) {
            try {
                chosenCol = ColumnColor.valueOf(rawColumn);
            } catch (IllegalArgumentException ignored) {
            }
        }

        controller.restoreControllerTurnState(activeInd, chosenCol);
        data.setMovementsLeft(activeInd, parseInt(values.get("movementsLeft"), 0));
        data.setHandsLeft(activeInd, parseInt(values.get("handsLeft"), 0));
        data.setKhanPending(activeInd, Boolean.parseBoolean(values.getOrDefault("khanPending", "false")));

        data.notifyStateRestored();
        return true;
    }

    /**
     * Convenience overload preserving compatibility with older callers.
     */
    public boolean loadMatchState(String filePath, BoardPanel boardPanel) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        boolean loaded = loadMatchState(new File(filePath));
        if (loaded && boardPanel != null) {
            boardPanel.repaint();
        }
        return loaded;
    }

    private static void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(toPureAscii(line));
        writer.newLine();
    }

    private static void writeKeyValue(BufferedWriter writer, String key, String value) throws IOException {
        writer.write(toPureAscii(key));
        writer.write('=');
        writer.write(toPureAscii(value));
        writer.newLine();
    }

    private static String joinStrings(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(toPureAscii(items.get(i)));
        }
        return sb.toString();
    }

    private static List<String> splitStrings(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }
        String[] parts = raw.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Ensures every written character strictly belongs to printable 7-bit US-ASCII (32..126).
     */
    private static String toPureAscii(String text) {
        if (text == null) {
            return "";
        }
        String replaced = text
            .replace("\u00E3", "a")
            .replace("\u00E1", "a")
            .replace("\u00E2", "a")
            .replace("\u00E0", "a")
            .replace("\u00E9", "e")
            .replace("\u00EA", "e")
            .replace("\u00ED", "i")
            .replace("\u00F3", "o")
            .replace("\u00F4", "o")
            .replace("\u00F5", "o")
            .replace("\u00FA", "u")
            .replace("\u00E7", "c")
            .replace("\u00C3", "A")
            .replace("\u00C1", "A")
            .replace("\u00C9", "E")
            .replace("\u00CD", "I")
            .replace("\u00D3", "O")
            .replace("\u00DA", "U")
            .replace("\u00C7", "C");

        StringBuilder sb = new StringBuilder(replaced.length());
        for (int i = 0; i < replaced.length(); i++) {
            char ch = replaced.charAt(i);
            if (ch >= 32 && ch <= 126) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
