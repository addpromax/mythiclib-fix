package io.lumine.mythic.lib.hologram;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.listener.option.GameIndicators;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Hologram {

    public abstract void despawn();

    public abstract boolean isSpawned();

    public abstract void updateLocation(@NotNull Location loc);

    public void flyOut(@NotNull GameIndicators settings, @NotNull Vector dir) {
        new BukkitRunnable() {
            final Location loc = getLocation().clone();
            double v = 6 * settings.initialUpwardVelocity; // Initial upward velocity
            int i = 0; // Counter

            private final double acc = -10 * settings.gravity; // Downwards acceleration
            private static final double DT = 3d / 20d; // Delta_t used to integrate acceleration and velocity

            @Override
            public void run() {
                try {
                    // Check if hologram is still spawned
                    if (!isSpawned()) {
                        cancel();
                        return;
                    }

                    if (i == 0) dir.multiply(2 * settings.radialVelocity);

                    // Remove hologram when reaching end of life
                    if (i++ >= settings.lifespan) {
                        despawn();
                        cancel();
                        return;
                    }

                    v += acc * DT;
                    loc.add(dir.getX() * DT, v * DT, dir.getZ() * DT);
                    updateLocation(loc);
                } catch (Exception e) {
                    MythicLib.plugin.getLogger().warning("Error in hologram flyOut task: " + e.getMessage());
                    despawn();
                    cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, settings.tickPeriod);
    }

    public abstract void updateLines(List<String> lines);

    public abstract List<String> getLines();

    public abstract Location getLocation();

    public static Hologram create(Location loc, List<String> lines) {
        return MythicLib.plugin.getHologramFactory().newHologram(loc, lines);
    }
}
