package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.Position;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.apache.commons.lang.Validate;

@MechanicMetadata
public class SetZMechanic extends VariableMechanic {
    private final DoubleFormula coordinate;

    public SetZMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("z");

        coordinate = new DoubleFormula(config.getString("z"));
    }

    @Override
    public void cast(SkillMetadata meta) {

        Variable targetVar = meta.getCustomVariable(getVariableName());
        Validate.isTrue(targetVar instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        Position target = (Position) targetVar.getStored();

        target.setZ(coordinate.evaluate(meta));
    }
}
