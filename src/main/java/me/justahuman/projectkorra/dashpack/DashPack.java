package me.justahuman.projectkorra.dashpack;

import com.google.common.base.Charsets;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import me.justahuman.projectkorra.dashpack.listener.HackathonListener;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;

public final class DashPack extends JavaPlugin {
    public static DashPack instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        FileConfiguration abilityConfig = ConfigManager.defaultConfig.get();
        InputStream abilityStream = getResource("abilities.yml");
        if (abilityStream != null) {
            abilityConfig.addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(abilityStream, Charsets.UTF_8)));
            ConfigManager.defaultConfig.save();
        }

        FileConfiguration languageConfig = ConfigManager.languageConfig.get();
        InputStream languageStream = getResource("language.yml");
        if (languageStream != null) {
            languageConfig.addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(languageStream, Charsets.UTF_8)));
            ConfigManager.languageConfig.save();
        }

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new HackathonListener(), this);

        CoreAbility.registerPluginAbilities(this, "me.justahuman.projectkorra.dashpack.ability");
    }

    @Override
    public void onDisable() {

    }

    public static NamespacedKey key(String key) {
        return new NamespacedKey(instance, key);
    }
}
