package io.lumine.mythic.lib.script.targeter.entity;

import io.lumine.mythic.lib.script.targeter.location.DefaultLocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.List;

/**
 * Uses the target entity if not null or caster by default.
 * <p>
 * Analog of {@link DefaultLocationTargeter}
 */
public class DefaultEntityTargeter implements EntityTargeter {

    @Override
    public List<Entity> findTargets(SkillMetadata meta) {
        return Arrays.asList(meta.getSkillEntity(false));
    }
}
