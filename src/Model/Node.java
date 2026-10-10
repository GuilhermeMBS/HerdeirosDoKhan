package model;

import java.util.ArrayList;
import java.util.List;

abstract class Node {
    protected List<Province> provinces;
    protected List<Node> neighbors;

    Node() {
        this.provinces = new ArrayList<>();
        this.neighbors = new ArrayList<>();
    }

    void addNeighbor(Node neighbor) {
        if (!this.neighbors.contains(neighbor)) {
            this.neighbors.add(neighbor);
        }
    }

    void addProvince(Province province) {
        if (!this.provinces.contains(province)) {
            this.provinces.add(province);
        }
    }

    List<Province> getProvinces() {
        return this.provinces;
    }

    abstract boolean buildYurt(Player player);
    abstract boolean hasYurt();
}
