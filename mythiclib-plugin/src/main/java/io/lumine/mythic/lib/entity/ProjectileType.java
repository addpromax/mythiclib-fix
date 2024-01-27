package io.lumine.mythic.lib.entity;

import io.lumine.mythic.lib.skill.trigger.TriggerType;

public enum ProjectileType {
    ARROW,
    TRIDENT;

    private final TriggerType tick, hit, land;

    ProjectileType() {
        tick = new TriggerType(name() + "_TICK");
        hit = new TriggerType(name() + "_HIT");
        land = new TriggerType(name() + "_LAND");
    }

    public TriggerType getTickTrigger() {
        return tick;
    }

    public TriggerType getHitTrigger() {
        return hit;
    }

    public TriggerType getLandTrigger() {
        return land;
    }
}