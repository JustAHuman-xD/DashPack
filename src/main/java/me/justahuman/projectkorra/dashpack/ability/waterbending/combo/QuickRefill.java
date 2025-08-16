package me.justahuman.projectkorra.dashpack.ability.waterbending.combo;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.PlayerLocationAbility;
import me.justahuman.projectkorra.dashpack.ability.AddonComboAbility;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@Getter
public class QuickRefill extends WaterAbility implements PlayerLocationAbility, AddonComboAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.DURATION)
    private long dashTime = getLong("DashTime");
    private long Interval = getLong("Interval");
    private int maxFill = getInt("MaxFill");
    private boolean playSound = getBoolean("PlaySound");

    private long time;

    public QuickRefill(Player player) {
        super(player);
        if (bPlayer.canBendIgnoreBinds(this) && !CoreAbility.hasAbility(player, QuickRefill.class)) {
            start();
        }
    }

    @Override
    public void progress() {
        long time = System.currentTimeMillis();
        if (time - getStartTime() >= dashTime) {
            remove();
            return;
        } else if (time - this.time < this.Interval) {
            return;
        }
        this.time = time;

        PlayerInventory inventory = player.getInventory();
        if (player.isInWaterOrRainOrBubbleColumn() && inventory.contains(Material.GLASS_BOTTLE)) {
            ItemStack waterBottle = WaterReturn.waterBottleItem();
            for (int i = 0; i < maxFill && inventory.contains(Material.GLASS_BOTTLE); i++) {
                int slot = inventory.first(Material.GLASS_BOTTLE);
                ItemStack item = inventory.getItem(slot);
                if (item != null) {
                    item.subtract();
                    if (item.isEmpty()) {
                        inventory.setItem(slot, waterBottle.clone());
                    } else {
                        inventory.addItem(waterBottle.clone()).values().forEach(excess -> player.getWorld().dropItemNaturally(player.getLocation(), excess));
                    }
                }
            }
            if (playSound) {
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 0.25f, 1);
            }
            bPlayer.addCooldown(this);
            remove();
        }
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
        return new QuickRefill(player);
    }

    @Override
    public String getName() {
        return "QuickRefill";
    }
}
