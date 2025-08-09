package me.justahuman.pk_hackathon.ability.airbending.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import lombok.Getter;
import me.justahuman.pk_hackathon.PlayerLocationAbility;
import me.justahuman.pk_hackathon.ability.AddonComboAbility;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
public class AirBreak extends AirAbility implements PlayerLocationAbility, AddonComboAbility {
    public static final ComboManager.AbilityInformation COMBO_INFO = new ComboManager.AbilityInformation("AirSuction", ClickType.LEFT_CLICK);

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
        GeneralMethods.setVelocity(this, player, new Vector(0, ySpeed, 0));
        if (resetFallDistance) {
            player.setFallDistance(0);
        }
        bPlayer.addCooldown(this);
        remove();
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
    public String getName() {
        return "AirBreak";
    }
}
