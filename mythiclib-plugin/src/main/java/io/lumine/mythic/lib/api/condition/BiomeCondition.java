package io.lumine.mythic.lib.api.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.condition.type.LocationCondition;
import io.lumine.mythic.lib.api.condition.type.MMOCondition;
import org.bukkit.Location;

public class BiomeCondition extends MMOCondition implements LocationCondition {
    private final String biome;

    public BiomeCondition(MMOLineConfig config) {
        super(config);

        config.validateKeys("biome");
        this.biome = config.getString("biome");
    }

    @Override
    public boolean check(Location location) {
        return location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ()).name().equalsIgnoreCase(biome);
    }
}
