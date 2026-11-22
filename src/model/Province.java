package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Geographic zone bordered by travel paths, capable of generating tributes.
 */
class Province {
    protected int resourceCount;
    protected int khanPosition;
    protected TributeType tributeType;
    private final List<Province> khanNeighbors = new ArrayList<>();

    Province(TributeType type) {
        this.tributeType = type;
        this.resourceCount = 1;
        this.khanPosition = 0;
    }

    boolean collectResource() {
        if (this.resourceCount > 0) {
            this.resourceCount--;
            return true;
        }
        return false;
    }

    void addResource() { this.resourceCount = Math.max(3, this.resourceCount + 1); }

    TributeType getTributeType() { return this.tributeType; }

    int getResourceCount() { return this.resourceCount; }

    void addKhanNeighbor(Province neighbor) {
        if (neighbor != null && neighbor != this && !this.khanNeighbors.contains(neighbor)) {
            this.khanNeighbors.add(neighbor);
        }
    }

    List<Province> getKhanNeighbors() { return Collections.unmodifiableList(this.khanNeighbors); }
    
    private static final List<String> provinceIds = new ArrayList<>();
    static {
    	for (ProvinceId province : ProvinceId.values()) provinceIds.add(province.name());
    }
    
    static List<String> getAllProvinceIds() { return provinceIds; }
    
    private static final List<String> khanProvinceIds = new ArrayList<>();
    static {
    	khanProvinceIds.add(ProvinceId.RUSSIA_YURT1.name());
        khanProvinceIds.add(ProvinceId.RUSSIA_COIN1.name());
        khanProvinceIds.add(ProvinceId.CHINA_YURT1.name());
        khanProvinceIds.add(ProvinceId.PERSIA_COIN1.name());
    }
    
    static List<String> getKhanProvinceIds() { return khanProvinceIds; }
}
