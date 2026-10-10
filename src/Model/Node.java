package model;

import java.util.ArrayList;
import java.util.List;

abstract class Node {
    protected List<Province> provinces;
    protected List<Node> neighbors;

    protected Player yurtOwner;

    Node() {
        this.provinces = new ArrayList<>();
        this.neighbors = new ArrayList<>();

        this.yurtOwner = null;
    }

    boolean hasYurt() {
        return this.yurtOwner != null;
    }

    boolean buildYurt(Player player) {
        if (this.yurtOwner == null) {
            this.yurtOwner = player;
            return true;
        }
        return false;
    }

    void addNeighbor(Node neighbor) {
        if (this.neighbors.contains(neighbor) == false) {
            this.neighbors.add(neighbor);
        }
    }

    void addProvince(Province province) {
        if (this.provinces.contains(province) == false) {
            this.provinces.add(province);
        }
    }

    List<Province> getProvinces() {
        return this.provinces;
    }
}
