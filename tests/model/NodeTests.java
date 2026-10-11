package model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

// Stub enumerations and classes to satisfy the test context.
// Replace these with your actual enums/Player class in the final project.
enum TreasureType { FUR, ANIMAL, SILK, IRON, SPICE }
enum TributeType { COIN, SWORD, YURT }

class Player {
    private String name;
    Player(String name) { this.name = name; }
    String getName() { return this.name; }
}

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

    /**
     * Verifies the maximum concurrent pawn occupancy mechanics for double nodes.
     */
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

    /**
     * Verifies the maximum concurrent structural (Yurt) occupancy for double nodes.
     */
    @Test
    public void testDoubleNodeYurtLimit() {
        DoubleNode node = new DoubleNode("Persia", new TradingPost(1, new TreasureRequirements[0], 0));
        
        assertTrue("First yurt constructed", node.buildYurt(this.playerBatu));
        assertTrue("Second yurt constructed", node.buildYurt(this.playerOgedei));
        assertFalse("Third yurt rejected by capacity limit", node.buildYurt(this.playerTolui)); 
    }

    /**
     * Validates the unique asymmetrical rule allowing only 'Tolui' to build on the Capital.
     */
    @Test
    public void testCentralNodeToluiException() {
        CentralNode karakorum = new CentralNode(false);
        
        assertFalse("Standard player rejected at capital", karakorum.buildYurt(this.playerBatu));
        assertTrue("Tolui rule processed successfully", karakorum.buildYurt(this.playerTolui));
        assertFalse("Limit of one structure per node enforced", karakorum.buildYurt(this.playerTolui));
    }

    /**
     * Tests the transition rules for a city's geopolitical subjugation.
     */
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

    /**
     * Validates the rigid mechanical ceiling of tribute generation in provinces.
     */
    @Test
    public void testProvinceResourceCap() {
        Province province = new Province(TributeType.COIN);
        assertEquals("Initial load should be 1", 1, province.resourceCount);
        
        province.addResource();
        province.addResource();
        province.addResource(); // Push past logical boundary
        
        assertEquals("Cap should hold at 3", 3, province.resourceCount);
    }

    /**
     * Tests standard resource depletion and bottom bounds handling.
     */
    @Test
    public void testProvinceResourceDepletion() {
        Province province = new Province(TributeType.SWORD);
        
        assertTrue("Valid collection", province.collectResource());
        assertFalse("Empty state collection rejected", province.collectResource());
        assertEquals("Count should never drop below 0", 0, province.resourceCount);
    }

    /**
     * Simulates the exact matching and evaluation array constraints of the TradingPost contract.
     */
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

    /**
     * Validates the topological propagation effect of the Khan's placement algorithm.
     */
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
