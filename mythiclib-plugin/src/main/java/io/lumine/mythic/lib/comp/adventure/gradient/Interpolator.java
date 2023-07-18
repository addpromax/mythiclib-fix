package io.lumine.mythic.lib.comp.adventure.gradient;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 * <p>
 * This class is responsible for interpolating between two colors.
 */
@FunctionalInterface
public
interface Interpolator {

    double[] interpolate(double from, double to, int max);

    Interpolator LINEAR = (from, to, max) -> {
        final double[] res = new double[max];
        for (int i = 0; i < max; i++)
            res[i] = from + i * ((to - from) / (max - 1));
        return res;
    };

    Interpolator QUADRATIC_SLOW_TO_FAST = (from, to, max) -> {
        final double[] results = new double[max];
        double a = (to - from) / (max * max);
        for (int i = 0; i < results.length; i++)
            results[i] = a * i * i + from;
        return results;
    };

    Interpolator QUADRATIC_FAST_TO_SLOW = (from, to, max) -> {
        final double[] results = new double[max];
        double a = (from - to) / (max * max);
        double b = -2 * a * max;
        for (int i = 0; i < results.length; i++)
            results[i] = a * i * i + b * i + from;
        return results;
    };
}