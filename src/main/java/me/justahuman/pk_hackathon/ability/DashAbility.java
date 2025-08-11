package me.justahuman.pk_hackathon.ability;

import com.destroystokyo.paper.ParticleBuilder;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;
import me.justahuman.pk_hackathon.PKHackathon;
import me.justahuman.pk_hackathon.ability.airbending.AirDash;
import me.justahuman.pk_hackathon.ability.chi.ChiDash;
import me.justahuman.pk_hackathon.ability.earthbending.EarthDash;
import me.justahuman.pk_hackathon.ability.firebending.FireDash;
import me.justahuman.pk_hackathon.ability.waterbending.WaterDash;
import me.justahuman.pk_hackathon.util.DashDirection;
import me.justahuman.pk_hackathon.util.DashInformation;
import me.justahuman.pk_hackathon.util.Utils;
import org.bukkit.Color;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public interface DashAbility extends PlayerLocationAbility, PassiveAbility, MyAddonAbility {
    // ** Ability methods ** //

    @Override
    default void progress() {
        adjustVelocity();
        dashEffect();
        postDash();
        remove();
    }

    @Override
    default boolean isSneakAbility() {
        return false;
    }

    @Override
    default boolean isHarmlessAbility() {
        return true;
    }

    @Override
    default String getDescription() {
        return ConfigManager.languageConfig.get().getString(configPath() + ".Description");
    }

    // ** PassiveAbility methods ** //

    @Override
    default boolean isInstantiable() {
        return false;
    }

    @Override
    default boolean isProgressable() {
        return true;
    }

    // ** DashAbility methods ** //

    boolean tryDash(BendingPlayer player, Input input, DashDirection direction);

    default <A extends CoreAbility & DashAbility> boolean canDash(A ability) {
        if (!ability.isEnabled() || ability.getBendingPlayer().isOnCooldown("Dash") || !ability.getBendingPlayer().canBendIgnoreBindsCooldowns(ability)) {
            return false;
        }

        Player player = ability.getPlayer();
        if (player.isInWaterOrBubbleColumn() && !ability.inWater()) {
            return false;
        }

        if (!player.isOnGround() && !player.isInWaterOrBubbleColumn()) {
            if (!ability.inAir()) {
                return false;
            } else if (has("MaxAirHeight")) {
                double maxAir = Math.pow(getDouble("MaxAirHeight"), 2);
                Location location = player.getLocation();
                while (location.distanceSquared(player.getLocation()) < maxAir && location.getBlock().isEmpty()) {
                    location = location.add(0, -1, 0);
                }

                if (location.getBlock().isEmpty()) {
                    return false;
                }
            }
        }

        if (player.getFoodLevel() <= 6) {
            return false;
        }

        return !getVelocityDirection().isZero();
    }

    default <A extends CoreAbility & AddonComboAbility> boolean usingCombo(BendingPlayer player, Class<A> clazz) {
        A defaultAbility = (A) CoreAbility.getAbility(clazz);
        return defaultAbility != null && usingCombo(player, () -> (A) defaultAbility.createNewComboInstance(player.getPlayer()), clazz);
    }

    default <A extends CoreAbility & AddonComboAbility> boolean usingCombo(BendingPlayer player, Supplier<A> supplier, Class<A> clazz) {
        A defaultAbility = (A) CoreAbility.getAbility(clazz);
        if (defaultAbility == null) {
            return false;
        }

        ArrayList<ComboManager.AbilityInformation> combo = new ArrayList<>(ComboManager.getComboAbilities().get(defaultAbility.getName()).getAbilities());
        if (!combo.remove(AddonComboAbility.IMPOSSIBLE)) {
            return false;
        }

        if (defaultAbility.consumesDash()) {
            addUsage(player.getPlayer());
        }
        ArrayList<ComboManager.AbilityInformation> recent = ComboManager.getRecentlyUsedAbilities(player.getPlayer(), combo.size());
        if (combo.size() != recent.size()) {
            if (defaultAbility.consumesDash()) {
                removeUsage(player.getPlayer());
            }
            return false;
        }

        for (int i = 0; i < combo.size(); i++) {
            if (!combo.get(i).equalsWithoutTime(recent.get(i))) {
                if (defaultAbility.consumesDash()) {
                    removeUsage(player.getPlayer());
                }
                return false;
            }
        }

        A ability = supplier.get();
        if (ability != null && ability.isStarted()) {
            if (!defaultAbility.consumesDash()) {
                addUsage(player.getPlayer());
            }
            return true;
        } else {
            if (defaultAbility.consumesDash()) {
                removeUsage(player.getPlayer());
            }
            return false;
        }
    }

    default void adjustVelocity() {
        Player player = getPlayer();
        Vector dashVelocity = getVelocityDirection().multiply(getSpeedWithContext());
        Vector newVelocity = isAdditive() ? player.getVelocity().add(dashVelocity) : dashVelocity;
        GeneralMethods.setVelocity(this, player, newVelocity);
    }

    default void dashEffect() {
        Player player = getPlayer();
        BoundingBox box = player.getBoundingBox();
        Vector pushedDirection = getVelocityDirection().multiply(-1);
        for (int i = 0; i < pushParticleCount(); i++) {
            Vector offset = new Vector((Math.random() - 0.5) * 0.8 * box.getWidthX(), (Math.random() - 0.5) * 0.8 * box.getHeight(), (Math.random() - 0.5) * 0.8 * box.getWidthZ());
            offset.add(new Vector(0, box.getHeight() / 2, 0));
            Location particleLocation = player.getLocation().add(offset);
            Location endLocation = particleLocation.clone().add(pushedDirection.clone().multiply(Math.random() * 1.5));
            new ParticleBuilder(Particle.TRAIL)
                    .location(particleLocation)
                    .data(new Particle.Trail(endLocation, getParticleColor(), (int) ((Math.random() + 0.5) * 10)))
                    .spawn();
        }
    }

    default Color getParticleColor() {
        return Color.WHITE;
    }

    default void addUsage() {
        addUsage(getPlayer());
    }

    default void addUsage(Player player) {
        Utils.addComboAbility(player, new DashInformation(getName(), System.currentTimeMillis(), getDirection().with(getInput())));
    }

    default void removeUsage() {
        removeUsage(getPlayer());
    }

    default void removeUsage(Player player) {
        ComboManager.removeRecentType(player, ClickType.CUSTOM);
    }

    default void postDash() {
        if (autoSprint()) {
            getPlayer().setSprinting(true);
        }
        BendingPlayer.getBendingPlayer(getPlayer()).addCooldown("Dash", getCooldown());
        addUsage();
    }

    default boolean isAdditive() {
        return getBoolean("Additive");
    }

    default double getBaseSpeed() {
        return getDouble("Speed");
    }

    default int getPitchRestriction() {
        return getInt("PitchRestriction", -1);
    }

    default boolean inAir() {
        return getBoolean("InAir");
    }

    default boolean inWater() {
        return getBoolean("InWater");
    }

    default boolean autoSprint() {
        return getBoolean("AutoSprint");
    }

    default int particleCount() {
        return getInt("Particles");
    }

    default int pushParticleCount() {
        return PKHackathon.instance.getConfig().getInt("Dash.PushParticles");
    }

    Input getInput();
    DashDirection getDirection();
    double getSpeed();
    default double getSpeedWithContext() {
        double speed = getSpeed();
        Player player = getPlayer();
        if (player.isInWaterOrRainOrBubbleColumn()) {
            speed *= getDouble("InWaterSpeedFactor", 1);
        } else if (!player.isOnGround()) {
            speed *= getDouble("InAirSpeedFactor", 1);
        }
        return speed;
    }

    default Vector getVelocityDirection() {
        return getDirection().getVector(getPlayer(), getInput(), getPitchRestriction());
    }

    // ** Static Methods ** //

    static DashAbility get(Element element) {
        return (DashAbility) switch (element.getName()) {
            case "Air" -> CoreAbility.getAbility(AirDash.class);
            case "Water" -> CoreAbility.getAbility(WaterDash.class);
            case "Earth" -> CoreAbility.getAbility(EarthDash.class);
            case "Fire" -> CoreAbility.getAbility(FireDash.class);
            case "Chi" -> CoreAbility.getAbility(ChiDash.class);
            default -> null;
        };
    }
}
