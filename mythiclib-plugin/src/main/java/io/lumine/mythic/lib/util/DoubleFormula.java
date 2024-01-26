package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Instead of directly using a double in a skill, we rather use a string
 * where internal or PAPI placeholders are parsed before finally
 * evaluating the formula. This represents 90% of the total skill
 * system configurability.
 */
public class DoubleFormula {
    @Nullable
    private final String value;
    @Nullable
    private final Double constant;

    public static final DoubleFormula ZERO = new DoubleFormula(0);

    public DoubleFormula(@NotNull String inputFormula) {
        String value = null;
        Double constant = null;

        try {
            constant = Double.valueOf(inputFormula);
        } catch (IllegalArgumentException exception) {
            value = inputFormula;
        }

        this.value = value;
        this.constant = constant;
    }

    /**
     * Double formula with constant value
     */
    public DoubleFormula(double trivialValue) {
        this.value = null;
        this.constant = trivialValue;
    }

    public double evaluate(@NotNull SkillMetadata meta) {

        // Easy case
        if (constant != null) return constant;

        try {
            return MythicLib.plugin.getFormulaParser().evaluateAsDouble(meta.parseString(value));
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not evaluate '" + value + "' while casting skill '" + meta.getCast().getHandler().getId() + "': " + exception.getMessage());
            return 0;
        }
    }

    public static DoubleFormula constant(double value) {
        return new DoubleFormula(value);
    }
}
