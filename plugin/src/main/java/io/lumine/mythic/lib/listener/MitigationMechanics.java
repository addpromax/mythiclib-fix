package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.mitigation.PlayerBlockEvent;
import io.lumine.mythic.lib.api.event.mitigation.PlayerDodgeEvent;
import io.lumine.mythic.lib.api.event.mitigation.PlayerParryEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.player.cooldown.CooldownType;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MitigationMechanics implements Listener {
    private static final Random RANDOM = new Random();
    private static final List<EntityDamageEvent.DamageCause> MITIGATION_CAUSES = Arrays.asList(EntityDamageEvent.DamageCause.PROJECTILE, EntityDamageEvent.DamageCause.ENTITY_ATTACK, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK);

    // Mitigation configs
    private boolean actionBarMessage;
    private double dodgeKnockback, parryKnockback, parryDefaultCooldown, blockDefaultCooldown, dodgeDefaultCooldown;

    // Mitigation chat messages
    private String parryMessage, blockMessage, dodgeMessage;

    public MitigationMechanics() {
        reload();
    }

    public void reload() {
        dodgeKnockback = MythicLib.plugin.getConfig().getDouble("mitigation.dodge.knockback");
        parryKnockback = MythicLib.plugin.getConfig().getDouble("mitigation.parry.knockback");

        parryDefaultCooldown = MythicLib.plugin.getConfig().getDouble("mitigation.parry.cooldown");
        blockDefaultCooldown = MythicLib.plugin.getConfig().getDouble("mitigation.block.cooldown");
        dodgeDefaultCooldown = MythicLib.plugin.getConfig().getDouble("mitigation.dodge.cooldown");

        parryMessage = MythicLib.plugin.getConfig().getString("mitigation.message.parry");
        dodgeMessage = MythicLib.plugin.getConfig().getString("mitigation.message.dodge");
        blockMessage = MythicLib.plugin.getConfig().getString("mitigation.message.block");
        actionBarMessage = MythicLib.plugin.getConfig().getBoolean("mitigation.message.action-bar");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void a(AttackEvent event) {
        if (!(event.getEntity() instanceof Player) || !MITIGATION_CAUSES.contains(event.toBukkit().getCause()) || event.getEntity().hasMetadata("NPC"))
            return;

        Player player = (Player) event.getEntity();
        MMOPlayerData playerData = MMOPlayerData.get(player);
        StatMap stats = playerData.getStatMap();

        // Dodging
        double dodgeRating = stats.getStat("DODGE_RATING") / 100;
        if (RANDOM.nextDouble() < dodgeRating && !playerData.isOnCooldown(CooldownType.DODGE)) {

            PlayerDodgeEvent mitigationEvent = new PlayerDodgeEvent(playerData, event.toBukkit());
            Bukkit.getPluginManager().callEvent(mitigationEvent);
            if (mitigationEvent.isCancelled())
                return;

            sendMessage(player, dodgeMessage, "damage", MythicLib.plugin.getMMOConfig().decimal.format(event.getDamage().getDamage()));
            playerData.applyCooldown(CooldownType.DODGE, calculateCooldown(dodgeDefaultCooldown, stats.getStat("DODGE_COOLDOWN_REDUCTION")));
            event.setCancelled(true);
            player.setNoDamageTicks(10);
            player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
            player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 16, 0, 0, 0, .06);
            if (dodgeKnockback > 0)
                player.setVelocity(getVector(player, event).multiply(-.85 * dodgeKnockback).setY(.3));
            return;
        }

        // Parrying
        double parryRating = stats.getStat("PARRY_RATING") / 100;
        if (RANDOM.nextDouble() < parryRating && !playerData.isOnCooldown(CooldownType.PARRY)) {

            PlayerParryEvent mitigationEvent = new PlayerParryEvent(playerData, event.toBukkit());
            Bukkit.getPluginManager().callEvent(mitigationEvent);
            if (mitigationEvent.isCancelled())
                return;

            playerData.applyCooldown(CooldownType.PARRY, calculateCooldown(parryDefaultCooldown, stats.getStat("PARRY_COOLDOWN_REDUCTION")));
            event.setCancelled(true);
            player.setNoDamageTicks(10);
            sendMessage(player, parryMessage, "damage", MythicLib.plugin.getMMOConfig().decimal.format(event.getDamage().getDamage()));
            player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
            player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 16, 0, 0, 0, .06);
            if (parryKnockback > 0 && event.toBukkit() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event.toBukkit()).getDamager() instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity) ((EntityDamageByEntityEvent) event.toBukkit()).getDamager();
                attacker.setVelocity(normalize(attacker.getLocation().toVector().subtract(player.getLocation().toVector())).setY(.35).multiply(parryKnockback));
            }
            return;
        }

        // Blocking
        double blockRating = stats.getStat("BLOCK_RATING") / 100;
        if (RANDOM.nextDouble() < blockRating && !playerData.isOnCooldown(CooldownType.BLOCK)) {

            double blockPower = stats.getStat("BLOCK_POWER") / 100;
            PlayerBlockEvent mitigationEvent = new PlayerBlockEvent(playerData, event.toBukkit(), blockPower);
            Bukkit.getPluginManager().callEvent(mitigationEvent);
            if (mitigationEvent.isCancelled())
                return;

            playerData.applyCooldown(CooldownType.BLOCK, calculateCooldown(blockDefaultCooldown, stats.getStat("BLOCK_COOLDOWN_REDUCTION")));
            sendMessage(player, blockMessage,
                    "damage", MythicLib.plugin.getMMOConfig().decimal.format(mitigationEvent.getDamageBlocked()),
                    "power", MythicLib.plugin.getMMOConfig().decimal.format(mitigationEvent.getPower() * 100.));
            event.getDamage().multiplicativeModifier(1 - mitigationEvent.getPower());
            player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR.toSound(), 2, 1);

            double yaw = getYaw(player, getVector(player, event)) + 90;
            for (double j = yaw - 90; j < yaw + 90; j += 5)
                for (double y = 0; y < 2; y += .1)
                    player.getWorld().spawnParticle(Particle.REDSTONE,
                            player.getLocation().clone().add(Math.cos(Math.toRadians(j)) * .7, y, Math.sin(Math.toRadians(j)) * .7), 1,
                            new Particle.DustOptions(Color.GRAY, 1f));
        }
    }

    private void sendMessage(Player player, String format, Object... placeholders) {
        if (format == null || format.isEmpty())
            return;

        format = ChatColor.translateAlternateColorCodes('&', format);
        for (int i = 0; i < placeholders.length; i += 2)
            format = format.replace("#" + placeholders[i].toString() + "#", placeholders[i + 1].toString());

        if (actionBarMessage)
            MythicLib.plugin.getVersion().getWrapper().sendActionBar(player, format);
        else
            player.sendMessage(format);
    }

    /**
     * @param cooldown      Default cooldown
     * @param reductionStat Mitigation cooldown reduction
     * @return The actual player cooldown
     */
    private double calculateCooldown(double cooldown, double reductionStat) {
        return cooldown * (1 - reductionStat / 100);
    }

    /**
     * @param victim Entity being hit
     * @return If there is a damager, returns a vector pointing towards damager.
     *         Otherwise, just returns the victim's eye location.
     */
    @NotNull
    private Vector getVector(LivingEntity victim, AttackEvent event) {
        final Entity damager = event.toBukkit() instanceof EntityDamageByEntityEvent ? ((EntityDamageByEntityEvent) event.toBukkit()).getDamager() : null;
        return damager == null ? normalize(damager.getLocation().subtract(victim.getLocation()).toVector()) : victim.getEyeLocation().getDirection();
    }

    @NotNull
    private Vector normalize(Vector vec) {
        return vec.lengthSquared() == 0 ? vec : vec.normalize();
    }

    private double getYaw(Entity player, Vector vec) {
        return new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ()).setDirection(vec).getYaw();
    }
}

