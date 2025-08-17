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
public class TicksOptionItem extends AbstractItem {
    private final String option;
    private final Function<PlayerSettings, Integer> getter;
    private final BiConsumer<PlayerSettings, Integer> setter;

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (!clickType.isMouseClick()) {
            return;
        }

        PlayerSettings settings = PlayerSettings.get(player);
        if (clickType == ClickType.MIDDLE) {
            setter.accept(settings, getter.apply(PlayerSettings.def()));
        } else {
            int currentValue = getter.apply(settings);
            int change = clickType.isShiftClick() ? 5 : 1;
            change *= clickType.isRightClick() ? -1 : 1;
            setter.accept(settings, Math.max(1, currentValue + change));
        }
        settings.save(player);
        notifyWindows();
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        return new ConfiguredOptionProvider(
                option,
                () -> String.valueOf(getter.apply(PlayerSettings.get(viewer))),
                () -> Objects.equals(getter.apply(PlayerSettings.get(viewer)), getter.apply(PlayerSettings.def()))
        );
    }
}
