package me.justahuman.projectkorra.dashpack.settings.menu.item;

import com.projectkorra.projectkorra.util.ChatUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.justahuman.projectkorra.dashpack.DashPack;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ConfiguredOptionProvider implements ItemProvider {
    private final String path;
    private final Supplier<String> value;
    private final Supplier<Boolean> isDefault;

    public ConfiguredOptionProvider(String option, String value, boolean isDefault) {
        this(option, () -> value, () -> isDefault);
    }

    public ConfiguredOptionProvider(String option, Supplier<String> value, Supplier<Boolean> isDefault) {
        this.path = "SettingsMenu." + option;
        this.value = value;
        this.isDefault = isDefault;
    }

    private String format(String string) {
        return ChatUtil.color(string.replace("%value%", value.get()));
    }

    private String getString(String path) throws IllegalArgumentException {
        return getString(path, null);
    }

    private String getString(String path, String def) throws IllegalArgumentException {
        return getDirectString(this.path + "." + path, def);
    }

    private String getDirectString(String path, String def) throws IllegalArgumentException {
        try {
            return format(DashPack.instance.getConfig().getString(path, def));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get string from config at path: " + this.path + "." + path, e);
        }
    }

    private List<String> getStringList(String path) throws IllegalArgumentException {
        try {
            return DashPack.instance.getConfig().getStringList(this.path + "." + path).stream().map(this::format).toList();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get string list from config at path: " + this.path + "." + path, e);
        }
    }

    @Override
    public @NotNull ItemStack get(@Nullable String lang) {
        try {
            FileConfiguration config = DashPack.instance.getConfig();
            ItemStack itemStack = Registry.ITEM.get(Key.key(getString("Material"))).createItemStack();
            itemStack.editMeta(meta -> {
                meta.setItemModel(NamespacedKey.fromString(getString("Model", itemStack.getType().getDefaultData(DataComponentTypes.ITEM_MODEL).toString())));
                if (config.contains(path + ".ModelData")) {
                    meta.setCustomModelData(config.getInt(path + ".ModelData"));
                }
                meta.setItemName(getString("Name") + (isDefault.get() ? getDirectString("SettingsMenu._All.DefaultNameSuffix", null) : ""));
                meta.setLore(getStringList("Lore"));
            });
            return itemStack;
        } catch (Exception e) {
            DashPack.instance.getLogger().warning("Failed to load item from config at path: " + path);
            e.printStackTrace();
            return ItemStack.empty();
        }
    }
}
