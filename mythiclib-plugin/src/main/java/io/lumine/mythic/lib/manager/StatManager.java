package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.handler.AttributeStatHandler;
import io.lumine.mythic.lib.api.stat.handler.DelegateStatHandler;
import io.lumine.mythic.lib.api.stat.handler.MovementSpeedStatHandler;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.util.ConfigFile;
import io.lumine.mythic.lib.version.VMaterial;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public class StatManager {
    private final Map<String, StatHandler> handlers = new HashMap<>();

    public void initialize(boolean clearBefore) {
        if (clearBefore) handlers.clear();
        else UtilityMethods.loadDefaultFile("", "stats.yml");

        final ConfigurationSection statsConfig = new ConfigFile("stats").getConfig();

        // Default stat handlers
        try {
            registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_ARMOR, SharedStat.ARMOR, Material.IRON_CHESTPLATE, "Armor bonus of an Entity."));
            registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_ARMOR_TOUGHNESS, SharedStat.ARMOR_TOUGHNESS, Material.GOLDEN_CHESTPLATE, "Armor toughness bonus of an Entity."));
            registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_ATTACK_DAMAGE, SharedStat.ATTACK_DAMAGE, Material.IRON_SWORD, "Attack damage of an Entity."));
            registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_ATTACK_SPEED, SharedStat.ATTACK_SPEED, Material.LIGHT_GRAY_DYE, "Attack speed of an Entity."));
            registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_KNOCKBACK_RESISTANCE, SharedStat.KNOCKBACK_RESISTANCE, Material.TNT_MINECART, "Resistance of an Entity to knockback."));
            registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_LUCK, SharedStat.LUCK, VMaterial.GRASS_BLOCK.get(), "Luck bonus of an Entity."));
            registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_MAX_HEALTH, SharedStat.MAX_HEALTH, Material.APPLE, "Maximum health of an Entity."));
            final StatHandler msStatHandler = new MovementSpeedStatHandler(statsConfig);
            registerStat(msStatHandler);
            registerStat(new DelegateStatHandler(statsConfig, SharedStat.SPEED_MALUS_REDUCTION, msStatHandler));

            // 1.20.2
            if (MythicLib.plugin.getVersion().isAbove(1, 20, 2))
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_MAX_ABSORPTION, SharedStat.MAX_ABSORPTION, Material.GOLDEN_APPLE, "Max amount of absorption hearts."));

            // 1.20.5
            if (MythicLib.plugin.getVersion().isAbove(1, 20, 5)) {
                registerStat(new AttributeStatHandler(statsConfig, Attribute.PLAYER_BLOCK_BREAK_SPEED, SharedStat.BLOCK_BREAK_SPEED, Material.IRON_PICKAXE, "Speed of breaking blocks."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.PLAYER_BLOCK_INTERACTION_RANGE, SharedStat.BLOCK_INTERACTION_RANGE, Material.SPYGLASS, "How far players may break or interact with blocks."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.PLAYER_ENTITY_INTERACTION_RANGE, SharedStat.ENTITY_INTERACTION_RANGE, Material.SPYGLASS, "How far players may hit or interact with entities."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER, SharedStat.FALL_DAMAGE_MULTIPLIER, Material.GOLDEN_APPLE, "Max amount of absorption hearts."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_GRAVITY, SharedStat.GRAVITY, Material.STONE, "How strong gravity is."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_JUMP_STRENGTH, SharedStat.JUMP_STRENGTH, Material.FEATHER, "How high you can jump."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_SAFE_FALL_DISTANCE, SharedStat.SAFE_FALL_DISTANCE, Material.RED_BED, "How high you can drop from without fall damage."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_SCALE, SharedStat.SCALE, Material.GUARDIAN_SPAWN_EGG, "Size of an entity."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_STEP_HEIGHT, SharedStat.STEP_HEIGHT, Material.OAK_SLAB, "How high you can climb blocks when walking."));
            }

            // 1.21
            if (MythicLib.plugin.getVersion().isAbove(1, 21)) {
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_BURNING_TIME, SharedStat.BURNING_TIME, Material.COOKED_BEEF, "A factor for increasing/reducing mining speed"));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE, SharedStat.EXPLOSION_KNOCKBACK_RESISTANCE, Material.OBSIDIAN, "Resistance to knockback due to explosions."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.PLAYER_MINING_EFFICIENCY, SharedStat.MINING_EFFICIENCY, Material.IRON_PICKAXE, "A factor for increasing/reducing mining speed"));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_MOVEMENT_EFFICIENCY, SharedStat.MOVEMENT_EFFICIENCY, Material.SOUL_SAND, "Movement speed factor when walking on blocks that slow down movement."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_OXYGEN_BONUS, SharedStat.OXYGEN_BONUS, Material.GLASS_BOTTLE, "Determines the chance not to use up air when underwater."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.PLAYER_SNEAKING_SPEED, SharedStat.SNEAKING_SPEED, Material.LEATHER_BOOTS, "Movement speed when sneaking."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.PLAYER_SUBMERGED_MINING_SPEED, SharedStat.SUBMERGED_MINING_SPEED, Material.IRON_PICKAXE, "Mining speed factor when submerged."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.PLAYER_SWEEPING_DAMAGE_RATIO, SharedStat.SWEEPING_DAMAGE_RATIO, Material.IRON_SWORD, "Damage ratio when performing sweep melee attacks."));
                registerStat(new AttributeStatHandler(statsConfig, Attribute.GENERIC_WATER_MOVEMENT_EFFICIENCY, SharedStat.WATER_MOVEMENT_EFFICIENCY, Material.WATER_BUCKET, "Movement speed factor when submerged."));
            }

        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load default stat handlers:");
            exception.printStackTrace();
        }

        // Load stat handlers
        for (String key : collectReferencedStats(statsConfig))
            try {
                final String stat = UtilityMethods.enumName(key);
                handlers.putIfAbsent(stat, new StatHandler(statsConfig, stat));
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not load stat handler '" + key + "': " + exception.getMessage());
            }
    }

    @NotNull
    private Iterable<String> collectReferencedStats(ConfigurationSection config) {
        final List<String> keys = new ArrayList<>();
        for (String key : config.getKeys(false))
            keys.addAll(config.getConfigurationSection(key).getKeys(false));
        return keys;
    }

    @NotNull
    public static String format(String stat, MMOPlayerData player) {
        return player.getStatMap().getInstance(stat).formatFinal();
    }

    @NotNull
    public static String format(String stat, double value) {
        @Nullable final StatHandler handler = MythicLib.plugin.getStats().handlers.get(stat);
        return (handler == null ? MythicLib.plugin.getMMOConfig().decimal : handler.getDecimalFormat()).format(value);
    }

    /**
     * Lets the MMO- plugins knows that a specific stat needs an update
     * whenever the value of the player stat changes (due to a MythicLib
     * stat modifier being added/being removed/expiring).
     *
     * @param handler Behaviour of given stat
     */
    public void registerStat(@NotNull StatHandler handler, String... aliases) {
        Validate.notNull(handler, "StatHandler cannot be null");

        handlers.put(handler.getStat(), handler);
        for (String alias : aliases)
            handlers.put(alias, handler);
    }

    @NotNull
    public Optional<StatHandler> getHandler(String stat) {
        return Optional.ofNullable(handlers.get(stat));
    }

    public boolean isRegistered(String stat) {
        return handlers.containsKey(stat);
    }

    @NotNull
    public Set<String> getRegisteredStats() {
        return handlers.keySet();
    }

    @NotNull
    public Collection<StatHandler> getHandlers() {
        return handlers.values();
    }

    public void clearRegisteredStats(Predicate<StatHandler> filter) {
        final Iterator<StatHandler> ite = handlers.values().iterator();
        while (ite.hasNext()) if (filter.test(ite.next())) ite.remove();
    }

    @Deprecated
    public void runUpdate(StatMap map, String stat) {
        map.getInstance(stat).update();
    }

    @Deprecated
    public void runUpdates(@NotNull StatMap map) {
        for (StatInstance ins : map.getInstances()) ins.update();
    }

    @Deprecated
    public void runUpdate(@NotNull StatInstance instance) {
        instance.update();
    }

    @Deprecated
    public double getBaseValue(String stat, StatMap map) {
        @Nullable final StatHandler handler = handlers.get(stat);
        return handler == null ? 0 : handler.getBaseValue(map.getInstance(stat));
    }

    @Deprecated
    public double getBaseValue(StatInstance instance) {
        return instance.getBase();
    }

    @Deprecated
    public double getTotalValue(String stat, StatMap map) {
        return map.getStat(stat);
    }

    @Deprecated
    public double getTotalValue(StatInstance instance) {
        return instance.getTotal();
    }

    @Deprecated
    public void registerStat(String stat, StatHandler handler) {
        Validate.notNull(stat, "Stat cannot be null");
        Validate.notNull(handler, "StatHandler cannot be null");

        handlers.put(stat, handler);
    }
}
