package me.justahuman.projectkorra.dashpack.ability.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.Catapult;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import me.justahuman.projectkorra.dashpack.ability.earthbending.combo.EarthLaunch;
import me.justahuman.projectkorra.dashpack.ability.earthbending.combo.EarthSlam;
import me.justahuman.projectkorra.dashpack.util.DashDirection;
import org.bukkit.Input;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class EarthDash extends EarthAbility implements DashAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.SELF_PUSH)
    private double speed = getBaseSpeed();

    private Input input;
    private DashDirection direction;
    private boolean plummet;

    public EarthDash(Player player, Input input, DashDirection direction, boolean plummet) {
        super(player);
        this.input = input;
        this.direction = direction;
        this.plummet = plummet;
        if (canDash(this)) {
            start();
        }
    }

    @Override
    public boolean maxAirApplicable(Block block) {
        return isEarthbendable(player, getName(), block);
    }

    @Override
    public boolean inAir() {
        return (plummet && direction.describing(this).contains(DashDirection.DOWN)) || DashAbility.super.inAir();
    }

    @Override
    public Vector getVelocityDirection(Player player) {
        return plummet ? new Vector(0, -speed, 0) : DashAbility.super.getVelocityDirection(player);
    }

    @Override
    public boolean isAdditive() {
        return (!plummet || getPlayer().getVelocity().getY() < 0) && DashAbility.super.isAdditive();
    }

    @Override
    public void dashEffect() {
        Block beneath = getPlayer().getLocation().getBlock().getRelative(0, -1, 0);
        if (isEarthbendable(getPlayer(), getName(), beneath)) {
            getPlayer().getWorld().spawnParticle(Particle.BLOCK, getLocation().add(0, 0.15, 0), particleCount(), 0.3d, 0d, 0.3d, beneath.getBlockData());
        }
        playEarthbendingSound(getLocation());
    }

    @Override
    public boolean isHiddenAbility() {
        return DashAbility.super.isHiddenAbility();
    }

    @Override
    public String getDescription() {
        return DashAbility.super.getDescription();
    }

    @Override
    public String getName() {
        return "EarthDash";
    }

    @Override
    public boolean tryDash(BendingPlayer player, Input input, DashDirection direction) {
        this.input = input;
        this.direction = direction;

        if (!player.getPlayer().isOnGround()) {
            return new EarthDash(player.getPlayer(), input, direction, true).isStarted();
        }

        Block beneath = player.getPlayer().getLocation().getBlock().getRelative(0, -1, 0);
        if (!isEarthbendable(player.getPlayer(), getName(), beneath)) {
            return false;
        }

        return usingCombo(player, EarthLaunch.class) || usingCombo(player, EarthSlam.class)
                || new EarthDash(player.getPlayer(), input, direction, false).isStarted();
    }
}
