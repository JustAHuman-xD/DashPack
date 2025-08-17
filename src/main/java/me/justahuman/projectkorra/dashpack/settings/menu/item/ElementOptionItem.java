package me.justahuman.projectkorra.dashpack.settings.menu.item;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import me.justahuman.projectkorra.dashpack.settings.PlayerSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.ArrayList;
import java.util.List;

public class ElementOptionItem extends AbstractItem {
    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (!clickType.isMouseClick()) {
            return;
        }

        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer == null) {
            return;
        }

        List<String> elements = new ArrayList<>(List.of("First"));
        for (Element element : Element.getAllElements()) {
            if (DashAbility.get(element) != null && bPlayer.hasElement(element)) {
                elements.add(element.getName());
            }
        }
        if (elements.size() == 1) {
            return;
        } else if (elements.size() == 2) {
            elements.remove(1);
        }

        PlayerSettings settings = PlayerSettings.get(player);
        String current = settings.element();
        int currentIndex = elements.indexOf(current == null ? "First" : settings.element(bPlayer).getName());
        if (currentIndex == -1) {
            currentIndex = 0;
        }

        int nextIndex = (currentIndex + (clickType.isRightClick() ? -1 : 1)) % elements.size();
        String nextElementName = elements.get(nextIndex);
        settings.setElement(nextElementName.equals("First") ? null : nextElementName);
        settings.save(player);
        notifyWindows();
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        return new ConfiguredOptionProvider(
                "Element",
                () -> {
                    BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(viewer);
                    if (bPlayer == null) {
                        return "None";
                    }

                    PlayerSettings settings = PlayerSettings.get(viewer);
                    String elementName = settings.element();
                    Element element = settings.element(bPlayer);
                    if (element == null) {
                        return "None";
                    }

                    return elementName == null ? "First &7(" + element.getColor() + element.getName() + "&7)" : element.getColor() + element.getName();
                },
                () -> PlayerSettings.get(viewer).element() == null
        );
    }
}
