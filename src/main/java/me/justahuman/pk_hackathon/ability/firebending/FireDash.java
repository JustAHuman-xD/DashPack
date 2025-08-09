package me.justahuman.pk_hackathon.ability.firebending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ParticleEffect;
import lombok.Getter;
import me.justahuman.pk_hackathon.ability.DashAbility;
import me.justahuman.pk_hackathon.util.DashDirection;
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

    private final Input input;
    private final DashDirection direction;

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
            effect.display(player.getLocation().add(offset), 0, pushedDirection.getX(), pushedDirection.getY(), pushedDirection.getZ(), 0.1 + (Math.random() * 0.25));
        }
        playFirebendingSound(getLocation());
    }

    @Override
    public String getName() {
        return "FireDash";
    }

    @Override
    public boolean tryDash(BendingPlayer player, Input input, DashDirection direction) {
        return new FireDash(player.getPlayer(), input, direction).isStarted();
    }
}
