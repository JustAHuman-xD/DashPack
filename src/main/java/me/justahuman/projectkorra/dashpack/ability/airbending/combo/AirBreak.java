package me.justahuman.projectkorra.dashpack.ability.airbending.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.PlayerLocationAbility;
import me.justahuman.projectkorra.dashpack.ability.AddonComboAbility;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
public class AirBreak extends AirAbility implements PlayerLocationAbility, AddonComboAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    private double ySpeed = getDouble("YSpeed");
    private boolean resetFallDistance = getBoolean("ResetFallDistance");

    public AirBreak(Player player) {
        super(player);
        if (bPlayer.canBendIgnoreBinds(this) && !player.isOnGround()) {
            this.start();
        }
    }

    @Override
    public void progress() {
        playAirbendingParticles(player.getLocation(), getInt("Particles"));
        player.getWorld().playSound(getSound(), player);
        GeneralMethods.setVelocity(this, player, new Vector(0, ySpeed, 0));
        if (resetFallDistance) {
            player.setFallDistance(0);
        }
        bPlayer.addCooldown(this);
        remove();
    }

    @Override
    public boolean consumesDash() {
        return true;
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return true;
    }

    @Override
    public Object createNewComboInstance(Player player) {
        return new AirBreak(player);
    }

    @Override
    public String getName() {
        return "AirBreak";
    }
}
