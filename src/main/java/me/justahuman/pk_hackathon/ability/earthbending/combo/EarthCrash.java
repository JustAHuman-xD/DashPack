package me.justahuman.pk_hackathon.ability.earthbending.combo;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.AttributeModification;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.earthbending.Ripple;
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;

@Getter
public class EarthCrash extends EarthAbility implements Listener, PlayerLocationAbility, AddonComboAbility {
    // TODO: PR: Ripple needs to reinitialize locations on attribute recalculation
    private static final MethodHandle INITIALIZE_LOCATIONS;
    static {
        MethodHandle handle = null;
        try {
            Method method = Ripple.class.getDeclaredMethod("initializeLocations");
            method.setAccessible(true);
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Ripple.class, MethodHandles.lookup());
            handle = lookup.unreflect(method);
        } catch (Exception e) {
            PKHackathon.instance.getLogger().severe("Failed to find Ripple#initializeLocations method for EarthCrash ability.");
            e.printStackTrace();
        }
        INITIALIZE_LOCATIONS = handle;
    }

    private static final NamespacedKey RANGE_MODIFIER = PKHackathon.key("EarthCrashRange");
    private static final NamespacedKey DAMAGE_MODIFIER = PKHackathon.key("EarthCrashDamage");
    private static final NamespacedKey KNOCKBACK_MODIFIER = PKHackathon.key("EarthCrashKnockback");
    private static final ComboManager.AbilityInformation COMBO_INFO = new ComboManager.AbilityInformation("EarthDash", ClickType.CUSTOM);

    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.DURATION)
    private long dashTime = getLong("DashTime");
    private double rangeFactor = getDouble("RangeFactor");
    private double pushFactor = getDouble("PushFactor");
    private double damageFactor = getDouble("DamageFactor");

    private final Ripple ripple;

    public EarthCrash(Player player, Ripple ripple, long dashTime) {
        super(player);
        this.ripple = ripple;
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
    public void onRipple(AbilityRecalculateAttributeEvent event) {
        if (event.getAbility() instanceof Ripple ripple) {
            EarthCrash crash = null;
            for (EarthCrash earthCrash : CoreAbility.getAbilities(ripple.getPlayer(), EarthCrash.class)) {
                if (earthCrash.ripple.equals(ripple)) {
                    crash = earthCrash;
                    break;
                }
            }

            if (crash == null) {
                ArrayList<ComboManager.AbilityInformation> recent = ComboManager.getRecentlyUsedAbilities(ripple.getPlayer(), 1);
                if (!recent.isEmpty() && recent.get(0).equalsWithoutTime(COMBO_INFO) ) {
                    new EarthCrash(ripple.getPlayer(), ripple, System.currentTimeMillis() - recent.get(0).getTime());
                }
            }

            if (crash != null && crash.isStarted()) {
                switch (event.getAttribute()) {
                    case Attribute.RANGE -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, crash.rangeFactor, RANGE_MODIFIER));
                    case Attribute.DAMAGE -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, crash.damageFactor, DAMAGE_MODIFIER));
                    case Attribute.KNOCKBACK -> event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, crash.pushFactor, KNOCKBACK_MODIFIER));
                }

                try {
                    ripple.getLocations().clear();
                    if (INITIALIZE_LOCATIONS != null) {
                        INITIALIZE_LOCATIONS.invoke(ripple);
                        ripple.setMaxStep(ripple.getLocations().size());
                    } else {
                        throw new IllegalStateException("EarthCrash started when no Ripple#initializeLocations method was found");
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void progress() {
        bPlayer.addCooldown(this);
        remove();
    }

    @Override
    public boolean isEnabled() {
        return INITIALIZE_LOCATIONS != null && super.isEnabled();
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
        return "EarthCrash";
    }
}
