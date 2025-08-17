package me.justahuman.projectkorra.dashpack.ability.firebending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ParticleEffect;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import me.justahuman.projectkorra.dashpack.ability.firebending.combo.FireTunnel;
import me.justahuman.projectkorra.dashpack.ability.firebending.combo.LightningStep;
import me.justahuman.projectkorra.dashpack.util.DashDirection;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class FireDash extends FireAbility implements DashAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.SELF_PUSH)
    private double speed = getBaseSpeed();

    private Input input;
    private DashDirection direction;

    public FireDash(Player player, Input input, DashDirection direction) {
        super(player);
        this.input = input;
        this.direction = direction;
        if (canDash(this)) {
            start();
        }
    }

    @Override
    public void dashEffect() {
        Player player = getPlayer();
        BoundingBox box = player.getBoundingBox();
        Vector pushedDirection = getVelocityDirection().multiply(-1);
        ParticleEffect effect = bPlayer.hasSubElement(Element.BLUE_FIRE) ? ParticleEffect.SOUL_FIRE_FLAME : ParticleEffect.FLAME;
        for (int i = 0; i < particleCount(); i++) {
            Vector offset = new Vector((Math.random() - 0.5) * 0.8 * box.getWidthX(), (Math.random() - 0.5) * 0.8 * box.getHeight(), (Math.random() - 0.5) * 0.8 * box.getWidthZ());
            offset.add(new Vector(0, box.getHeight() / 2, 0));
            effect.display(player.getLocation().add(offset), 0, pushedDirection.getX(), pushedDirection.getY(), pushedDirection.getZ(), 0.4 + (Math.random() * 0.4));
        }
        playFirebendingSound(getLocation());
    }

    @Override
    public double getSpeed() {
        return speed * (bPlayer.hasSubElement(Element.BLUE_FIRE) ? getDouble("BlueFireSpeedFactor", 1) : 1.0);
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
        return "FireDash";
    }

    @Override
    public boolean tryDash(BendingPlayer player, Input input, DashDirection direction) {
        this.input = input;
        this.direction = direction;
        return usingCombo(player, LightningStep.class) || usingCombo(player, FireTunnel.class)
                || new FireDash(player.getPlayer(), input, direction).isStarted();
    }
}
