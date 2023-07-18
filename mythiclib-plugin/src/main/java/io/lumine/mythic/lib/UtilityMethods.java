package io.lumine.mythic.lib;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.condition.RegionCondition;
import io.lumine.mythic.lib.api.condition.type.MMOCondition;
import io.lumine.mythic.lib.api.event.DamageCheckEvent;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class UtilityMethods {
    /**
     * NOT FINAL CODE.
     * THIS WILL BE MASSIVELY REWORKED VERY SOON!
     */
    public static MMOCondition getCondition(String input) {
        MMOLineConfig config = new MMOLineConfig(input);
        String key = config.getKey().toLowerCase();
        switch (key) {
            case "region":
                if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) return null;
                return new RegionCondition(config);
        }

        return null;
    }

    /**
     * Fake events are used very commonly in MythicLib and other plugins.
     * This is used to pre-check if a player is able to fight with/target
     * another player in anticipation i.e before casting the damage method.
     *
     * @param event Some damage event
     * @return If the event is a fake event, used for an interaction check
     */
    public static boolean isFakeEvent(@NotNull EntityDamageEvent event) {
        return event.getDamage() == 0 || event instanceof DamageCheckEvent;
    }

    @NotNull
    public static Runnable serverThreadCatch(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                Bukkit.getScheduler().runTask(plugin, () -> throwable.printStackTrace());
            }
        };
    }

    public static boolean isAir(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private final static char[] DELAY_CHARACTERS = {'m', 'h', 'd', 'm', 'y'};
    private final static long[] DELAY_AMOUNTS = {60, 60 * 60, 60 * 60 * 24, 60 * 60 * 24 * 30, 60 * 60 * 24 * 30 * 365};

    public static String formatDelay(long millis) {

        if (millis < 1000 * 60)
            return "1m";

        String format = "";
        for (int j = DELAY_CHARACTERS.length - 1; j >= 0; j--) {
            long divisor = DELAY_AMOUNTS[j] * 1000;
            if (millis < divisor)
                continue;

            format = (millis / divisor) + DELAY_CHARACTERS[j] + " " + format;
            millis = millis % divisor;
        }

        return format;
    }

    private static final int PTS_PER_BLOCK = 10;

    public static void drawVector(Vector vec, Location source, Color color) {

        final double step = 1d / ((double) PTS_PER_BLOCK) / vec.length();
        for (double d = 0; d < 1; d += step) {
            Location inter = source.clone().add(vec.clone().multiply(d));
            inter.getWorld().spawnParticle(Particle.REDSTONE, inter, 0, new Particle.DustOptions(color, .6f));
        }
    }

    /**
     * @param loc Where we are looking for nearby entities
     * @return List of all entities surrounding a location. This method loops
     *         through the 9 surrounding chunks and collect all entities from
     *         them. This list can be cached and used multiple times in the same
     *         tick for projectile based spells which need to run entity
     *         checkups
     */
    public static List<Entity> getNearbyChunkEntities(Location loc) {
        List<Entity> entities = new ArrayList<>();

        int cx = loc.getChunk().getX();
        int cz = loc.getChunk().getZ();
        // d

        for (int x = -1; x < 2; x++)
            for (int z = -1; z < 2; z++)
                entities.addAll(Arrays.asList(loc.getWorld().getChunkAt(cx + x, cz + z).getEntities()));

        return entities;
    }

    /**
     * Interaction type is set to OFFENSE_SKILL by default. No bounding box checks
     *
     * @param source Player targeting the entity
     * @param target The entity being hit
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(Player source, Entity target) {
        return canTarget(source, null, target, InteractionType.OFFENSE_SKILL);
    }

    /**
     * Interaction type is set to OFFENSE_SKILL by default.
     *
     * @param source Player targeting the entity
     * @param loc    If the given location is not null, this method checks if this
     *               location is inside the bounding box of the entity hit
     * @param target The entity being hit
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(Player source, Location loc, Entity target) {
        return canTarget(source, loc, target, InteractionType.OFFENSE_SKILL);
    }

    /**
     * No bounding box checks
     *
     * @param source      Player targeting the entity
     * @param target      The entity being hit
     * @param interaction Type of interaction
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(Player source, Entity target, InteractionType interaction) {
        return canTarget(source, null, target, interaction);
    }

    private static final double BOUNDING_BOX_EXPANSION = .2;

    /**
     * @param source      Player targeting the entity
     * @param loc         If the given location is not null, this method checks if this
     *                    location is inside the bounding box of the entity hit
     * @param target      The entity being hit
     * @param interaction Type of interaction
     * @return If the entity can be damaged, by a specific player, at a specific spot
     */
    public static boolean canTarget(@Nullable Player source, @Nullable Location loc, @NotNull Entity target, @NotNull InteractionType interaction) {

        // Check for bounding box
        // Small computations first
        if (loc != null && !target.getBoundingBox().expand(BOUNDING_BOX_EXPANSION).contains(loc.toVector()))
            return false;

        // Interaction type check
        if (!MythicLib.plugin.getEntities().canInteract(source, target, interaction)) return false;

        return true;
    }

    public static boolean isRealPlayer(Entity entity) {
        return entity instanceof Player && !entity.hasMetadata("NPC");
    }

    public static boolean isMetaItem(@Nullable ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName();
    }

    /**
     * @param player Player to heal
     * @param heal   Heal amount
     *               <br>
     *               Negative values are just ignored
     */
    public static void heal(@NotNull LivingEntity player, double heal) {
        heal(player, heal, false);
    }

    /**
     * @param player         Player to heal
     * @param heal           Heal amount
     * @param allowNegatives If passing a negative health value will damage the entity x)
     *                       <br>
     *                       If <code>false</code>, negative values are just ignored
     */
    public static void heal(@NotNull LivingEntity player, double heal, boolean allowNegatives) {
        if (heal > 0 || allowNegatives)
            player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + heal));
    }

    private static final Random RANDOM = new Random();

    /**
     * Super useful to display enum names like DIAMOND_SWORD in chat
     *
     * @param input String with lower cases and spaces only
     * @return Same string with capital letters at the beginning of each word.
     */
    public static String caseOnWords(String input) {
        StringBuilder builder = new StringBuilder(input);
        boolean isLastSpace = true;
        for (int i = 0; i < builder.length(); i++) {
            char ch = builder.charAt(i);
            if (isLastSpace && ch >= 'a' && ch <= 'z') {
                builder.setCharAt(i, (char) (ch + ('A' - 'a')));
                isLastSpace = false;
            } else isLastSpace = ch == ' ';
        }
        return builder.toString();
    }

    public static void dropItemNaturally(Location loc, ItemStack stack) {
        double dx = ((RANDOM.nextFloat() * 0.5F) + 0.25D) / 10;
        double dy = ((RANDOM.nextFloat() * 0.5F) + 0.25D) / 10;
        double dz = ((RANDOM.nextFloat() * 0.5F) + 0.25D) / 10;
        loc.getWorld().dropItem(loc.add(0.5, 0.5, 0.5), stack).setVelocity(new Vector(dx, dy, dz));
    }

    /**
     * Used to find players in chunks around some location. This is
     * used when displaying individual holograms to a list of players.
     *
     * @param loc Target location
     * @return Players in chunks around the location
     */
    @Deprecated
    public static List<Player> getNearbyPlayers(Location loc) {
        final List<Player> players = new ArrayList<>();

        final int cx = loc.getChunk().getX(), cz = loc.getChunk().getZ();

        for (int x = -1; x < 2; x++)
            for (int z = -1; z < 2; z++)
                for (Entity target : loc.getWorld().getChunkAt(cx + x, cz + z).getEntities())
                    if (target instanceof Player) players.add((Player) target);

        return players;
    }

    public static void loadDefaultFile(String path, String name) {
        final String newPath = path.isEmpty() ? "" : "/" + path;
        final File folder = new File(MythicLib.plugin.getDataFolder() + newPath);
        if (!folder.exists()) folder.mkdir();

        final File file = new File(MythicLib.plugin.getDataFolder() + newPath, name);
        if (!file.exists()) try {
            Files.copy(MythicLib.plugin.getResource("default/" + (path.isEmpty() ? "" : path + "/") + name), file.getAbsoluteFile().toPath());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * The 'vanished' meta data should be supported by vanish plugins
     * to let all the plugins knows when a player is vanished.
     *
     * @return If a given player is vanished or not
     */
    public static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished"))
            if (meta.asBoolean()) return true;
        return false;
    }

    /**
     * @return Upper case string, with spaces and - replaced by _
     */
    public static String enumName(String str) {
        return str.toUpperCase().replace("-", "_").replace(" ", "_");
    }

    public static String ymlName(String str) {
        return str.toLowerCase().replace("_", "-").replace(" ", "-");
    }

    public static double[] getYawPitch(Vector axis) {
        double _2PI = 6.283185307179586D;
        double x = axis.getX();
        double z = axis.getZ();

        if (x == 0 && z == 0) return new double[]{0, axis.getY() > 0 ? -90 : 90};
        else {
            double theta = Math.atan2(-x, z);
            double yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);
            double xz = Math.sqrt(x * x + z * z);
            double pitch = (float) Math.toDegrees(Math.atan(-axis.getY() / xz));
            return new double[]{yaw, pitch};
        }
    }

    /**
     * Useful when dealing with Pvp stuff. If a VANILLA attack is due to
     * a player, this method will return the damage source ie the player.
     *
     * @param event Some damage event
     * @return The player, if this event is due to him. It is the player which
     *         is taken into account when PvP is toggled on.
     */
    @Nullable
    public static Player getPlayerDamager(EntityDamageByEntityEvent event) {
        if (isRealPlayer(event.getDamager())) return (Player) event.getDamager();

        if (event.getDamager() instanceof Projectile) {
            final ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter instanceof Entity && isRealPlayer((Entity) shooter)) return (Player) shooter;
        }

        return null;
    }

    public static Vector rotate(Vector rotated, Vector axis) {
        double[] pitchYaw = getYawPitch(axis);
        return rotate(rotated, Math.toRadians(pitchYaw[0]), Math.toRadians(pitchYaw[1]));
    }

    public static Vector rotate(Vector rotated, double yaw, double pitch) {
        return rotAxisY(rotAxisX(rotated, pitch), -yaw);
    }

    private static Vector rotAxisX(Vector rotated, double angle) {
        double y = rotated.getY() * Math.cos(angle) - rotated.getZ() * Math.sin(angle);
        double z = rotated.getY() * Math.sin(angle) + rotated.getZ() * Math.cos(angle);
        return rotated.setY(y).setZ(z);
    }

    private static Vector rotAxisY(Vector rotated, double angle) {
        double x = rotated.getX() * Math.cos(angle) + rotated.getZ() * Math.sin(angle);
        double z = rotated.getX() * -Math.sin(angle) + rotated.getZ() * Math.cos(angle);
        return rotated.setX(x).setZ(z);
    }

    public static double getAltitude(Entity entity) {
        return getAltitude(entity.getLocation());
    }

    public static double getAltitude(Location loc) {
        final Location moving = loc.clone();
        while (!moving.getBlock().getType().isSolid()) moving.add(0, -1, 0);

        return loc.getY() - moving.getBlockY() - 1;
    }

    private static final Map<String, String> DEBUG_COLOR_PREFIX = new HashMap<>();

    static {
        DEBUG_COLOR_PREFIX.put("MythicLib", "§a");
        DEBUG_COLOR_PREFIX.put("MMOItems", "§c");
        DEBUG_COLOR_PREFIX.put("MMOCore", "§6");
        DEBUG_COLOR_PREFIX.put("RPGInventory", "§e");
    }

    /**
     * Sends a debug message. All plugins depending on MythicLib must use this
     * function to send debug message, which is more convenient for users.
     * MMOInventory has its own option, because it's standalone.
     *
     * @param plugin  Plugin that needs debug
     * @param prefix  What's being debugged
     * @param message Debug message
     */
    public static void debug(@NotNull JavaPlugin plugin, @Nullable String prefix, @NotNull String message) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.notNull(message, "Message cannot be null");

        final String colorPrefix = DEBUG_COLOR_PREFIX.getOrDefault(plugin.getName(), "");

        if (MythicLib.plugin.getMMOConfig().debugMode)
            plugin.getLogger().log(Level.INFO, colorPrefix + "[Debug" + (prefix == null ? "" : ": " + prefix) + "] " + message);
    }
}
