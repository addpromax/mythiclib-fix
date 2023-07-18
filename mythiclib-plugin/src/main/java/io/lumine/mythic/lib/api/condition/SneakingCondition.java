package io.lumine.mythic.lib.api.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.condition.type.MMOCondition;
import io.lumine.mythic.lib.api.condition.type.PlayerCondition;
import org.bukkit.entity.Player;

public class SneakingCondition extends MMOCondition implements PlayerCondition {
    public SneakingCondition(MMOLineConfig config) {
        super(config);
    }

    @Override
    public boolean check(Player player) {
        return player.isSneaking();
    }
}

