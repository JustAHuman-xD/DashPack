package me.justahuman.pk_hackathon.listener;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.event.AbilityLoadEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import me.justahuman.pk_hackathon.PKHackathon;
import me.justahuman.pk_hackathon.ability.DashAbility;
import me.justahuman.pk_hackathon.ability.MyAddonAbility;
import me.justahuman.pk_hackathon.util.DashDirection;
import me.justahuman.pk_hackathon.util.DashTap;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class HackathonListener implements Listener {
    // TODO: Adjust timeout based on players ping
    private static final int DASH_TAP_TIMEOUT = 3;

    private final Map<UUID, DashData> dashing = new HashMap<>();

    @EventHandler
    public void onBendingReload(BendingReloadEvent event) {
        PKHackathon.instance.reloadConfig();
        dashing.clear();
    }

    @EventHandler
    public void onAbilityLoad(AbilityLoadEvent<?> event) {
        // TODO: Make a PR that makes using CoreAbility.registerPluginAbilities() respect if an ability is an AddonAbility (it does not currently t-t)
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

        List<Element> elements = bPlayer.getElements();
        if (elements.isEmpty()) {
            return;
        }

        // TODO: Support explicitly choosing which element's dash to use
        Element chosen = elements.get(0);
        DashAbility dashAbility = DashAbility.get(chosen);
        if (dashAbility == null) {
            return;
        }

        int time = player.getTicksLived();
        Input oldInput = player.getCurrentInput();
        Input newInput = event.getInput();

        DashData dashData = dashing.remove(player.getUniqueId());
        if (dashData != null && time - dashData.tapTime > DASH_TAP_TIMEOUT) {
            dashData = null;
        }

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
