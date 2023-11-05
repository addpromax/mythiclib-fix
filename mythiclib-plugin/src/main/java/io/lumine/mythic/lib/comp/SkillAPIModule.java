package io.lumine.mythic.lib.comp;

import com.sucy.skill.api.DefaultCombatProtection;
import com.sucy.skill.api.event.SkillDamageEvent;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.fake.FakeEventCaller;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillAPIModule implements Listener, FakeEventCaller<EntityDamageEvent> {
    public SkillAPIModule() {
        MythicLib.plugin.getFakeEvents().registerFakeEventCaller(EntityDamageEvent.class, this);
        Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
    }

    @Override
    public boolean isFake(EntityDamageEvent event) {
        return event instanceof DefaultCombatProtection.FakeEntityDamageByEntityEvent;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void a(SkillDamageEvent event) {
        final StatProvider damager = StatProvider.get(event.getDamager(), EquipmentSlot.MAIN_HAND, true);
        final DamageMetadata damageMeta = new DamageMetadata(event.getDamage(), DamageType.SKILL, DamageType.MAGIC);
        final AttackMetadata attackMeta = new AttackMetadata(damageMeta, event.getTarget(), damager);
        MythicLib.plugin.getDamage().markAsMetadata(attackMeta);
    }
}
