package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Regen_Ally extends SkillHandler<TargetSkillResult> {
    public Regen_Ally() {
        super();

        registerModifiers("heal", "duration");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta, InteractionType.SUPPORT_SKILL);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();

        new BukkitRunnable() {
            final double duration = Math.min(skillMeta.getParameter("duration"), 60) * 20;
            final double hps = skillMeta.getParameter("heal") / duration * 4;
            double ti = 0;
            double a = 0;

            public void run() {
                if (ti++ > duration || target.isDead()) {
                    cancel();
                    return;
                }

                a += Math.PI / 16;
                target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(1.3 * Math.cos(a), .3, 1.3 * Math.sin(a)), 0);

                if (ti % 4 == 0)
                    UtilityMethods.heal(target, hps);
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
