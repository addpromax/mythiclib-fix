package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Swiftness extends SkillHandler<SimpleSkillResult> {
    public Swiftness() {
        super();

        registerModifiers("amplifier", "duration");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration");
        int amplifier = (int) skillMeta.getParameter("amplifier");

        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), VersionSound.ENTITY_ZOMBIE_PIGMAN_ANGRY.toSound(), 1, .3f);
        for (double y = 0; y <= 2; y += .2)
            for (double j = 0; j < Math.PI * 2; j += Math.PI / 16)
                if (random.nextDouble() <= .7)
                    caster.getWorld().spawnParticle(Particle.SPELL_INSTANT, caster.getLocation().add(Math.cos(j), y, Math.sin(j)), 0);
        caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), amplifier));
        caster.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (duration * 20), amplifier));
    }
}
