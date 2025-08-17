package me.justahuman.projectkorra.dashpack.ability.chi.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.DashingCombo;
import me.justahuman.projectkorra.dashpack.ability.PlayerLocationAbility;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Getter
public class ReflexiveStrike extends ChiAbility implements PlayerLocationAbility, DashingCombo {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.DURATION)
    private long dashTime = getLong("DashTime");
    private long hitInterval = getLong("HitInterval");
    private double hitRadius = getDouble("HitRadius");
    @Attribute(Attribute.DAMAGE)
    private double damage = getDouble("Damage");
    private int maxHits = getInt("MaxHits", -1);

    private Vector initialVelocity;
    private long time;
    private Set<UUID> hit = new HashSet<>();

    public ReflexiveStrike(Player player) {
        super(player);
        if (bPlayer.canBendIgnoreBinds(this) && !CoreAbility.hasAbility(player, ReflexiveStrike.class)) {
            initialVelocity = player.getVelocity();
            start();
        }
    }

    @Override
    public void progress() {
        long time = System.currentTimeMillis();
        if (time - this.time < this.hitInterval) {
            return;
        }
        this.time = time;

        if (time - getStartTime() >= dashTime) {
            remove();
            return;
        }

        Predicate<Entity> predicate = GeneralMethods.getEntityFilter().and(entity -> entity != player && !hit.contains(entity.getUniqueId()) && entity instanceof LivingEntity && !RegionProtection.isRegionProtected(this, entity.getLocation()));
        for (Entity entity : player.getWorld().getNearbyEntities(player.getBoundingBox().expand(hitRadius), predicate)) {
            LivingEntity target = (LivingEntity) entity;
            target.damage(damage, player);
            player.getWorld().playSound(getSound(), player);
            hit.add(target.getUniqueId());
            bPlayer.addCooldown(this);

            if (maxHits != -1 && hit.size() >= maxHits) {
                remove();
                return;
            }
        }

        if (!isDashing()) {
            remove();
        }
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
        return new ReflexiveStrike(player);
    }

    @Override
    public String getName() {
        return "ReflexiveStrike";
    }
}
