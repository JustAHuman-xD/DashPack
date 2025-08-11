package me.justahuman.pk_hackathon.ability.airbending.combo;

import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirBurst;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.AttributeModification;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.event.AbilityRecalculateAttributeEvent;
import lombok.Getter;
import me.justahuman.pk_hackathon.PKHackathon;
import me.justahuman.pk_hackathon.ability.PlayerLocationAbility;
import me.justahuman.pk_hackathon.ability.AddonComboAbility;
import me.justahuman.pk_hackathon.ability.ListenerAbility;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@Getter
public class AirCrash extends AirAbility implements ListenerAbility, PlayerLocationAbility, AddonComboAbility {
    private static final NamespacedKey RANGE_MODIFIER = PKHackathon.key("AirCrashRange");
    private static final NamespacedKey DAMAGE_MODIFIER = PKHackathon.key("AirCrashDamage");
    private static final NamespacedKey KNOCKBACK_MODIFIER = PKHackathon.key("AirCrashKnockback");

    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.DURATION)
    private long dashTime = getLong("DashTime");
    private double rangeFactor = getDouble("RangeFactor");
    private double pushFactor = getDouble("PushFactor");
    private double damageFactor = getDouble("DamageFactor");

    private final AirBurst airBurst;
    private boolean removable = false;

    public AirCrash(Player player, AirBurst airBurst, long dashTime) {
        super(player);
        this.airBurst = airBurst;
        if (dashTime <= this.dashTime && bPlayer.canBendIgnoreBinds(this)) {
            start();
        }
    }

    @EventHandler
    public void onAirBurst(AbilityRecalculateAttributeEvent event) {
        if (event.getAbility() instanceof AirBurst burst && burst.isFallBurst()) {
            AirCrash crash = null;
            for (AirCrash airCrash : CoreAbility.getAbilities(burst.getPlayer(), AirCrash.class)) {
                if (airCrash.airBurst.equals(burst)) {
                    crash = airCrash;
                    break;
                }
            }

            if (crash == null) {
                long comboTime = checkCustomCombo(burst.getPlayer());
                if (comboTime != -1) {
                    new AirCrash(burst.getPlayer(), burst, System.currentTimeMillis() - comboTime);
                }
            }

            if (crash != null && crash.isStarted()) {
                switch(event.getAttribute()) {
                    case Attribute.SELF_PUSH -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, crash.pushFactor, KNOCKBACK_MODIFIER));
                    case Attribute.DAMAGE -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, crash.damageFactor, DAMAGE_MODIFIER));
                }
            }
        } else if (event.getAbility() instanceof AirBlast blast && blast.getSource() != null && event.getAttribute().equals(Attribute.RANGE)) {
            for (AirCrash airCrash : CoreAbility.getAbilities(blast.getPlayer(), AirCrash.class)) {
                if (airCrash.airBurst.equals(blast.getSource())) {
                    airCrash.removable = true;
                    event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, airCrash.rangeFactor, RANGE_MODIFIER));
                    break;
                }
            }
        }
    }

    @Override
    public void progress() {
        // TODO: Some effect/sound to indicate the crash
        airBurst.setFallThreshold(-1);
        bPlayer.addCooldown(this);

        if (removable || System.currentTimeMillis() - getStartTime() >= dashTime) {
            remove();
        }
    }

    @Override
    public boolean forceCustomHandling() {
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
        return null;
    }

    @Override
    public String getName() {
        return "AirCrash";
    }
}
