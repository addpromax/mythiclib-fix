package io.lumine.mythic.lib.script.variable.def;

import io.lumine.mythic.lib.script.variable.SimpleVariableRegistry;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableMetadata;
import io.lumine.mythic.lib.script.variable.VariableRegistry;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;

@VariableMetadata(name = "attributes")
public class AttributesVariable extends Variable<Attributable> {
    public static final SimpleVariableRegistry<Attributable> VARIABLE_REGISTRY = new SimpleVariableRegistry<>();

    static {
        for (Attribute attribute : Attribute.values())
            VARIABLE_REGISTRY.registerVariable(attribute.name().substring("GENERIC_".length()).toLowerCase(), var -> new DoubleVariable("temp", var.getAttribute(attribute).getValue()));
    }

    public AttributesVariable(String name, Attributable attr) {
        super(name, attr);
    }

    @Override
    public VariableRegistry<Variable<Attributable>> getVariableRegistry() {
        return VARIABLE_REGISTRY;
    }

    @Override
    public String toString() {
        return getStored() == null ? "None" : "Attributable";
    }
}