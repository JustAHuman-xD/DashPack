package me.justahuman.pk_hackathon.ability.airbending.combo;

import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.airbending.AirBurst;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.AttributeModification;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.event.AbilityRecalculateAttributeEvent;
import com.projectkorra.projectkorra.util.ClickType;
import lombok.Getter;
import me.justahuman.pk_hackathon.PKHackathon;
import me.justahuman.pk_hackathon.PlayerLocationAbility;
import me.justahuman.pk_hackathon.ability.AddonComboAbility;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;

@Getter
public class AirCrash extends AirAbility implements Listener, PlayerLocationAbility, AddonComboAbility {
    private static final NamespacedKey DAMAGE_MODIFIER = PKHackathon.key("AirCrashDamage");
    private static final NamespacedKey KNOCKBACK_MODIFIER = PKHackathon.key("AirCrashKnockback");
    private static final ComboManager.AbilityInformation COMBO_INFO = new ComboManager.AbilityInformation("AirDash", ClickType.CUSTOM);

    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.DURATION)
    private long dashTime = getLong("DashTime");
    private double pushFactor = getDouble("PushFactor");
    private double damageFactor = getDouble("DamageFactor");

    private final AirBurst airBurst;

    public AirCrash(Player player, AirBurst airBurst, long dashTime) {
        super(player);
        this.airBurst = airBurst;
        if (dashTime <= this.dashTime && bPlayer.canBendIgnoreBinds(this)) {
            start();
        }
    }

    @Override
    public void load() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PKHackathon.instance);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onAirBurst(AbilityRecalculateAttributeEvent event) {
        if (event.getAbility() instanceof AirBurst burst && burst.isFallBurst()) {
            ArrayList<ComboManager.AbilityInformation> recent = ComboManager.getRecentlyUsedAbilities(burst.getPlayer(), 1);
            if (!recent.isEmpty() && recent.get(0).equalsWithoutTime(COMBO_INFO)) {
                AirCrash crash = new AirCrash(burst.getPlayer(), burst, System.currentTimeMillis() - recent.get(0).getTime());
                if (crash.isStarted()) {
                    switch(event.getAttribute()) {
                        case Attribute.SELF_PUSH -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, crash.pushFactor, KNOCKBACK_MODIFIER));
                        case Attribute.DAMAGE -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, crash.damageFactor, DAMAGE_MODIFIER));
                    }
                }
            }
        }
    }

    @Override
    public void progress() {
        // TODO: Some effect/sound to indicate the crash
        airBurst.setFallThreshold(-1);
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
        return "AirCrash";
    }
}
