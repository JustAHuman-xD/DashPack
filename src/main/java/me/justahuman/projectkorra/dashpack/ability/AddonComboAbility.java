package me.justahuman.projectkorra.dashpack.ability;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.util.ClickType;
import me.justahuman.projectkorra.dashpack.util.DashDirection;
import me.justahuman.projectkorra.dashpack.util.DashInformation;
import me.justahuman.projectkorra.dashpack.util.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface AddonComboAbility extends MyAddonAbility, ComboAbility {
    ComboManager.AbilityInformation IMPOSSIBLE = new ComboManager.AbilityInformation("IMPOSSIBLE", ClickType.CUSTOM);

    default int getPriority() {
        return 0;
    }

    default long checkCustomCombo(Player player) {
        ComboManager.ComboAbilityInfo info = ComboManager.getComboAbilities().get(getName());
        if (info == null || !info.getAbilities().getFirst().equals(IMPOSSIBLE)) {
            return -1;
        }

        info.getAbilities().removeFirst();
        boolean valid = Utils.checkForValidCombo(player) == info;
        info.getAbilities().addFirst(IMPOSSIBLE);

        List<ComboManager.AbilityInformation> recent = ComboManager.getRecentlyUsedAbilities(player, 1);
        return valid ? (recent.isEmpty() ? System.currentTimeMillis() : recent.get(0).getTime()) : -1;
    }

    default boolean forceCustomHandling() {
        return false;
    }

    default boolean consumesDash() {
        return false;
    }

    @Override
    default ArrayList<ComboManager.AbilityInformation> getCombination() {
        List<String> comboList = getStringList("Combination");
        ArrayList<ComboManager.AbilityInformation> combination = ComboUtil.generateCombinationFromList(this, getStringList("Combination"));
        if (combination == null) {
            combination = new ArrayList<>();
        }

        if (comboList.size() != combination.size()) {
            for (int i = 0; i < comboList.size(); i++) {
                String step = comboList.get(i);
                String[] parts = step.split(":");
                if (combination.size() >= i && parts.length == 2 && CoreAbility.getAbility(parts[0]) instanceof DashAbility) {
                    String[] directions = parts[1].split(",");
                    List<DashDirection> dashDirections = new ArrayList<>();
                    for (String direction : directions) {
                        try {
                            dashDirections.add(DashDirection.valueOf(direction.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            ProjectKorra.log.warning("Invalid DashDirection \"" + direction + "\" in combo for " + getName() + ". Valid directions are: " + Arrays.toString(DashDirection.values()));
                        }
                    }
                    combination.add(i, new DashInformation(parts[0], dashDirections));
                    ProjectKorra.log.info("\"" + getName() + "\" had \"" + parts[0] + "\" added back to it's combo list after being incorrectly removed.");
                }
            }
        }
        if (consumesDash() || forceCustomHandling()) {
            if (!forceCustomHandling() && !(combination.getLast() instanceof DashInformation)) {
                ProjectKorra.log.warning("The last step of the combo for " + getName() + " is not a DashAbility, moving to core PK combo management.");
            } else {
                combination.addFirst(IMPOSSIBLE);
            }
        }
        return combination;
    }
}
