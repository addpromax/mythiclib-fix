package io.lumine.mythic.lib.comp.adventure.resolver.implementation;

import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgument;
import io.lumine.mythic.lib.comp.adventure.argument.AdventureArgumentQueue;
import io.lumine.mythic.lib.comp.adventure.gradient.GradientBuilder;
import io.lumine.mythic.lib.comp.adventure.gradient.Interpolator;
import io.lumine.mythic.lib.comp.adventure.resolver.ContextTagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

/**
 * mythiclib
 * 01/12/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class RainbowResolver implements ContextTagResolver {

    private static final Color[] colors;

    static {
        colors = new Color[]{
                new Color(243, 138, 50),
                new Color(255, 255, 85),
                new Color(82, 255, 56),
                new Color(62, 136, 252),
                new Color(248, 54, 126),
                new Color(240, 64, 70)
        };
    }

    @Override
    public @Nullable String resolve(@NotNull String src, @NotNull AdventureArgumentQueue argsQueue, @NotNull String context, @NotNull List<String> decorations) {
        if (!argsQueue.hasNext())
            return GradientBuilder.multiRgbGradient(context, colors, 0, Interpolator.LINEAR, decorations);
        AdventureArgument argument = argsQueue.pop();
        if (argument.asInt().isPresent())
            return GradientBuilder.multiRgbGradient(context, colors, 1 - argument.asInt().getAsInt(), Interpolator.LINEAR, decorations);
        else if (containsNumberAndExclamation(argument.value())) {
            try {
                int phase = Integer.parseInt(argument.value().substring(1));
                return GradientBuilder.multiRgbGradient(context, colors, 1 - phase, Interpolator.LINEAR, decorations);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (argument.value().equals("!"))
            return GradientBuilder.multiRgbGradient(context, colors, 1, Interpolator.LINEAR, decorations);
        return null;
    }

    private boolean containsNumberAndExclamation(String src) {
        return src.matches("![0-9]+");
    }
}
