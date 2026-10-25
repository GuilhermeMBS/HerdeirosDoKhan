package model;

import java.util.ArrayList;
import java.util.Collections;

enum PrivateMissionType implements MissionType {
    POWERUPS_DIFF,
    POWERUPS_SAME,
    YURTINSEQUENCE,
    POWERUPS_QTD,
    CITY_YURT,
    POWERUPS_HORSE_HAND,
    YURT_TRADINGPOST,
    POWERUPS_YURT,
    YURT_KHAN,
    CITY_2YURT
}

/**
 * Manages the deck of 10 Secret Counselor cards dealt to players at match setup.
 */
class PrivateCounselor extends Card {
    private final String name;
    private static final ArrayList<PrivateCounselor> cards = new ArrayList<>();

    PrivateCounselor(Mission m1, Mission m2, Mission m3, String name) {
        this.m1 = m1;
        this.m2 = m2;
        this.m3 = m3;
        this.name = name;
        this.p = null;
    }

    static {
        resetDeck();
    }

    static void resetDeck() {
        cards.clear();
        cards.add(new PrivateCounselor(
            new Mission(1, PrivateMissionType.POWERUPS_SAME),
            new Mission(2, PrivateMissionType.POWERUPS_SAME),
            new Mission(4, PrivateMissionType.POWERUPS_SAME), "Sacerdote"
        ));
        cards.add(new PrivateCounselor(
            new Mission(4, PrivateMissionType.POWERUPS_DIFF),
            new Mission(6, PrivateMissionType.POWERUPS_DIFF),
            new Mission(7, PrivateMissionType.POWERUPS_DIFF), "Colecionador"
        ));
        cards.add(new PrivateCounselor(
            new Mission(3, PrivateMissionType.YURTINSEQUENCE),
            new Mission(6, PrivateMissionType.YURTINSEQUENCE),
            new Mission(8, PrivateMissionType.YURTINSEQUENCE), "Viajante"
        ));
        cards.add(new PrivateCounselor(
            new Mission(4, PrivateMissionType.POWERUPS_QTD),
            new Mission(7, PrivateMissionType.POWERUPS_QTD),
            new Mission(10, PrivateMissionType.POWERUPS_QTD), "Soldado"
        ));
        cards.add(new PrivateCounselor(
            new Mission(3, PrivateMissionType.CITY_YURT),
            new Mission(6, PrivateMissionType.CITY_YURT),
            new Mission(8, PrivateMissionType.CITY_YURT), "Cidadão"
        ));
        cards.add(new PrivateCounselor(
            new Mission(2, PrivateMissionType.POWERUPS_HORSE_HAND),
            new Mission(4, PrivateMissionType.POWERUPS_HORSE_HAND),
            new Mission(5, PrivateMissionType.POWERUPS_HORSE_HAND), "Cavaleiro"
        ));
        cards.add(new PrivateCounselor(
            new Mission(2, PrivateMissionType.YURT_TRADINGPOST),
            new Mission(4, PrivateMissionType.YURT_TRADINGPOST),
            new Mission(6, PrivateMissionType.YURT_TRADINGPOST), "Mensageiro"
        ));
        cards.add(new PrivateCounselor(
            new Mission(1, PrivateMissionType.POWERUPS_YURT),
            new Mission(2, PrivateMissionType.POWERUPS_YURT),
            new Mission(3, PrivateMissionType.POWERUPS_YURT), "Governador"
        ));
        cards.add(new PrivateCounselor(
            new Mission(2, PrivateMissionType.CITY_2YURT),
            new Mission(3, PrivateMissionType.CITY_2YURT),
            new Mission(4, PrivateMissionType.CITY_2YURT), "Prefeito"
        ));
        cards.add(new PrivateCounselor(
            new Mission(2, PrivateMissionType.YURT_KHAN),
            new Mission(3, PrivateMissionType.YURT_KHAN),
            new Mission(4, PrivateMissionType.YURT_KHAN), "Bajulador"
        ));
        Collections.shuffle(cards);
    }

    static ArrayList<PrivateCounselor> giveTwoCards() {
        if (cards.size() < 2) {
            resetDeck();
        }
        ArrayList<PrivateCounselor> tmp = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            tmp.add(cards.remove(0));
        }
        return tmp;
    }

    boolean setFinished(Mission m) {
        if (m != null && !m.hasFinishedMission()) {
            m.setFinished();
            return true;
        }
        return false;
    }

    @Override
    public int gainPoints() {
        return (m1.hasFinishedMission() ? 1 : 0)
             + (m2.hasFinishedMission() ? 1 : 0)
             + (m3.hasFinishedMission() ? 1 : 0);
    }

    String getName() {
        return this.name;
    }
}
