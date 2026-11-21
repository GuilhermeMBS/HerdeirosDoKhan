package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the global supply of upgrade tiles and the 4 fixed slots
 * of the Upgrades Area on the bottom-left of the board.
 * Bought upgrades leave empty (null) slots until Genghis Khan visits
 * the Upgrades Area to grant empty-slot bonuses and refill all 4 slots.
 */
class UpgradeMarket {
    static final int OFFER_CAPACITY = 4;
    static final String EMPTY_SLOT_CODE = "EMPTY";

    private final List<Upgrade> deck = new ArrayList<>();
    private final List<Upgrade> slots = new ArrayList<>();
    private final List<Upgrade> discard = new ArrayList<>();

    UpgradeMarket() {
        resetMarket();
    }

    void resetMarket() {
        deck.clear();
        slots.clear();
        discard.clear();

        // Standard action upgrades across RED (-R), GREEN (-V), and YELLOW (-A)
        addCopies("CAV-R", 3);
        addCopies("CAV-V", 3);
        addCopies("CAV-A", 3);

        addCopies("MAO-R", 3);
        addCopies("MAO-V", 3);
        addCopies("MAO-A", 3);

        addCopies("MOE-R", 3);
        addCopies("MOE-V", 3);
        addCopies("MOE-A", 3);

        addCopies("ESP-R", 2);
        addCopies("ESP-V", 2);
        addCopies("ESP-A", 2);

        addCopies("CAS-R", 2);
        addCopies("CAS-V", 2);
        addCopies("CAS-A", 2);

        // Unique virtual treasure upgrades
        addCopies("BOI-A", 1);
        addCopies("CUI-A", 1);
        addCopies("MOB-R", 1);
        addCopies("MOP-V", 1);
        addCopies("PER-V", 1);

        Collections.shuffle(deck);

        for (int i = 0; i < OFFER_CAPACITY; i++) {
            slots.add(drawFromDeck());
        }
    }

    private void addCopies(String code, int qty) {
        for (int i = 0; i < qty; i++) {
            Upgrade u = createByCode(code);
            if (u != null) {
                deck.add(u);
            }
        }
    }

    /**
     * Returns a fixed-size list of 4 slot codes, using "EMPTY" for purchased slots.
     */
    List<String> getAvailableUpgradeCodes() {
        List<String> codes = new ArrayList<>(OFFER_CAPACITY);
        for (int i = 0; i < OFFER_CAPACITY; i++) {
            Upgrade u = (i < slots.size()) ? slots.get(i) : null;
            codes.add(u != null ? u.getCode() : EMPTY_SLOT_CODE);
        }
        return codes;
    }

    int getDeckSize() {
        return deck.size();
    }

    int countEmptySlots() {
        int empty = 0;
        for (int i = 0; i < OFFER_CAPACITY; i++) {
            if (i >= slots.size() || slots.get(i) == null) {
                empty++;
            }
        }
        return empty;
    }

