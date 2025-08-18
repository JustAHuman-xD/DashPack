package me.justahuman.projectkorra.dashpack.ability.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.BloodAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import me.justahuman.projectkorra.dashpack.util.DashDirection;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Color;
import org.bukkit.Input;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class BloodDash extends BloodAbility implements DashAbility {
    private static final Color BLOOD_COLOR = Color.fromRGB(0x730C00);

    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.SELF_PUSH)
    private double speed = getBaseSpeed();
    private double damage = getDouble("Damage");

    private Input input;
    private DashDirection direction;

    public BloodDash(Player player, Input input, DashDirection direction) {
        super(player);
        this.input = input;
        this.direction = direction;
        if (canDash(this) && player.getHealth() > damage) {
            start();
        }
    }

    @Override
    public void progress() {
        player.damage(damage, player);
        player.playSound(getSound(), Sound.Emitter.self());
        DashAbility.super.progress();
    }

    @Override
    public void dashEffect() {
        BoundingBox box = player.getBoundingBox();
        player.getWorld().spawnParticle(
                Particle.BLOCK,
                player.getLocation().add(0, player.getHeight() / 2, 0),
                particleCount(),
                box.getWidthX() / 6,
                box.getHeight() / 4,
                box.getWidthZ() / 6,
                0,
                Material.REDSTONE_BLOCK.createBlockData()
        );
        DashAbility.super.dashEffect();
    }

    @Override
    public Color getParticleColor() {
        return BLOOD_COLOR;
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
        return "BloodDash";
    }

    @Override
    public boolean tryDash(BendingPlayer player, Input input, DashDirection direction) {
        return false;
    }
}
