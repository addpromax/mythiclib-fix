package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.api.event.skill.SkillCastEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Implemented by MMOItems abilities or MMOCore class skills.
 * <p>
 * This class implements all skill restrictions and behaviours
 * that are SPECIFIC to MMOItems or MMOCore like resource costs,
 * cooldown messages, no-cooldown modes...
 * <p>
 * TODO move trigger type to a higher class like PassiveSkill,
 * TODO has NOTHING to do here and is very impractical when
 * TODO trying to cast skills
 *
 * @author jules
 */
public abstract class Skill implements CooldownObject {
    private final TriggerType trigger;

    public Skill(@NotNull TriggerType trigger) {
        this.trigger = Objects.requireNonNull(trigger, "Trigger cannot be null");
    }

    @NotNull
    public SkillResult cast(@NotNull MMOPlayerData caster) {
        return cast(new TriggerMetadata(caster, trigger));
    }

    @NotNull
    public SkillResult cast(@NotNull SynchronizedDataHolder caster) {
        return cast(caster.getMMOPlayerData());
    }

    @NotNull
    public SkillResult cast(@NotNull TriggerMetadata triggerMeta) {
        return cast(triggerMeta.toSkillMetadata(this));
    }

    @NotNull
    public <T extends SkillResult> SkillResult cast(@NotNull SkillMetadata meta) {
        final SkillHandler<T> handler = (SkillHandler<T>) getHandler();

        // Lower level skill restrictions
        final T result = handler.getResult(meta);
        if (!result.isSuccessful()) return result;

        // High level skill restrictions
        if (!getResult(meta)) return result;

        // Call first Bukkit event
        final PlayerCastSkillEvent called = new PlayerCastSkillEvent(meta, result);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled()) return result;

        // If the delay is null we cast normally the skill
        final int delayTicks = (int) (meta.getParameter("delay") * 20);
        if (delayTicks <= 0) castInstantly(meta, result);
        else new CastingDelayHandler(meta, result);

        return result;
    }

    /**
     * Called when the casting delay (potentially zero) is passed. This
     * not DOES call {@link PlayerCastSkillEvent} and not DOES check for
     * both high & low level skill conditions.
     * <p>
     * This method however calls {@link SkillCastEvent} after skill casting.
     */
    @NotNull
    public <T extends SkillResult> void castInstantly(@NotNull SkillMetadata meta, @NotNull T result) {

        // High level skill effects
        whenCast(meta);

        // Lower level skill effects
        ((SkillHandler<T>) getHandler()).whenCast(result, meta);

        // Call second Bukkit event
        Bukkit.getPluginManager().callEvent(new SkillCastEvent(meta, result));
    }

    /**
     * This method should be used to check for resource costs
     * or other skill limitations.
     * <p>
     * Runs last after {@link SkillHandler#getResult(SkillMetadata)}
     *
     * @param skillMeta Info of skill being cast
     * @return If the skill can be cast
     */
    public abstract boolean getResult(@NotNull SkillMetadata skillMeta);

    /**
     * This is NOT where the actual skill effects are applied.
     * <p>
     * This method should be used to handle resource costs
     * or cooldown messages if required.
     * <p>
     * Runs first before {@link SkillHandler#whenCast(SkillResult, SkillMetadata)}
     *
     * @param skillMeta Info of skill being cast
     */
    public abstract void whenCast(@NotNull SkillMetadata skillMeta);

    @NotNull
    public abstract SkillHandler<?> getHandler();

    /**
     * This contains the following information:
     * - whether or not the skill is active or passive
     * - whether or not the skill is silent
     *
     * @return Context in which this skill is triggered
     */
    @NotNull
    public TriggerType getTrigger() {
        return trigger;
    }

    /**
     * @deprecated Skill modifiers are now called "parameters"
     */
    @Deprecated
    public double getModifier(String path) {
        return getParameter(path);
    }

    /**
     * !! WARNING !! Final skill parameter values also depend
     * on the player's skill modifiers, and this method does NOT
     * take them into account.
     *
     * @param path Modifier name.
     * @return The skill parameter value unaffected by skill modifiers.
     * @see {@link SkillMetadata#getParameter(String)}
     */
    public double getParameter(String path) {
        return getModifier(path);
    }

    @Override
    public String getCooldownPath() {
        return "skill_" + getHandler().getId();
    }
}
