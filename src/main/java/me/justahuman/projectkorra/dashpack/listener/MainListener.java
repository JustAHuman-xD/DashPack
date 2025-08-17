package me.justahuman.projectkorra.dashpack.listener;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.event.AbilityLoadEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import me.justahuman.projectkorra.dashpack.DashPack;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import me.justahuman.projectkorra.dashpack.ability.MyAddonAbility;
import me.justahuman.projectkorra.dashpack.settings.PlayerSettings;
import me.justahuman.projectkorra.dashpack.util.DashDirection;
import me.justahuman.projectkorra.dashpack.util.DashTap;
import me.justahuman.projectkorra.dashpack.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class MainListener implements Listener {
    private final Map<UUID, DashData> dashing = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBendingReload(BendingReloadEvent event) {
        dashing.clear();
        DashPack.instance.reloadConfig();
        Bukkit.getScheduler().runTask(DashPack.instance, Utils::addPriorityToComboManager);
    }

    @EventHandler
    public void onAbilityLoad(AbilityLoadEvent<?> event) {
        if (event.getLoadable() instanceof MyAddonAbility ability) {
            ability.load();
        }
    }

    @EventHandler
    public void onDashInput(PlayerInputEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer == null) {
            return;
        }

        PlayerSettings settings = PlayerSettings.get(player);
        Element chosen = settings.element(bPlayer);
        DashAbility dashAbility = DashAbility.get(chosen);
        if (dashAbility == null) {
            return;
        }

        PlayerSettings def = PlayerSettings.def();
        int timeout = settings.inputTimeout(def);
        if (timeout == 0) {
            return;
        }

        if (settings.accountForPing(def)) {
            int ping = player.getPing();
            int pingThreshold = settings.pingThreshold(def);
            int pingIncrease = settings.pingCompensation(def);
            int maxCompensation = settings.maxPingCompensation(def);
            timeout += Math.min(maxCompensation, (ping / pingThreshold) * pingIncrease);
        }

        int time = player.getTicksLived();
        DashData dashData = dashing.remove(player.getUniqueId());
        if (dashData != null && time - dashData.tapTime > timeout) {
            dashData = null;
        }

        Input oldInput = player.getCurrentInput();
        Input newInput = event.getInput();
        DashDirection direction = DashDirection.from(oldInput, newInput, dashData != null ? dashData.tap : null);
        if (player.isSneaking() || direction == null) {
            return;
        }

        if (dashData == null || direction != dashData.direction) {
            dashing.put(player.getUniqueId(), new DashData(direction, DashTap.FIRST, time));
        } else if (dashData.tap == DashTap.FIRST) {
            dashing.put(player.getUniqueId(), new DashData(direction, DashTap.SECOND, time));
        } else {
            if (!dashAbility.tryDash(bPlayer, newInput, direction)) {
                dashing.put(player.getUniqueId(), dashData);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking() && event.getPlayer().isSprinting()) {
            event.getPlayer().setSprinting(false);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        dashing.remove(event.getPlayer().getUniqueId());
    }

    private record DashData(DashDirection direction, DashTap tap, int tapTime) { }
}
