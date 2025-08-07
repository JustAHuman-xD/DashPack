package me.justahuman.pk_hackathon;

import com.projectkorra.projectkorra.ability.CoreAbility;
import me.justahuman.pk_hackathon.ability.TempDashAbility;
import me.justahuman.pk_hackathon.listener.DashListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

// TODO: Make a config
public final class PKHackathon extends JavaPlugin {
    public static PKHackathon instance;
    public static TempDashAbility dash; // Temp

    @Override
    public void onEnable() {
        instance = this;

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new DashListener(), this);

        CoreAbility.registerPluginAbilities(this, "me.justahuman.pk_hackathon.ability");
    }

    @Override
    public void onDisable() {}
}
