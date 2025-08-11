package me.justahuman.pk_hackathon.ability.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;
import lombok.Getter;
import me.justahuman.pk_hackathon.ability.DashAbility;
import me.justahuman.pk_hackathon.util.DashDirection;
import org.bukkit.Input;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class WaterDash extends WaterAbility implements DashAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.SELF_PUSH)
    private double speed = getBaseSpeed();
    private int waterPitchRestriction = getInt("InWaterPitchRestriction", getPitchRestriction());
    private boolean requireWater = getBoolean("RequireWater");
    private boolean consumeWater = getBoolean("ConsumeWater");

    private Input input;
    private DashDirection direction;

    public WaterDash(Player player, Input input, DashDirection direction) {
        super(player);
        this.input = input;
        this.direction = direction;
        if (canDash(this)) {
            start();
            if (isStarted()) {
                if (requireWater && consumeWater && !player.isInWaterOrRainOrBubbleColumn()) {
                    WaterReturn.emptyWaterBottle(player);
                }
            }
        }
    }

    @Override
    public <A extends CoreAbility & DashAbility> boolean canDash(A ability) {
        if (requireWater && !player.isInWaterOrRainOrBubbleColumn() && !WaterReturn.hasWaterBottle(player)) {
            return false;
        }
        return DashAbility.super.canDash(ability);
    }

    @Override
    public void dashEffect() {
        Player player = getPlayer();
        World world = player.getWorld();
        BoundingBox box = player.getBoundingBox();
        Vector pushedDirection = getVelocityDirection().multiply(-1);
        for (int i = 0; i < particleCount(); i++) {
            Vector offset = new Vector((Math.random() - 0.5) * 0.8 * box.getWidthX(), (Math.random() - 0.5) * 0.8 * box.getHeight(), (Math.random() - 0.5) * 0.8 * box.getWidthZ());
            offset.add(new Vector(0, box.getHeight() / 2, 0));
            if (player.isInWaterOrRainOrBubbleColumn()) {
                world.spawnParticle(Particle.BUBBLE, player.getLocation().add(offset).add(Math.random() * pushedDirection.getX(), Math.random() * pushedDirection.getY(), Math.random() * pushedDirection.getZ()), 1);
            } else {
                world.spawnParticle(Particle.SPLASH, player.getLocation().add(offset), 0, pushedDirection.getX(), pushedDirection.getY(), pushedDirection.getZ(), 0.1 + (Math.random() * 0.25));
            }
        }
        playWaterbendingSound(getLocation());
    }

    @Override
    public boolean inAir() {
        return player.isInRain() || DashAbility.super.inAir();
    }

    @Override
    public int getPitchRestriction() {
        return player.isInWaterOrRainOrBubbleColumn() ? waterPitchRestriction : DashAbility.super.getPitchRestriction();
    }

    @Override
    public String getName() {
        return "WaterDash";
    }

    @Override
    public boolean tryDash(BendingPlayer player, Input input, DashDirection direction) {
        this.input = input;
        this.direction = direction;
        return new WaterDash(player.getPlayer(), input, direction).isStarted();
    }
}
