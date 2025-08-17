package me.justahuman.projectkorra.dashpack.ability.chi.combo;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.MovementHandler;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.DashingCombo;
import me.justahuman.projectkorra.dashpack.ability.PlayerLocationAbility;
import org.bukkit.Particle;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@Getter
public class Disable extends ChiAbility implements PlayerLocationAbility, DashingCombo {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.DURATION)
    private long dashTime = getLong("DashTime");
    @Attribute(Attribute.DAMAGE)
    private double damage = getDouble("Damage");
    private long hitInterval = getLong("HitInterval");
    private double hitRadius = getDouble("HitRadius");
    private int hitParticles = getInt("HitParticles");
    private long duration = getLong("Duration");

    private Vector initialVelocity;
    private long time;

    public Disable(Player player) {
        super(player);
        if (bPlayer.canBendIgnoreBinds(this) && !CoreAbility.hasAbility(player, Disable.class)) {
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

        Predicate<Entity> predicate = GeneralMethods.getEntityFilter().and(entity -> entity != player && entity instanceof LivingEntity && !RegionProtection.isRegionProtected(this, entity.getLocation()));
        for (Entity entity : player.getWorld().getNearbyEntities(player.getBoundingBox().expand(hitRadius), predicate)) {
            LivingEntity target = (LivingEntity) entity;
            if (target instanceof Creature creature) {
                creature.setTarget(null);
            } else if (target instanceof Player targetPlayer && Suffocate.isChannelingSphere(targetPlayer)) {
                Suffocate.remove(targetPlayer);
            }

            BoundingBox box = target.getBoundingBox();
            MovementHandler mh = new MovementHandler(target, CoreAbility.getAbility(Disable.class));
            mh.stopWithDuration(this.duration / 1000 * 20, Element.CHI.getColor() + "* Disabled *");
            target.getWorld().playSound(getSound(), target);
            target.getWorld().spawnParticle(Particle.ENCHANTED_HIT, target.getLocation().add(0, target.getHeight() / 2, 0), hitParticles, box.getWidthX() / 4, box.getHeight() / 4, box.getWidthZ() / 4, 0);
            target.damage(damage, player);
            bPlayer.addCooldown(this);
            remove();
            return;
        }

        if (!isDashing()) {
            remove();
        }
    }

    @Override
    public int getPriority() {
        return 100;
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
    public String getName() {
        return "Disable";
    }

    @Override
    public Object createNewComboInstance(Player player) {
        return new Disable(player);
    }
}
