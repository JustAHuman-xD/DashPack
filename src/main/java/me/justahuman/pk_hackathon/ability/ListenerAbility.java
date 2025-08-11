package me.justahuman.pk_hackathon.ability;

import me.justahuman.pk_hackathon.PKHackathon;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public interface ListenerAbility extends Listener, MyAddonAbility {
    @Override
    default void load() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PKHackathon.instance);
    }

    @Override
    default void stop() {
        HandlerList.unregisterAll(this);
    }
}
