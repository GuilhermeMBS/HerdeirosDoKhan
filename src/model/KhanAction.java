package model;

import java.util.List;

class KhanAction implements GameAction {
    private final Province province;
    private final Khan khan;

    KhanAction(Province province, Khan khan) {
        this.province = province;
        this.khan = khan;
    }

    @Override
    public DebugReturn execute(Player p, Node node) {
        if (!p.mustUseKhan()) {
            return DebugReturn.MISSING_KHANMOVE;
        }

        if (this.province == null) {
            if (this.khan.isAtUpgradesArea()) {
                return DebugReturn.REPEAT_KHAN_POS;
            }
            this.khan.moveToUpgradesArea();
        } else {
            if (this.khan.isAtProvince(this.province)) {
                return DebugReturn.REPEAT_KHAN_POS;
            }
            List<Province> neighbors = !this.province.getKhanNeighbors().isEmpty()
                ? this.province.getKhanNeighbors()
                : node.getProvinces();
            this.khan.moveToProvince(this.province, neighbors);
        }

        p.useKhan();
        return DebugReturn.SUCCESS;
    }
}
