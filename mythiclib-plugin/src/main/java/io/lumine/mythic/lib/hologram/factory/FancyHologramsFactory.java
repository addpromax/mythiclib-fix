package io.lumine.mythic.lib.hologram.factory;

import io.lumine.mythic.lib.hologram.Hologram;
import io.lumine.mythic.lib.hologram.HologramFactory;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Compatibility layer with FancyHolograms.
 * Works with both 2.x and 3.x APIs using reflection, so MythicLib can
 * be distributed without a hard compile-time dependency on either version.
 */
public class FancyHologramsFactory implements HologramFactory {

    @Override
    public @NotNull Hologram newHologram(@NotNull Location loc, @NotNull List<String> lines) {
        return new FHImpl(loc, lines);
    }

    /** Wrapper that delegates to the concrete FancyHolograms hologram instance via reflection */
    private static final class FHImpl extends Hologram {

        private final Object delegate;          // the FancyHolograms hologram instance
        private final Class<?> delegateClass;    // its runtime class

        private boolean spawned = true;

        FHImpl(Location loc, List<String> textLines) {

            String name = "MythicLib-" + UUID.randomUUID();

            Object hologram = null;
            Class<?> holoClass = null;

            try {
                // Attempt FancyHolograms 3.x builder pathway first
                Class<?> builderCls = Class.forName("de.oliver.fancyholograms.api.data.builder.TextHologramBuilder");
                Method createM = builderCls.getMethod("create", String.class, Location.class);
                Object builder = createM.invoke(null, name, loc);

                // text(List<String>)
                builderCls.getMethod("text", List.class).invoke(builder, textLines);

                // persistent(boolean)
                builderCls.getMethod("persistent", boolean.class).invoke(builder, false);

                // buildAndRegister()
                Method buildAndReg = builderCls.getMethod("buildAndRegister");
                hologram = buildAndReg.invoke(builder);

                holoClass = hologram.getClass();

            } catch (ClassNotFoundException e) {
                // Fall back to 2.x simple API (no builder)
                try {
                    // FancyHolograms 2.x: use plugin factory
                    Object plugin = Bukkit.getPluginManager().getPlugin("FancyHolograms");
                    if (plugin == null)
                        throw new IllegalStateException("FancyHolograms plugin not found");
                    Class<?> pluginCls = plugin.getClass();

                    try {
                        Method getFactory = pluginCls.getMethod("getHologramFactory");
                        Object factoryFunc = getFactory.invoke(plugin);

                        // Build TextHologramData
                        Class<?> dataCls = Class.forName("de.oliver.fancyholograms.api.data.TextHologramData");
                        Object data = dataCls.getConstructor(String.class, Location.class).newInstance(name, loc);
                        dataCls.getMethod("setText", List.class).invoke(data, textLines);

                        Method applyM = factoryFunc.getClass().getMethod("apply", Object.class);
                        hologram = applyM.invoke(factoryFunc, data);
                        holoClass = hologram.getClass();
                    } catch (NoSuchMethodException nf2) {
                        // Older 2.x API: createSimpleHologram(String, Location, List<String>) on plugin class
                        Method createSimple = pluginCls.getMethod("createSimpleHologram", String.class, Location.class, List.class);
                        hologram = createSimple.invoke(plugin, name, loc, textLines);
                        holoClass = hologram.getClass();
                    }
                } catch (Throwable ex) {
                    throw new RuntimeException("Could not create FancyHolograms hologram", ex);
                }
            } catch (Throwable error) {
                throw new RuntimeException("Failed to initialise FancyHolograms hologram", error);
            }

            this.delegate = hologram;
            this.delegateClass = holoClass;
        }

        // -------- Delegate helpers --------
        private Object getData() {
            try {
                return delegateClass.getMethod("getData").invoke(delegate);
            } catch (Throwable ignored) {
                return null;
            }
        }

        @Override
        public void despawn() {
            if (!spawned) return;
            spawned = false;
            try {
                // Try registry unregister (3.x)
                Class<?> fhApi = Class.forName("de.oliver.fancyholograms.api.FancyHolograms");
                Object api = fhApi.getMethod("get").invoke(null);
                Object registry = fhApi.getMethod("getRegistry").invoke(api);
                registry.getClass().getMethod("unregister", delegateClass).invoke(registry, delegate);
            } catch (Throwable ignored) {
                // Fallback: call despawn()/delete if available
                try {
                    delegateClass.getMethod("despawn").invoke(delegate);
                } catch (Throwable ignored2) {
                    // ignore
                }
            }
        }

        @Override
        public boolean isSpawned() {
            return spawned;
        }

        @Override
        public void updateLocation(@NotNull Location loc) {
            try {
                Object data = getData();
                if (data != null) {
                    data.getClass().getMethod("setLocation", Location.class).invoke(data, loc);
                } else {
                    delegateClass.getMethod("setLocation", Location.class).invoke(delegate, loc);
                }
            } catch (Throwable ignored) {
            }
        }

        @Override
        public void updateLines(@NotNull List<String> lines) {
            try {
                Object data = getData();
                if (data != null) {
                    // 3.x TextHologramData#setText(List)
                    try {
                        data.getClass().getMethod("setText", List.class).invoke(data, lines);
                        return;
                    } catch (NoSuchMethodException ignored) {
                    }
                }

                // 2.x may expose setText(List) directly on hologram
                delegateClass.getMethod("setText", List.class).invoke(delegate, lines);
            } catch (Throwable ignored) {
            }
        }

        @Override
        public List<String> getLines() {
            try {
                Object data = getData();
                if (data != null) {
                    Object res = data.getClass().getMethod("getText").invoke(data);
                    if (res instanceof List) return (List<String>) res;
                }
                Object res = delegateClass.getMethod("getText").invoke(delegate);
                if (res instanceof List) return (List<String>) res;
            } catch (Throwable ignored) {
            }
            return Collections.emptyList();
        }

        @Override
        public Location getLocation() {
            try {
                Object data = getData();
                if (data != null) {
                    Object loc = data.getClass().getMethod("getLocation").invoke(data);
                    if (loc instanceof Location) return (Location) loc;
                }
                Object loc = delegateClass.getMethod("getLocation").invoke(delegate);
                if (loc instanceof Location) return (Location) loc;
            } catch (Throwable ignored) {
            }
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
    }

    /**
     * FancyHolograms attaches the InteractionTrait to every newly created hologram by default.
     * That trait persists its configuration to the disk which would generate a new JSON file
     * for each temporary damage indicator spawned by MythicLib. To avoid polluting the
     * plugins/FancyHolograms/data/traits/interaction_trait folder we explicitly unregister the
     * trait from the registry at runtime (only available in FH 3.x).
     */
    static {
        try {
            Class<?> fhApi = Class.forName("de.oliver.fancyholograms.api.FancyHolograms");
            Object api = fhApi.getMethod("get").invoke(null);
            Object traitRegistry = fhApi.getMethod("getTraitRegistry").invoke(api);
            Class<?> interactionTraitCls = Class.forName("de.oliver.fancyholograms.trait.builtin.InteractionTrait");
            traitRegistry.getClass().getMethod("unregister", Class.class).invoke(traitRegistry, interactionTraitCls);
        } catch (Throwable ignored) {
            // Either FancyHolograms is not present, running an older 2.x version, or API changed – ignore.
        }
    }
} 