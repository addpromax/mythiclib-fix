package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleModifierSupplier implements ModifierSupplier {
    private final List<PlayerModifier> cache = new ArrayList<>();

    @NotNull
    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.OTHER;
    }

    @NotNull
    @Override
    public ModifierSource getModifierSource() {
        return ModifierSource.OTHER;
    }

    @NotNull
    @Override
    public Collection<PlayerModifier> getModifierCache() {
        return cache;
    }
}
