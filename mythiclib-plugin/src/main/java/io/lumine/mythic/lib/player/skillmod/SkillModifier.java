package io.lumine.mythic.lib.player.skillmod;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.api.InstanceModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.skill.handler.SkillHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A skill "modifier" modifies a specific parameter of a skill,
 * in the same way that a stat modifier modifies a stat for a player.
 * It can also be given a boolean formula, which determines which
 * skills the modifier will apply onto.
 *
 * @author Guillaume, Jules
 */
public class SkillModifier extends InstanceModifier {

    /**
     * The list of all the skills this modifier will be applied to.
     * A skill modifier can target one skill or a set of skills like
     * giving for example +10% damage to all the passive skills.
     */
    private final List<SkillHandler<?>> skills;
    private final String parameter;

    /**
     * Flat skill buff (simplest modifier you can think about)
     */
    public SkillModifier(String key, String parameter, List<SkillHandler<?>> skills, double value) {
        this(key, parameter, skills, value, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    /**
     * skill buff given by an external mechanic, like a party buff, item set bonuses,
     * skills or abilities... Anything apart from items and armor.
     */
    public SkillModifier(String key, String parameter, List<SkillHandler<?>> skills, double value, ModifierType type) {
        this(key, parameter, skills, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    public SkillModifier(String key, String parameter, List<SkillHandler<?>> skills, double value, ModifierType type, EquipmentSlot slot, ModifierSource source) {
        super(key, slot, source, value, type);

        this.skills = skills;
        this.parameter = parameter;
    }

    public SkillModifier(UUID uniqueId, String key, String parameter, List<SkillHandler<?>> skills, double value, ModifierType type, EquipmentSlot slot, ModifierSource source) {
        super(uniqueId, key, slot, source, value, type);

        this.skills = skills;
        this.parameter = parameter;
    }

    /**
     * Used to add a constant to some existing stat modifier, usually an
     * integer, for instance it is used when a skill buff trigger is triggered multiple times.
     *
     * @param offset The offset added.
     * @return A new instance of SkillBuff with modified value
     */
    public SkillModifier add(double offset) {
        return new SkillModifier(getUniqueId(), getKey(), parameter, new ArrayList(skills), value + offset, type, getSlot(), getSource());
    }

    public List<SkillHandler<?>> getSkills() {
        return skills;
    }

    public String getParameter() {
        return parameter;
    }

    /**
     * Used to register the skillBuff for only 1 specific skill.
     */
    @Deprecated
    public void register(MMOPlayerData playerData, SkillHandler<?> handler) {
        playerData.getSkillModifierMap().getInstance(handler, parameter).registerModifier(this);
    }

    /**
     * Used to unregister the skillBuff for only 1 specific skill.
     */
    @Deprecated
    public void unregister(MMOPlayerData playerData, SkillHandler<?> handler) {
        playerData.getSkillModifierMap().getInstance(handler, parameter).removeModifier(getUniqueId());
    }

    @Override
    public void register(MMOPlayerData playerData) {
        for (SkillHandler<?> handler : skills)
            playerData.getSkillModifierMap().getInstance(handler, parameter).registerModifier(this);
    }

    @Override
    public void unregister(MMOPlayerData playerData) {
        for (SkillHandler<?> handler : skills)
            playerData.getSkillModifierMap().getInstance(handler, parameter).removeModifier(getUniqueId());
    }
}
