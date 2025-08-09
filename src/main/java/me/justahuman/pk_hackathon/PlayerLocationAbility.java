package me.justahuman.pk_hackathon;

import com.projectkorra.projectkorra.ability.Ability;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PlayerLocationAbility extends Ability {
    @Override
    default Location getLocation() {
        Player player = getPlayer();
        return player != null ? player.getLocation() : null;
    }
}
