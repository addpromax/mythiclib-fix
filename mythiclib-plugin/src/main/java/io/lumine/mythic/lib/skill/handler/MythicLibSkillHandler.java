package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.MechanicQueue;
import io.lumine.mythic.lib.skill.result.MythicLibSkillResult;
import org.bukkit.configuration.ConfigurationSection;

/**
 * A skill behaviour based on a custom MythicLib script
 */
public class MythicLibSkillHandler extends SkillHandler<MythicLibSkillResult> {
    private final Script script;

    public MythicLibSkillHandler(ConfigurationSection config, Script script) {
        super(config, script.getId());

        this.script = script;
    }

    public MythicLibSkillHandler(Script script) {
        super(script.getId());

        this.script = script;
    }

    @Override
    public MythicLibSkillResult getResult(SkillMetadata meta) {
        return new MythicLibSkillResult(meta, script);
    }

    @Override
    public void whenCast(MythicLibSkillResult result, SkillMetadata skillMeta) {
        new MechanicQueue(skillMeta, script).next();
    }
}
