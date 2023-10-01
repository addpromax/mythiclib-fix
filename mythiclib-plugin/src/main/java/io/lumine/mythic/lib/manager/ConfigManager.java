package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.interaction.relation.EmptyPvPInteractionRules;
import io.lumine.mythic.lib.comp.interaction.relation.InteractionRules;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.handler.MythicLibSkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ConfigManager {
    public final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();

    public DecimalFormat decimal, decimals;
    public boolean playerAbilityDamage, castingDelayCancelOnMove, enableCastingDelayBossbar, fixTooLargePackets, debugMode, ignoreShiftTriggers, ignoreOffhandClickTriggers;
    public String naturalDefenseFormula, elementalDefenseFormula, castingDelayBossbarFormat;
    public double castingDelaySlowness;
    public int maxSyncTries;

    @NotNull
    public InteractionRules interactionRules;

    @Nullable
    public Skill skillCastScript, skillCancelScript;

    public void reload() {
        final ConfigurationSection config = MythicLib.plugin.getConfig();

        // Decimal formatting
        formatSymbols.setDecimalSeparator(getFirstChar(config.getString("number-format.decimal-separator")));
        decimal = newDecimalFormat("0.#");
        decimals = newDecimalFormat("0.##");

        // Combat
        interactionRules = config.getBoolean("interaction_rules.enabled") ? new InteractionRules(config.getConfigurationSection("interaction_rules")) : new EmptyPvPInteractionRules();

        // Other options
        playerAbilityDamage = config.getBoolean("player-ability-damage");
        naturalDefenseFormula = config.getString("defense-application.natural");
        elementalDefenseFormula = config.getString("defense-application.elemental");
        fixTooLargePackets = config.getBoolean("fix-too-large-packets");
        debugMode = config.getBoolean("debug");
        maxSyncTries = config.getInt("max-sync-tries");
        ignoreShiftTriggers = config.getBoolean("ignore_shift_triggers");
        ignoreOffhandClickTriggers = config.getBoolean("ignore_offhand_click_triggers");

        // Casting delay
        castingDelaySlowness = config.getDouble("casting-delay.slowness");
        castingDelayCancelOnMove = config.getBoolean("casting-delay.cancel-on-move");
        enableCastingDelayBossbar = config.getBoolean("casting-delay.bossbar.enabled");
        castingDelayBossbarFormat = config.getString("casting-delay.bossbar.format");
        try {
            skillCastScript = config.getBoolean("casting-delay.cast-script.enabled") ?
                    new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(MythicLib.plugin.getSkills().loadScript(config.get("casting-delay.cast-script.script")))) : null;
        } catch (IllegalArgumentException exception) {
            skillCastScript = null;
        }
        try {
            skillCancelScript = config.getBoolean("casting-delay.cancel-script.enabled") ?
                    new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(MythicLib.plugin.getSkills().loadScript(config.get("casting-delay.cancel-script.script")))) : null;
        } catch (IllegalArgumentException exception) {
            skillCancelScript = null;
        }
    }

    /**
     * MMOCore and MMOItems mostly cache the return value of that method
     * in static fields for easy access, therefore a server restart is
     * required when editing the decimal-separator option in the ML config
     *
     * @param pattern Something like "0.#"
     * @return New decimal format with the decimal separator given in the MythicLib
     *         main plugin config.
     */
    public DecimalFormat newDecimalFormat(String pattern) {
        return new DecimalFormat(pattern, formatSymbols);
    }

    private char getFirstChar(String str) {
        return str == null || str.isEmpty() ? '.' : str.charAt(0);
    }
}
