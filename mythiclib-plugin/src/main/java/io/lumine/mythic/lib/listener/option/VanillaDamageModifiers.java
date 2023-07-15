package io.lumine.mythic.lib.listener.option;

import bsh.EvalError;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class VanillaDamageModifiers implements Listener {
    private final Map<EntityDamageEvent.DamageCause, String> formulas = new HashMap<>();

    public VanillaDamageModifiers(@NotNull ConfigurationSection config) {
        config = config.getConfigurationSection("source");
        if (config == null) return;

        for (String key : config.getKeys(false))
            try {
                EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.valueOf(UtilityMethods.enumName(key));
                formulas.put(cause, config.getString(key));
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load vanilla damage modifier '" + key + "': " + exception.getMessage());
            }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        final Player player = (Player) event.getEntity();

        @Nullable String formula = formulas.get(event.getCause());
        if (formula == null) return;

        try {
            formula = applyInternalPlaceholders(formula, event);
            final double evaluated = (double) MythicLib.plugin.getFormulaParser().eval(player, formula);
            event.setDamage(evaluated);
        } catch (EvalError exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not evaluate formula '" + formula + "' for player '" + player.getUniqueId() + "': " + exception.getMessage());
        }
    }

    @NotNull
    private String applyInternalPlaceholders(@NotNull String str, @NotNull EntityDamageEvent event) {
        str = str.replace("{event_damage}", String.valueOf(event.getDamage()));
        return str;
    }
}
