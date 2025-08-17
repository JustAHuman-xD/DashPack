package me.justahuman.projectkorra.dashpack.settings.menu.item;

import lombok.RequiredArgsConstructor;
import me.justahuman.projectkorra.dashpack.settings.PlayerSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class BooleanOptionItem extends AbstractItem {
    private final String option;
    private final Function<PlayerSettings, Boolean> getter;
    private final BiConsumer<PlayerSettings, Boolean> setter;

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (!clickType.isLeftClick() && !clickType.isRightClick()) {
            return;
        }

        PlayerSettings settings = PlayerSettings.get(player);
        setter.accept(settings, !getter.apply(settings));
        settings.save(player);
        notifyWindows();
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        PlayerSettings settings = PlayerSettings.get(viewer);
        PlayerSettings defSettings = PlayerSettings.def();
        boolean current = getter.apply(settings);
        boolean isDefault = Objects.equals(current, getter.apply(defSettings));
        return new ConfiguredOptionProvider(option + (current ? "True" : "False"), String.valueOf(current), isDefault);
    }
}
