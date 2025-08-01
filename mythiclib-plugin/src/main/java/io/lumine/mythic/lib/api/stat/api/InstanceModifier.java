package io.lumine.mythic.lib.api.stat.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.Pair;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Used anywhere where instances similar to Bukkit's attribute
 * instances are being modified by numerical modifiers.
 * <p>
 * Examples:
 * - MythicLib stat modifiers
 * - MythicLib skill modifiers
 * - MMOCore attribute modifiers
 */
public abstract class InstanceModifier extends PlayerModifier {
    protected final double value;
    protected final ModifierType type;

    public InstanceModifier(@NotNull String key, double value) {
        this(key, EquipmentSlot.OTHER, ModifierSource.OTHER, value, ModifierType.FLAT);
    }

    public InstanceModifier(@NotNull String key, EquipmentSlot slot, ModifierSource source, double value, ModifierType type) {
        this(UUID.randomUUID(), key, slot, source, value, type);
    }

    public InstanceModifier(@NotNull UUID uniqueId, @NotNull String key, EquipmentSlot slot, ModifierSource source, double value, ModifierType type) {
        super(uniqueId, key, slot, source);

        this.value = value;
        this.type = type;
    }

    public InstanceModifier(String key, EquipmentSlot slot, ModifierSource source, String str) {
        super(key, slot, source);

        Validate.notNull(str, "String cannot be null");
        Validate.notEmpty(str, "String cannot be empty");

        Pair<ModifierType, Double> readValueInfo = ModifierType.pairFromString(str);
        type = readValueInfo.getLeft();
        value = readValueInfo.getRight();
    }

    public InstanceModifier(ConfigObject object) {
        super(object.getString("key"), EquipmentSlot.OTHER, ModifierSource.OTHER);

        value = object.getDouble("value");
        type = object.getBoolean("multiplicative", false) ? ModifierType.RELATIVE
                : object.getBoolean("scalar") ? ModifierType.ADDITIVE_MULTIPLIER
                : ModifierType.FLAT;
    }

    @NotNull
    public ModifierType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return MythicLib.plugin.getMMOConfig().decimal.format(value) + type.toStringSuffix();
    }
}
