package me.justahuman.pk_hackathon.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ReflectionHandler;
import me.justahuman.pk_hackathon.ability.AddonComboAbility;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils {
    private static final List<BlockFace> AXIS = new ArrayList<>(List.of(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST));

    /**
     * @deprecated To be replaced with an official method in ComboManager
     */
    @Deprecated(forRemoval = true)
    public static void addComboAbility(Player player, ComboManager.AbilityInformation information) {
        ComboManager.addRecentAbility(player, information);
        final ComboManager.ComboAbilityInfo comboAbil = checkForValidCombo(player);
        if (comboAbil == null) {
            return;
        } else if (!player.hasPermission("bending.ability." + comboAbil.getName())) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (comboAbil.getComboType() instanceof Class<?> clazz) {
                    try {
                        ReflectionHandler.instantiateObject(clazz, player);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (comboAbil.getComboType() instanceof ComboAbility ability) {
                        ability.createNewComboInstance(player);
                    }
                }
            }

        }.runTaskLater(ProjectKorra.plugin, 1L);
    }

    /**
     * @deprecated To be replaced with an official method in ComboManager
     * Checks the player's recent abilities for a valid combo with consideration of priorities.
     */
    @Deprecated(forRemoval = true)
    public static ComboManager.ComboAbilityInfo checkForValidCombo(Player player) {
        final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer == null) {
            return null;
        }

        final ArrayList<ComboManager.AbilityInformation> playerCombo = ComboManager.getRecentlyUsedAbilities(player, 8);
        final List<String> abilities = new ArrayList<>(ComboManager.getComboAbilities().keySet());
        abilities.sort(Comparator.comparingInt(ability -> CoreAbility.getAbility(ability) instanceof AddonComboAbility combo ? combo.getPriority() : 0));
        Collections.reverse(abilities);
        for (final String ability : abilities) {
            final CoreAbility coreAbility = CoreAbility.getAbility(ability);
            if (coreAbility == null || !coreAbility.isEnabled() || bPlayer.isOnCooldown(ability)) {
                continue;
            }

            final ComboManager.ComboAbilityInfo customAbility = ComboManager.getComboAbilities().get(ability);
            final ArrayList<ComboManager.AbilityInformation> abilityCombo = customAbility.getAbilities();
            final int size = abilityCombo.size();
            if (playerCombo.size() < size) {
                continue;
            }

            boolean isValid = true;
            for (int i = 1; i <= size; i++) {
                final ComboManager.AbilityInformation playerInfo = playerCombo.get(playerCombo.size() - i);
                final ComboManager.AbilityInformation comboInfo = abilityCombo.get(abilityCombo.size() - i);
                if (playerInfo.getAbilityName().equals(comboInfo.getAbilityName()) && playerInfo.getClickType() == ClickType.LEFT_CLICK_ENTITY && comboInfo.getClickType() == ClickType.LEFT_CLICK) {
                    continue;
                } else if (!playerInfo.equalsWithoutTime(comboInfo)) {
                    isValid = false;
                    break;
                }
            }

            if (isValid) {
                return customAbility;
            }
        }

        return null;
    }

    public static BlockFace yawToFace(float yaw) {
        return AXIS.get(Math.round(yaw / 90f) & 0x3);
    }

    public static List<BlockFace> getAxis() {
        return AXIS;
    }
}
