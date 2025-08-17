package me.justahuman.projectkorra.dashpack.ability.airbending.combo;

import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirBurst;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.AttributeModification;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.event.AbilityRecalculateAttributeEvent;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.DashPack;
import me.justahuman.projectkorra.dashpack.ability.PlayerLocationAbility;
import me.justahuman.projectkorra.dashpack.ability.AddonComboAbility;
import me.justahuman.projectkorra.dashpack.ability.ListenerAbility;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@Getter
public class AirCrash extends AirAbility implements ListenerAbility, PlayerLocationAbility, AddonComboAbility {
    private static final NamespacedKey RANGE_MODIFIER = DashPack.key("AirCrashRange");
    private static final NamespacedKey DAMAGE_MODIFIER = DashPack.key("AirCrashDamage");
    private static final NamespacedKey KNOCKBACK_MODIFIER = DashPack.key("AirCrashKnockback");

    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.DURATION)
    private long dashTime = getLong("DashTime");
    private double rangeFactor = getDouble("RangeFactor");
    private double knockbackFactor = getDouble("KnockbackFactor");
    private double damageFactor = getDouble("DamageFactor");

    private final AirBurst airBurst;
    private boolean removable = false;

    public AirCrash(Player player, AirBurst airBurst, long dashTime) {
        super(player);
        this.airBurst = airBurst;
        if (dashTime <= this.dashTime && bPlayer.canBendIgnoreBinds(this)) {
            start();
            if (isStarted()) {
                this.airBurst.setFallThreshold(0);
                this.bPlayer.addCooldown(this);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAirBurst(AbilityStartEvent event) {
        if (event.getAbility() instanceof AirBurst burst && burst.isFallBurst() && !CoreAbility.hasAbility(burst.getPlayer(), AirCrash.class)) {
            long comboTime = checkCustomCombo(burst.getPlayer());
            if (comboTime != -1) {
                new AirCrash(burst.getPlayer(), burst, System.currentTimeMillis() - comboTime);
            }
        }
    }

    @EventHandler
    public void onBlastFromBurst(AbilityRecalculateAttributeEvent event) {
        if (event.getAbility() instanceof AirBlast blast && blast.getSource() != null) {
            AirCrash airCrash = CoreAbility.getAbility(blast.getPlayer(), AirCrash.class);
            if (airCrash != null && airCrash.airBurst == blast.getSource()) {
                airCrash.removable = true;
                switch(event.getAttribute()) {
                    case Attribute.RANGE -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, airCrash.rangeFactor, RANGE_MODIFIER));
                    case Attribute.KNOCKBACK -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, airCrash.knockbackFactor, KNOCKBACK_MODIFIER));
                    case Attribute.DAMAGE -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, airCrash.damageFactor, DAMAGE_MODIFIER));
                }
            }
        }
    }

    @Override
    public void progress() {
        if (removable || System.currentTimeMillis() - getStartTime() >= dashTime) {
            if (removable) {
                player.getWorld().playSound(getSound(), player);
            }
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
