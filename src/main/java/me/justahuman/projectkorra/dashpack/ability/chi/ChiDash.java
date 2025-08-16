package me.justahuman.projectkorra.dashpack.ability.chi;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import me.justahuman.projectkorra.dashpack.util.DashDirection;
import org.bukkit.Color;
import org.bukkit.Input;
import org.bukkit.entity.Player;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class ChiDash extends ChiAbility implements DashAbility {
    private static final Color PARTICLE_COLOR = Color.fromRGB(16755200);

    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.SELF_PUSH)
    private double speed = getBaseSpeed();

    private Input input;
    private DashDirection direction;

    public ChiDash(Player player, Input input, DashDirection direction) {
        super(player);
        this.input = input;
        this.direction = direction;
        if (canDash(this)) {
            start();
        }
    }

    @Override
    public Color getParticleColor() {
        return PARTICLE_COLOR;
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
        return "ChiDash";
    }

    @Override
    public boolean tryDash(BendingPlayer player, Input input, DashDirection direction) {
        this.input = input;
        this.direction = direction;
        return new ChiDash(player.getPlayer(), input, direction).isStarted();
    }
}
