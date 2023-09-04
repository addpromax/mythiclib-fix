package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Human_Shield extends SkillHandler<TargetSkillResult> {
    public Human_Shield() {
        super();

        registerModifiers("reduction", "redirect", "duration", "low");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        TargetSkillResult result = new TargetSkillResult(meta, 7, InteractionType.SUPPORT_SKILL);
        return result.isSuccessful(meta) && result.getTarget() instanceof Player ? result : new TargetSkillResult((LivingEntity) null);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1, 1);
        new HumanShield(skillMeta.getCaster().getData(), (Player) result.getTarget(), skillMeta.getParameter("reduction"), skillMeta.getParameter("redirect"), skillMeta.getParameter("duration"), skillMeta.getParameter("low"));
    }

    public static class HumanShield extends BukkitRunnable implements Listener {
        private final MMOPlayerData caster;
        private final Player target;
        private final double damageCoefficient, redirectRate, duration, minimumHealthPercentage;

        private int j;

        public HumanShield(MMOPlayerData caster, Player target, double reduction, double redirect, double duration, double low) {
            this.target = target;
            this.caster = caster;

            damageCoefficient = 1 - Math.min(1, reduction / 100);
            redirectRate = redirect / 100;
            this.duration = duration * 20;
            minimumHealthPercentage = low / 100;

            runTaskTimer(MythicLib.plugin, 0, 1);
            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
        }

        @EventHandler
        public void a(EntityDamageEvent event) {
            if (event.getEntity().equals(target)) {

                double damage = event.getDamage() * damageCoefficient;
                event.setDamage(damage);

                double health = caster.getPlayer().getHealth() - damage * redirectRate;
                if (health > caster.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * minimumHealthPercentage)
                    caster.getPlayer().setHealth(health);
                else {
                    caster.getPlayer().setHealth(1);
                    close();
                }
            }
        }

        @Override
        public void run() {
            if (!caster.isOnline() || caster.getPlayer().isDead() || !target.isOnline() || target.isDead() || j++ >= duration) {
                close();
                return;
            }

            double a = (double) j / 5;
            target.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, target.getLocation().add(Math.cos(a), 1 + Math.sin(a / 3) / 1.3, Math.sin(a)), 0);
        }

        private void close() {
            cancel();
            HandlerList.unregisterAll(this);
        }
    }
}
