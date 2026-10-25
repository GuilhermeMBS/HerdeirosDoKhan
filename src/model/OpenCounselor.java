package model;

import java.util.ArrayList;
import java.util.Collections;

enum OpenMissionType implements MissionType {
    YURT,
    YURTREGION,
    SWORD,
    EACHRESOURCE,
    TREASURE,
    CITIESDOMINATED,
    POWERUPS_LINE,
    POWERUPS_COL,
    POWERUPS_SQUARE,
    POINTS
}

/**
 * Manages the deck of 14 Open Counselor cards, of which 4 are revealed per match.
 */
class OpenCounselor extends Card {
    private static ArrayList<OpenCounselor> cardsInGame = null;
    private static final ArrayList<OpenCounselor> cards = new ArrayList<>();

    private OpenCounselor(int qtd, OpenMissionType type) {
        this.m1 = new Mission(qtd, type);
        this.m2 = null;
        this.m3 = null;
        this.p = null;
    }

    static {
        resetDeck();
    }

    static void resetDeck() {
        cards.clear();
        cardsInGame = null;

        cards.add(new OpenCounselor(4, OpenMissionType.SWORD));
        cards.add(new OpenCounselor(6, OpenMissionType.YURT));
        cards.add(new OpenCounselor(10, OpenMissionType.YURT));
        cards.add(new OpenCounselor(1, OpenMissionType.YURTREGION));
        cards.add(new OpenCounselor(2, OpenMissionType.YURTREGION));
        cards.add(new OpenCounselor(4, OpenMissionType.TREASURE));
        cards.add(new OpenCounselor(1, OpenMissionType.EACHRESOURCE));
        cards.add(new OpenCounselor(2, OpenMissionType.POINTS));
        cards.add(new OpenCounselor(3, OpenMissionType.POINTS));
        cards.add(new OpenCounselor(2, OpenMissionType.CITIESDOMINATED));
        cards.add(new OpenCounselor(3, OpenMissionType.CITIESDOMINATED));
        cards.add(new OpenCounselor(1, OpenMissionType.POWERUPS_LINE));
        cards.add(new OpenCounselor(1, OpenMissionType.POWERUPS_COL));
        cards.add(new OpenCounselor(1, OpenMissionType.POWERUPS_SQUARE));

        Collections.shuffle(cards);
    }

    private static void createFourCards() {
        if (cards.size() < 4) {
            resetDeck();
        }
        cardsInGame = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            cardsInGame.add(cards.remove(0));
        }
    }

    static ArrayList<OpenCounselor> giveCardsInGame() {
        if (cardsInGame == null) {
            createFourCards();
        }
        return cardsInGame;
    }

    boolean playerHasCompleted(Player p) {
        if (this.p != null) {
            return false;
        }
        this.p = p;
        this.m1.setFinished();
        return true;
    }

    @Override
    public int gainPoints() {
        return hasBeenCompleted() ? 1 : 0;
    }

    private boolean hasBeenCompleted() {
        return this.p != null;
    }

    OpenMissionType getMissionType() {
        return (OpenMissionType) this.m1.getMissionType();
    }

    int getMissionTarget() {
        return this.m1.getTargetAmount();
    }
}
