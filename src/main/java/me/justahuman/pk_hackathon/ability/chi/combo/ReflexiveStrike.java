package me.justahuman.pk_hackathon.ability.chi.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import lombok.Getter;
import me.justahuman.pk_hackathon.ability.PlayerLocationAbility;
import me.justahuman.pk_hackathon.ability.AddonComboAbility;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * TODO: Make this, disable, quick refill, etc, stop early if the player stops moving.
 */
@Getter
public class ReflexiveStrike extends ChiAbility implements PlayerLocationAbility, AddonComboAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.DURATION)
    private long dashTime = getLong("DashTime");
    private long hitInterval = getLong("HitInterval");
    private double hitRadius = getDouble("HitRadius");
    @Attribute(Attribute.DAMAGE)
    private double damage = getDouble("Damage");

    private long time;
    private Set<UUID> hit = new HashSet<>();

    public ReflexiveStrike(Player player) {
        super(player);
        if (bPlayer.canBendIgnoreBinds(this) && !CoreAbility.hasAbility(player, ReflexiveStrike.class)) {
            start();
        }
    }

    @Override
    public void progress() {
        long time = System.currentTimeMillis();
        if (time - getStartTime() >= dashTime) {
            remove();
            return;
        } else if (time - this.time < this.hitInterval) {
            return;
        }
        this.time = time;

        Predicate<Entity> predicate = GeneralMethods.getEntityFilter().and(entity -> entity != player && !hit.contains(entity.getUniqueId()) && entity instanceof LivingEntity && !RegionProtection.isRegionProtected(this, entity.getLocation()));
        for (Entity entity : player.getWorld().getNearbyEntities(player.getBoundingBox().expand(hitRadius), predicate)) {
            LivingEntity target = (LivingEntity) entity;
            target.damage(damage, player);
            hit.add(target.getUniqueId());
            bPlayer.addCooldown(this);
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
