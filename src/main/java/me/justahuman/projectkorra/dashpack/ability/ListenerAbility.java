package me.justahuman.projectkorra.dashpack.ability;

import me.justahuman.projectkorra.dashpack.DashPack;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public interface ListenerAbility extends Listener, MyAddonAbility {
    @Override
    default void load() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DashPack.instance);
    }

    @Override
    default void stop() {
        HandlerList.unregisterAll(this);
    }
}