    Upgrade peekAvailable(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return null;
        }
        return slots.get(slotIndex);
    }

    /**
     * Removes the upgrade at the specified slot, leaving that slot empty (null)
     * so empty spaces accumulate for Genghis Khan's refill bonus.
     */
    Upgrade takeAvailable(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return null;
        }
        Upgrade taken = slots.get(slotIndex);
        slots.set(slotIndex, null);
        return taken;
    }

    Upgrade drawFromDeck() {
        if (deck.isEmpty() && !discard.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck);
        }
        if (deck.isEmpty()) {
            return null;
        }
        return deck.remove(0);
    }

    /**
     * Triggered when Genghis Khan moves to the Upgrades Area:
     * counts empty slots prior to refill, discards remaining face-up tiles,
     * and refills all 4 slots from the deck.
     *
     * @return the number of empty slots before the refill (used to grant Coin/Sword bonuses)
     */
    int refreshMarket() {
        int emptyBeforeRefill = countEmptySlots();

        for (int i = 0; i < slots.size(); i++) {
            Upgrade remaining = slots.get(i);
            if (remaining != null) {
                discard.add(remaining);
            }
        }

        slots.clear();
        for (int i = 0; i < OFFER_CAPACITY; i++) {
            slots.add(drawFromDeck());
        }

        return emptyBeforeRefill;
    }

    void restoreAvailableByCodes(List<String> codes) {
        slots.clear();
        for (int i = 0; i < OFFER_CAPACITY; i++) {
            String code = (codes != null && i < codes.size()) ? codes.get(i) : EMPTY_SLOT_CODE;
            if (code == null || EMPTY_SLOT_CODE.equalsIgnoreCase(code.trim())) {
                slots.add(null);
            } else {
                slots.add(createByCode(code));
            }
        }
    }

    static Upgrade createByCode(String rawCode) {
        if (rawCode == null) {
            return null;
        }
        String code = rawCode.trim().toUpperCase();
        switch (code) {
            // Horse upgrades depict 2 horses
            case "CAV-R": return new Upgrade("CAV-R", Icon.HORSE, 2, ColumnColor.RED);
            case "CAV-V": return new Upgrade("CAV-V", Icon.HORSE, 2, ColumnColor.GREEN);
            case "CAV-A": return new Upgrade("CAV-A", Icon.HORSE, 2, ColumnColor.YELLOW);

            case "MAO-R": return new Upgrade("MAO-R", Icon.HAND, 1, ColumnColor.RED);
            case "MAO-V": return new Upgrade("MAO-V", Icon.HAND, 1, ColumnColor.GREEN);
            case "MAO-A": return new Upgrade("MAO-A", Icon.HAND, 1, ColumnColor.YELLOW);

            case "MOE-R": return new Upgrade("MOE-R", Icon.VIRTUAL_COIN, 1, ColumnColor.RED);
            case "MOE-V": return new Upgrade("MOE-V", Icon.VIRTUAL_COIN, 1, ColumnColor.GREEN);
            case "MOE-A": return new Upgrade("MOE-A", Icon.VIRTUAL_COIN, 1, ColumnColor.YELLOW);

            case "ESP-R": return new Upgrade("ESP-R", Icon.VIRTUAL_SWORD, 1, ColumnColor.RED);
            case "ESP-V": return new Upgrade("ESP-V", Icon.VIRTUAL_SWORD, 1, ColumnColor.GREEN);
            case "ESP-A": return new Upgrade("ESP-A", Icon.VIRTUAL_SWORD, 1, ColumnColor.YELLOW);

            case "CAS-R": return new Upgrade("CAS-R", Icon.VIRTUAL_YURT, 1, ColumnColor.RED);
            case "CAS-V": return new Upgrade("CAS-V", Icon.VIRTUAL_YURT, 1, ColumnColor.GREEN);
            case "CAS-A": return new Upgrade("CAS-A", Icon.VIRTUAL_YURT, 1, ColumnColor.YELLOW);

            case "BOI-A": return new Upgrade("BOI-A", Icon.VIRTUAL_TREASURE, 1, ColumnColor.YELLOW, TreasureType.ANIMAL);
            case "CUI-A": return new Upgrade("CUI-A", Icon.VIRTUAL_TREASURE, 1, ColumnColor.YELLOW, TreasureType.SPICE);
            case "MOB-R": return new Upgrade("MOB-R", Icon.VIRTUAL_TREASURE, 1, ColumnColor.RED, TreasureType.IRON);
            case "MOP-V": return new Upgrade("MOP-V", Icon.VIRTUAL_TREASURE, 1, ColumnColor.GREEN, TreasureType.FUR);
            case "PER-V": return new Upgrade("PER-V", Icon.VIRTUAL_TREASURE, 1, ColumnColor.GREEN, TreasureType.SILK);
            default:
                return null;
        }
    }
}
