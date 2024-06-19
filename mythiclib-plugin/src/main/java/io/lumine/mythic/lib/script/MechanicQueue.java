package io.lumine.mythic.lib.script;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.mechanic.misc.DelayMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.logging.Level;

/**
 * This class is used to take into account the delay mechanic.
 * Because of this mechanic, you can't cast all the mechanics at
 * the same time and must keep track of what mechanic was cast last.
 * <p>
 * One queue created PER skill casting. If you want to cast the same
 * skill a second time, a second {@link MechanicQueue} must be created
 */
public class MechanicQueue {
    private final Iterator<Mechanic> queue;
    private final SkillMetadata meta;
    private final Script skill;

    /**
     * Used in error messages to tell the user
     * what mechanic threw the error.
     */
    private int counter = 0;

    public MechanicQueue(SkillMetadata meta, Script skill) {
        this.meta = meta;
        this.queue = skill.getMechanics().iterator();
        this.skill = skill;
    }

    /**
     * Casts the next mechanic in the queue. Calling this
     * method for the first time starts casting a skill.
     *
     * @return False if the queue has no more mechanic to cast
     * @implNote This method is implemented iteratively while a while loop
     * and NOT recursively as skills can be composed of hundreds or thousands
     * of mechanics leaving to the Java call stack able to freely grow and
     * take loads of RAM, eventually killing performance (tail-end optimization
     * does not work here).
     */
    public boolean next() {
        while (queue.hasNext()) {

            counter++;
            final Mechanic mechanic = queue.next();

            // Schedule delayed execution of next mechanic
            if (mechanic instanceof DelayMechanic) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(MythicLib.plugin, this::next, ((DelayMechanic) mechanic).getDelay(meta));
                return false;
            }

            // Java does not support tail-end optimization. Avoid growing stack at all cost.
            try {
                mechanic.cast(meta);
            } catch (RuntimeException exception) {
                MythicLib.plugin.getLogger().log(Level.WARNING, "Could not execute mechanic n" + counter + " from skill '" + skill.getId() + "': " + exception.getMessage());
            }
        }

        return true;
    }
}
