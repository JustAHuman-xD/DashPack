package me.justahuman.projectkorra.dashpack.ability.airbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import me.justahuman.projectkorra.dashpack.ability.airbending.combo.AirBreak;
import me.justahuman.projectkorra.dashpack.util.DashDirection;
import org.bukkit.Input;
import org.bukkit.entity.Player;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class AirDash extends AirAbility implements DashAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.SELF_PUSH)
    private double speed = getBaseSpeed();

    private Input input;
    private DashDirection direction;

    public AirDash(Player player, Input input, DashDirection direction) {
        super(player);
        this.input = input;
        this.direction = direction;
        if (canDash(this)) {
            start();
        }
    }

    @Override
    public void dashEffect() {
        DashAbility.super.dashEffect();
        playAirbendingParticles(getLocation(), particleCount());
        playAirbendingSound(getLocation());
        // TODO: better sound effect
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
        return "AirDash";
    }

    @Override
    public boolean tryDash(BendingPlayer player, Input input, DashDirection direction) {
        this.input = input;
        this.direction = direction;
        return usingCombo(player, AirBreak.class) || new AirDash(player.getPlayer(), input, direction).isStarted();
    }
}
