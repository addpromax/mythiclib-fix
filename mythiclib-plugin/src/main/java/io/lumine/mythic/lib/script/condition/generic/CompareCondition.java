package io.lumine.mythic.lib.script.condition.generic;

import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.condition.Condition;

import java.util.function.BiPredicate;

/**
 * Compares two double numbers using the specified comparator
 */
public class CompareCondition extends Condition {
    private final Comparator comparator;
    private final DoubleFormula first, second;

    private static final double SMALLEST_DIFFERENCE = .0000001;

    public CompareCondition(ConfigObject config) {
        super(config);

        config.validateKeys("first", "comparator", "second");

        first = new DoubleFormula(config.getString("first"));
        second = new DoubleFormula(config.getString("second"));
        comparator = Comparator.fromString(config.getString("comparator"));
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return comparator.test(first.evaluate(meta), second.evaluate(meta));
    }

    public enum Comparator {
        EQUALS("=", (d1, d2) -> Math.abs(d1 - d2) < SMALLEST_DIFFERENCE),

        LOWER("<=", (d1, d2) -> d1 < d2 + SMALLEST_DIFFERENCE),

        GREATER(">=", (d1, d2) -> d1 + SMALLEST_DIFFERENCE > d2),

        STRICTLY_LOWER("<", (d1, d2) -> d1 < d2),

        STRICTLY_GREATER(">", (d1, d2) -> d1 > d2);

        private final String str;
        private final BiPredicate<Double, Double> predicate;

        Comparator(String str, BiPredicate<Double, Double> predicate) {
            this.str = str;
            this.predicate = predicate;
        }

        public boolean test(double d1, double d2) {
            return predicate.test(d1, d2);
        }

        @Override
        public String toString() {
            return str;
        }

        public static Comparator fromString(String input) {
            for (Comparator comp : values())
                if (comp.toString().equals(input) || comp.name().equals(input))
                    return comp;
            throw new IllegalArgumentException("Could not read comparator from '" + input + "'");
        }
    }
}
