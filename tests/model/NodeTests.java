package model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 Test Suite validating topological invariants, state mutations,
 * and logic constraints within the board's node hierarchy.
 */
public class NodeTests {

    private Player playerTolui;
    private Player playerBatu;
    private Player playerOgedei;

    @Before
    public void setUp() {
        this.playerTolui = new Player("Tolui");
        this.playerBatu = new Player("Batu");
        this.playerOgedei = new Player("Ogedei");
    }

    @Test
    public void testDoubleNodeOccupancyLimit() {
        TreasureRequirements[] requirements = {
            new TreasureRequirements(TreasureType.FUR, 1)
        };

        DoubleNode node = new DoubleNode("Persia", new TradingPost(1, requirements, 0));

        assertTrue("First pawn should enter", node.enterNode());
        assertTrue("Second pawn should enter", node.enterNode());
        assertFalse("Third pawn should be rejected by capacity limit", node.enterNode());

        node.leaveNode();
        assertTrue("Space freed; third pawn should now enter", node.enterNode());
    }

    @Test
    public void testDoubleNodeYurtLimit() {
        DoubleNode node = new DoubleNode("Persia", new TradingPost(1, new TreasureRequirements[0], 0));

        assertTrue("First yurt constructed", node.buildYurt(this.playerBatu));
        assertTrue("Second yurt constructed", node.buildYurt(this.playerOgedei));
        assertFalse("Third yurt rejected by capacity limit", node.buildYurt(this.playerTolui));
    }

    @Test
    public void testCentralNodeToluiException() {
        CentralNode karakorum = new CentralNode(false);

        assertFalse("Standard player rejected at capital", karakorum.buildYurt(this.playerBatu));
        assertTrue("Tolui rule processed successfully", karakorum.buildYurt(this.playerTolui));
        assertFalse("Limit of one structure per node enforced", karakorum.buildYurt(this.playerTolui));
    }

    @Test
    public void testSingleNodeConquerCondition() {
        SingleNode city = new SingleNode("Russia", "Kiev");
        city.treasures.add(TreasureType.FUR);

        assertFalse("Should not be conquered (Has treasure, no yurt)", city.isConquered());

        city.plunderTreasure(0);
        assertFalse("Should not be conquered (Empty treasure, but no yurt)", city.isConquered());

        city.buildYurt(this.playerBatu);
        assertTrue("Should be conquered (Empty treasure and yurt present)", city.isConquered());
    }

    @Test
    public void testProvinceResourceCap() {
        Province province = new Province(TributeType.COIN);
        assertEquals("Initial load should be 1", 1, province.resourceCount);

        province.addResource();
        province.addResource();
        province.addResource();

        assertEquals("Cap should hold at 3", 3, province.resourceCount);
    }

    @Test
    public void testProvinceResourceDepletion() {
        Province province = new Province(TributeType.SWORD);

        assertTrue("Valid collection", province.collectResource());
        assertFalse("Empty state collection rejected", province.collectResource());
        assertEquals("Count should never drop below 0", 0, province.resourceCount);
    }

    @Test
    public void testTradingPostDeliveryValidation() {
        TreasureRequirements[] requirements = {
            new TreasureRequirements(TreasureType.FUR, 2)
        };
        TradingPost post = new TradingPost(1, requirements, 0);

        TreasureRequirements[] insufficientOffer = {
            new TreasureRequirements(TreasureType.FUR, 1),
            new TreasureRequirements(TreasureType.ANIMAL, 5)
        };
        assertFalse("Insufficient requested type should fail transaction", post.processDelivery(insufficientOffer));

        TreasureRequirements[] validOffer = {
            new TreasureRequirements(TreasureType.FUR, 2)
        };
        assertTrue("Exact match should clear transaction", post.processDelivery(validOffer));
    }

    @Test
    public void testKhanResourcePropagation() {
        Khan khan = new Khan();
        Province target = new Province(TributeType.YURT);
        Province adjacent1 = new Province(TributeType.COIN);
        Province adjacent2 = new Province(TributeType.SWORD);

        List<Province> adjacents = new ArrayList<>();
        adjacents.add(adjacent1);
        adjacents.add(adjacent2);

        khan.moveToProvince(target, adjacents);

        assertEquals("Target province yielded 1 tribute", 2, target.resourceCount);
        assertEquals("Adjacent yielded 1 tribute", 2, adjacent1.resourceCount);
        assertEquals("Adjacent yielded 1 tribute", 2, adjacent2.resourceCount);
        assertFalse("Khan status updated accurately", khan.isAtUpgradesArea());
    }
}
