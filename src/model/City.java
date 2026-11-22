package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a city on the board that can be sacked for treasures and conquered.
 */
class City {
    private final String name;
    private boolean sackable = false;
    private Player conqueror = null;
    private final ArrayList<TreasureType> resources = new ArrayList<>();

    City(String name) { this.name = name; }

    boolean activateSacking() {
        this.sackable = true;
        this.resources.clear();
        List<TreasureType> generated = Resource.generateResources();
        if (generated != null) {
            this.resources.addAll(generated);
        }
        return true;
    }

    boolean canBeSacked() { return this.sackable; }

    TreasureType removeResource(int ind) {
        if (!this.sackable || this.resources.isEmpty() || ind < 0 || ind >= this.resources.size()) {
            return null;
        }
        return this.resources.remove(ind);
    }

    boolean defineDomination(Player player) {
        if (!(this.sackable && this.resources.isEmpty())) {
            return false;
        }
        this.conqueror = player;
        this.sackable = false;
        return true;
    }

    /** Sets the conqueror directly when loading a saved match. */
    void restoreConqueror(Player player) { this.conqueror = player; }

    Player dominatedBy() { return this.conqueror; }

    String getName() { return this.name; }

    List<TreasureType> getResources() { return Collections.unmodifiableList(this.resources); }

    ArrayList<TreasureType> getResourcesInternal() { return this.resources; }
    
    private final static EnumMap<Region, EnumMap<CityPosition, String>> cities = new EnumMap<>(Region.class);
    static {
    	cities.put(Region.CHINA, new EnumMap<CityPosition, String>(CityPosition.class) {{
    		put(CityPosition.CITY1, "Pequim");
    		put(CityPosition.CITY2, "Kaesong");
    		put(CityPosition.CITY3, "Cantão");
    	}});

    	cities.put(Region.RUSSIA, new EnumMap<CityPosition, String>(CityPosition.class) {{
    		put(CityPosition.CITY1, "Sarai");
    		put(CityPosition.CITY2, "Moscou");
    		put(CityPosition.CITY3, "Kiev");
    	}});
    	
    	cities.put(Region.PERSIA, new EnumMap<CityPosition, String>(CityPosition.class) {{
    		put(CityPosition.CITY1, "Samarcanda");
            put(CityPosition.CITY2, "Cabul");
            put(CityPosition.CITY3, "Bagdá");
    	}});
    }
    
    static EnumMap<Region, EnumMap<CityPosition, String>> getCitiesPerRegion() { return cities; }
    
    private final static List<String> cityNames = new ArrayList<>();
    static {
    	for (Region region : Region.values()) {
    		for (CityPosition pos : CityPosition.values()) cityNames.add(cities.get(region).get(pos));
    	}
    }
    static List<String> getCitiesNames() { return cityNames; }
    
    private final static String pathCityImg = "assets/cities/";
    private final static HashMap<String, String> cityImgPath = new HashMap<>();
    static {
    	for (String name : cityNames) cityImgPath.put(name, pathCityImg + name + ".jpg");
    }
    
    static HashMap<String, String> getCityImgPath() { return cityImgPath; }
}
