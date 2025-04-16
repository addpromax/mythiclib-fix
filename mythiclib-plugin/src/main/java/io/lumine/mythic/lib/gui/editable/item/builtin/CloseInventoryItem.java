package io.lumine.mythic.lib.gui.editable.item.builtin;

import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class CloseInventoryItem<T extends GeneratedInventory> extends PhysicalItem<T> {
    public CloseInventoryItem(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull Placeholders getPlaceholders(T inv, int n) {
        return new Placeholders();
    }

    @Override
    public void onClick(@NotNull T inv, @NotNull InventoryClickEvent event) {
        inv.getPlayer().closeInventory();
    }
}
