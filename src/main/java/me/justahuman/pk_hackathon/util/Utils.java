package me.justahuman.pk_hackathon.util;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.util.ReflectionHandler;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final List<BlockFace> AXIS = new ArrayList<>(List.of(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST));

    /**
     * @deprecated To be replaced with an official method in ComboManager
     */
    @Deprecated(forRemoval = true)
    public static void addComboAbility(Player player, ComboManager.AbilityInformation information) {
        ComboManager.addRecentAbility(player, information);
        final ComboManager.ComboAbilityInfo comboAbil = ComboManager.checkForValidCombo(player);
        if (comboAbil == null) {
            return;
        } else if (!player.hasPermission("bending.ability." + comboAbil.getName())) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (comboAbil.getComboType() instanceof Class) {
                    final Class<?> clazz = (Class<?>) comboAbil.getComboType();
                    try {
                        ReflectionHandler.instantiateObject(clazz, player);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (comboAbil.getComboType() instanceof ComboAbility) {
                        ((ComboAbility) comboAbil.getComboType()).createNewComboInstance(player);
                        return;
                    }
                }
            }

        }.runTaskLater(ProjectKorra.plugin, 1L);
    }

    public static BlockFace yawToFace(float yaw) {
        return AXIS.get(Math.round(yaw / 90f) & 0x3);
    }

    public static List<BlockFace> getAxis() {
        return AXIS;
    }
}
