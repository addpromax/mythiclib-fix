package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Poison extends SkillHandler<TargetSkillResult> {
    public Poison() {
        super();

        registerModifiers("duration", "amplifier");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();

        target.getWorld().spawnParticle(Particle.SLIME, target.getLocation().add(0, 1, 0), 32, 1, 1, 1, 0);
        target.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, target.getLocation().add(0, 1, 0), 24, 1, 1, 1, 0);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.5f, 2);
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (skillMeta.getParameter("duration") * 20), (int) skillMeta.getParameter("amplifier")));
    }
}
