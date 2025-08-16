package me.justahuman.projectkorra.dashpack.util;

import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.util.ClickType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DashInformation extends ComboManager.AbilityInformation {
    private final List<DashDirection> directions;

    public DashInformation(String name, List<DashDirection> directions) {
        this(name, 0, directions);
    }

    public DashInformation(String name, long time, List<DashDirection> directions) {
        super(name, ClickType.CUSTOM, time);
        this.directions = new ArrayList<>(directions);
        this.directions.sort(DashDirection::compareTo);
    }

    @Override
    public boolean equalsWithoutTime(ComboManager.AbilityInformation info) {
        if (getTime() != 0 && info instanceof DashInformation && info.getTime() == 0) {
            return info.equalsWithoutTime(this);
        }

        if (super.equalsWithoutTime(info) && info instanceof DashInformation dashInfo) {
            boolean any = directions.contains(DashDirection.ANY);
            if (any && directions.size() == 1) {
                return true;
            } else if (any) {
                for (DashDirection direction : directions) {
                    if (direction != DashDirection.ANY && dashInfo.directions.contains(direction)) {
                        return true;
                    }
                }
            } else {
                return directions.equals(dashInfo.directions);
            }
        }
        return false;
    }
}
