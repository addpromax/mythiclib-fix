package io.lumine.mythic.lib.listener.option;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.util.Vector;

public class RegenIndicators extends GameIndicators {
    public RegenIndicators(ConfigurationSection config) {
        super(config);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void a(EntityRegainHealthEvent event) {

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity) || event.getAmount() <= 0
                || ((LivingEntity) entity).getHealth() >= ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
            return;

        // Display no indicator around vanished player
        if (entity instanceof Player && UtilityMethods.isVanished((Player) entity))
            return;

        displayIndicator(entity, getRaw().replace("#", formatNumber(event.getAmount())), getIndicatorDirection(entity), IndicatorDisplayEvent.IndicatorType.REGENERATION);
    }

    /**
     * For non-player entities, a random direction is taken.
     * <p>
     * For players, direction is taken randomly in a PI/2
     * cone behind the player so that it does not bother the player
     *
     * @param entity Player or monster
     * @return Random (normalized) direction for the hologram
     */
    private Vector getIndicatorDirection(Entity entity) {

        if (entity instanceof Player) {
            double a = Math.toRadians(((Player) entity).getEyeLocation().getYaw()) + Math.PI * (1 + (random.nextDouble() - .5) / 2);
            return new Vector(Math.cos(a), 0, Math.sin(a));
        }

        double a = random.nextDouble() * Math.PI * 2;
        return new Vector(Math.cos(a), 0, Math.sin(a));
    }
}
