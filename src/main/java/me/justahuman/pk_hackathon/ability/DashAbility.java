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
import me.justahuman.pk_hackathon.PlayerLocationAbility;
import me.justahuman.pk_hackathon.ability.airbending.AirDash;
import me.justahuman.pk_hackathon.ability.chi.ChiDash;
import me.justahuman.pk_hackathon.ability.earthbending.EarthDash;
import me.justahuman.pk_hackathon.ability.firebending.FireDash;
import me.justahuman.pk_hackathon.ability.waterbending.WaterDash;
import me.justahuman.pk_hackathon.util.DashDirection;
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

        if (!player.isOnGround() && !player.isInWaterOrBubbleColumn() && !ability.inAir()) {
            return false;
        }

        if (player.getFoodLevel() <= 6) {
            return false;
        }

        return !getVelocityDirection().isZero();
    }

    default boolean usingCombo(BendingPlayer player, Supplier<? extends CoreAbility> supplier, ComboManager.AbilityInformation... abilities) {
        ArrayList<ComboManager.AbilityInformation> recent = ComboManager.getRecentlyUsedAbilities(player.getPlayer(), abilities.length);
        if (recent.size() == abilities.length) {
            for (int i = 0; i < abilities.length; i++) {
                if (!recent.get(i).equalsWithoutTime(abilities[i])) {
                    return false;
                }
            }

            if (supplier.get().isStarted()) {
                addUsage(player.getPlayer());
                return true;
            }
        }
        return false;
    }

    default void adjustVelocity() {
        Player player = getPlayer();
        Vector dashVelocity = getVelocityDirection().multiply(getSpeed());
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
        ComboManager.addRecentAbility(player, new ComboManager.AbilityInformation(getName(), ClickType.CUSTOM, System.currentTimeMillis()));
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
        return getInt("PitchRestriction");
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
