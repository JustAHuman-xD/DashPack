package me.justahuman.pk_hackathon.ability.earthbending.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import lombok.Getter;
import me.justahuman.pk_hackathon.ability.PlayerLocationAbility;
import me.justahuman.pk_hackathon.ability.AddonComboAbility;
import me.justahuman.pk_hackathon.ability.earthbending.EarthDash;
import me.justahuman.pk_hackathon.util.DashDirection;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class EarthLaunch extends EarthAbility implements PlayerLocationAbility, AddonComboAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.SELF_PUSH)
    private double pushFactor = getDouble("PushFactor");
    private boolean additive = getBoolean("Additive");

    private Vector launchVector;

    public EarthLaunch(Player player, Input input, DashDirection direction) {
        super(player);
        this.launchVector = direction.getVector(player, input, 0);

        CoreAbility catapult = CoreAbility.getAbility("Catapult");
        if (catapult != null && !bPlayer.canBend(catapult)) {
            return;
        }

        if (bPlayer.canBendIgnoreBinds(this) && !this.launchVector.isZero()) {
            this.start();
        }
    }

    @Override
    public void progress() {
        // TODO: launch particle effect
        playEarthbendingSound(getLocation());
        this.launchVector = this.launchVector.multiply(pushFactor).add(new Vector(0, pushFactor, 0));
        GeneralMethods.setVelocity(this, player, additive ? player.getVelocity().add(launchVector) : launchVector);
        CoreAbility catapult = CoreAbility.getAbility("Catapult");
        if (catapult != null) {
            bPlayer.addCooldown(catapult);
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
        return false;
    }

    @Override
    public Object createNewComboInstance(Player player) {
        EarthDash dash = (EarthDash) CoreAbility.getAbility(EarthDash.class);
        return dash == null ? null : new EarthLaunch(player, dash.getInput(), dash.getDirection());
    }

    @Override
    public String getName() {
        return "EarthLaunch";
    }
}
