package me.justahuman.pk_hackathon.ability;

import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.util.ClickType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public interface AddonComboAbility extends MyAddonAbility, ComboAbility {
    ArrayList<ComboManager.AbilityInformation> IMPOSSIBLE = new ArrayList<>(List.of(new ComboManager.AbilityInformation("IMPOSSIBLE", ClickType.CUSTOM)));

    @Override
    default Object createNewComboInstance(Player player) {
        return null;
    }

    @Override
    default ArrayList<ComboManager.AbilityInformation> getCombination() {
        return IMPOSSIBLE;
    }
}
