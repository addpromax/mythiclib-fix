package io.lumine.mythic.lib.player.particle;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierMap;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @deprecated Not implemented yet
 */
@Deprecated
public class ParticleEffectMap extends ModifierMap<ParticleEffect> {
    public ParticleEffectMap(MMOPlayerData playerData) {
        super(playerData);
    }

    @Override
    public @Nullable ParticleEffect addModifier(ParticleEffect modifier) {
        // TODO custom register function
        return super.addModifier(modifier);
    }

    @Override
    public @Nullable ParticleEffect removeModifier(UUID uuid) {
        // TODO custom remove function
        return super.removeModifier(uuid);
    }
}
