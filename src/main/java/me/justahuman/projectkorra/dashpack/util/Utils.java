package me.justahuman.projectkorra.dashpack.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ReflectionHandler;
import me.justahuman.projectkorra.dashpack.ability.AddonComboAbility;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import me.justahuman.projectkorra.dashpack.ability.airbending.AirDash;
import me.justahuman.projectkorra.dashpack.ability.chi.ChiDash;
import me.justahuman.projectkorra.dashpack.ability.earthbending.EarthDash;
import me.justahuman.projectkorra.dashpack.ability.firebending.FireDash;
import me.justahuman.projectkorra.dashpack.ability.waterbending.BloodDash;
import me.justahuman.projectkorra.dashpack.ability.waterbending.WaterDash;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils {
    private static final List<BlockFace> SURROUND = new ArrayList<>(List.of(BlockFace.SOUTH, BlockFace.SOUTH_EAST, BlockFace.EAST, BlockFace.NORTH_EAST, BlockFace.NORTH, BlockFace.NORTH_WEST, BlockFace.WEST, BlockFace.SOUTH_WEST, BlockFace.UP, BlockFace.DOWN));
    private static final List<BlockFace> ADJACENT = new ArrayList<>(List.of(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN));
    private static final List<BlockFace> AXIS = new ArrayList<>(List.of(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST));

    public static Element getFirstDashElement(BendingPlayer player) {
        if (player.canBendIgnoreBindsCooldowns(CoreAbility.getAbility(AirDash.class))) return Element.AIR;
        else if (player.canBendIgnoreBindsCooldowns(CoreAbility.getAbility(ChiDash.class))) return Element.CHI;
        else if (player.canBendIgnoreBindsCooldowns(CoreAbility.getAbility(FireDash.class))) return Element.FIRE;
        else if (player.canBendIgnoreBindsCooldowns(CoreAbility.getAbility(EarthDash.class))) return Element.EARTH;
        else if (player.canBendIgnoreBindsCooldowns(CoreAbility.getAbility(WaterDash.class))) return Element.WATER;
        else return null;
    }

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

    public static BlockFace getClosestBlockFace(Vector vector) {
        BlockFace closest = BlockFace.SELF;
        double maxDot = -Double.MAX_VALUE;
        vector.normalize();

        for (BlockFace face : ADJACENT) {
            Vector faceVector = face.getDirection();
            double dot = vector.normalize().dot(faceVector);
            if (dot > maxDot) {
                maxDot = dot;
                closest = face;
            }
        }
        return closest;
    }

    public static List<BlockFace> getAxis() {
        return AXIS;
    }

    public static List<BlockFace> getAdjacent() {
        return ADJACENT;
    }

    public static List<BlockFace> getSurround() {
        return SURROUND;
    }
}
