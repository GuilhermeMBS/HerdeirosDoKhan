package model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class GameModelTest {

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
    			new TreasureRequirements(TreasureType.FUR, 1),
    			new TreasureRequirements(TreasureType.ANIMAL, 1),
    			new TreasureRequirements(TreasureType.SILK, 1)
    	};
    	
        DoubleNode node = new DoubleNode("Persia", new TradingPost(1, requirements, 0));
        
        assertTrue(node.enterNode());
        assertTrue(node.enterNode());
        assertFalse(node.enterNode());
        
        node.leaveNode();
        assertTrue(node.enterNode());
    }

    @Test
    public void testDoubleNodeYurtLimit() {
    	TreasureRequirements[] requirements = {
    			new TreasureRequirements(TreasureType.FUR, 1),
    			new TreasureRequirements(TreasureType.ANIMAL, 1)
    	};
        DoubleNode node = new DoubleNode("Persia", new TradingPost(1, requirements, 0));
        
        assertTrue(node.buildYurt(this.playerBatu));
        assertTrue(node.buildYurt(this.playerOgedei));
        assertFalse(node.buildYurt(this.playerTolui)); // Rejeição da 3ª estrutura
    }

    @Test
    public void testCentralNodeToluiException() {
        CentralNode karakorum = new CentralNode(false);
        
        assertFalse(karakorum.buildYurt(this.playerBatu));
        assertTrue(karakorum.buildYurt(this.playerTolui));
        assertFalse(karakorum.buildYurt(this.playerTolui));
    }

    @Test
    public void testSingleNodeConquerCondition() {
        SingleNode city = new SingleNode("Russia", "Kiev");
        
        city.treasures.add(TreasureType.FUR);
        
        assertFalse(city.isConquered());
        
        city.plunderTreasure(0);
        assertFalse(city.isConquered());
        
        city.buildYurt(this.playerBatu);
        assertTrue(city.isConquered());
    }

    @Test
    public void testProvinceResourceCap() {
        Province province = new Province(TributeType.COIN);
        assertEquals(1, province.resourceCount);
        
        province.addResource();
        province.addResource();
        province.addResource();
        
        assertEquals(3, province.resourceCount);
    }

    @Test
    public void testProvinceResourceDepletion() {
        Province province = new Province(TributeType.SWORD);
        
        assertTrue(province.collectResource());
        assertFalse(province.collectResource());
        assertEquals(0, province.resourceCount);
    }

    @Test
    public void testTradingPostDeliveryValidation() {
    	TreasureRequirements[] requirements = {
    			new TreasureRequirements(TreasureType.FUR, 2)
    	};
        TradingPost post = new TradingPost(1, requirements, 0);
        
        TreasureRequirements[] insufficientOffer = {
    			new TreasureRequirements(TreasureType.FUR, 1),
    			new TreasureRequirements(TreasureType.ANIMAL, 5),
    			new TreasureRequirements(TreasureType.SILK, 5)
    	};
        assertFalse(post.processDelivery(insufficientOffer));
        
        TreasureRequirements[] validOffer = {
    			new TreasureRequirements(TreasureType.FUR, 2)
    	};
        assertTrue(post.processDelivery(validOffer));
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
        
        assertEquals(2, target.resourceCount);
        assertEquals(2, adjacent1.resourceCount);
        assertEquals(2, adjacent2.resourceCount);
        assertFalse(khan.isAtUpgradesArea());
    }
}
