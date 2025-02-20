package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.handler.MythicLibSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An inventory item that has no particular placeholder
 * yet it DOES support PAPI placeholders.
 */
public class SimpleItem<T extends GeneratedInventory> extends PhysicalItem<T> {
    private final Script script;

    public SimpleItem(ConfigurationSection config) {
        super(config);

        script = config.contains("on-click") ? MythicLib.plugin.getSkills().loadScript(config, "on-click") : null;
    }

    @Override
    public @NotNull Placeholders getPlaceholders(T inv, int n) {
        return new Placeholders();
    }

    @Override
    public void onClick(@NotNull T inv, @NotNull InventoryClickEvent event) {
        if (script != null) {
            SkillHandler<?> handler = new MythicLibSkillHandler(script);
            SimpleSkill skill = new SimpleSkill(handler);
            skill.cast(inv.getMMOPlayerData());
        }
    }
}
