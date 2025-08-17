package me.justahuman.projectkorra.dashpack.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import io.papermc.paper.registry.keys.SoundEventKeys;
import me.justahuman.projectkorra.dashpack.DashPack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Random;

public interface MyAddonAbility extends Ability, AddonAbility {
    Random RANDOM = new Random();
    Sound ERROR = Sound.sound(SoundEventKeys.BLOCK_NOTE_BLOCK_DIDGERIDOO, Sound.Source.MASTER, 0F, 1.0F);

    // ** AddonAbility methods ** //

    @Override
    default void load() {}

    @Override
    default void stop() {}

    @Override
    default String getAuthor() {
        return String.join(",", DashPack.instance.getPluginMeta().getAuthors());
    }

    @Override
    default String getVersion() {
        return DashPack.instance.getPluginMeta().getVersion();
    }

    // ** MyAddonAbility methods ** //

    default FileConfiguration config() {
        return ConfigManager.defaultConfig.get();
    }

    default String configPath() {
        String elementName = getElement().getName();
        if (getElement() instanceof Element.SubElement sub) {
            elementName = sub.getParentElement().getName();
        }
        return "Abilities." + elementName + "." + getName() + ".";
    }

    default boolean has(String path) {
        return config().contains(configPath() + path);
    }

    default int getInt(String path) {
        return getInt(path, 0);
    }

    default int getInt(String path, int def) {
        if (config().isList(configPath() + path)) {
            List<Integer> list = config().getIntegerList(configPath() + path);
            if (list.size() != 2) {
                DashPack.instance.getLogger().warning("Expected a range of 2 integers at path: " + configPath() + path + ", but found " + list.size() + ". Using default: " + def);
                return def;
            }
            return RANDOM.nextInt(list.get(0), list.get(1) + 1);
        }
        return config().getInt(configPath() + path, def);
    }

    default long getLong(String path) {
        if (config().isList(configPath() + path)) {
            List<Long> list = config().getLongList(configPath() + path);
            if (list.size() != 2) {
                DashPack.instance.getLogger().warning("Expected a range of 2 longs at path: " + configPath() + path + ", but found " + list.size() + ". Using default: 0");
                return 0;
            }
            return RANDOM.nextLong(list.get(0), list.get(1) + 1);
        }
        return config().getLong(configPath() + path);
    }

    default double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    default double getDouble(String path, double def) {
        if (config().isList(configPath() + path)) {
            List<Double> list = config().getDoubleList(configPath() + path);
            if (list.size() != 2) {
                DashPack.instance.getLogger().warning("Expected a range of 2 doubles at path: " + configPath() + path + ", but found " + list.size() + ". Using default: " + def);
                return def;
            }
            return RANDOM.nextDouble(list.get(0), list.get(1));
        }
        return config().getDouble(configPath() + path, def);
    }

    default float getFloat(String path) {
        return getFloat(path, 0.0f);
    }

    default float getFloat(String path, float def) {
        if (config().isList(configPath() + path)) {
            List<Double> list = config().getDoubleList(configPath() + path);
            if (list.size() != 2) {
                DashPack.instance.getLogger().warning("Expected a range of 2 floats at path: " + configPath() + path + ", but found " + list.size() + ". Using default: " + def);
                return def;
            }
            return (float) RANDOM.nextDouble(list.get(0), list.get(1));
        }
        return (float) config().getDouble(configPath() + path, def);
    }

    default boolean getBoolean(String path) {
        return config().getBoolean(configPath() + path);
    }

    default String getString(String path) {
        return getString(path, null);
    }

    default String getString(String path, String def) {
        return config().getString(configPath() + path, def);
    }

    default List<String> getStringList(String path) {
        return config().getStringList(configPath() + path);
    }

    default Sound getSound(String path) {
        try {
            return Sound.sound(
                    Key.key(getString(path + ".Key")),
                    Sound.Source.valueOf(getString(path + ".Source", "PLAYER").toUpperCase()),
                    getFloat(path + ".Volume", 1.0F),
                    getFloat(path + ".Pitch", 1.0F)
            );
        } catch(Exception e) {
            DashPack.instance.getLogger().warning("Failed to load sound from path: " + configPath() + path);
            e.printStackTrace();
            return ERROR;
        }
    }

    default Sound getSound() {
        return getSound("Sound");
    }

    default long getBaseCooldown() {
        return getLong("Cooldown");
    }
}
