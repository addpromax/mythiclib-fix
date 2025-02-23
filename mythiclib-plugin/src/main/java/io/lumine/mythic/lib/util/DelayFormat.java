package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Util class to change how time delays are displayed
 */
public class DelayFormat {

    private final static char[] DELAY_CHARACTERS = {'s', 'm', 'h', 'd', 'M', 'y'};
    private final static long[] DELAY_AMOUNTS = {
            1000,
            1000 * 60,
            1000 * 60 * 60,
            1000 * 60 * 60 * 24,
            (long) (1000 * 60 * 60 * 24 * 30.436875),
            (long) (1000 * 60 * 60 * 24 * 365.2491)};

    private final boolean[] charactersUsed;
    private final String[] translation;

    @Nullable
    private final String threshold, each;
    private final int smallestUnit;

    public DelayFormat() {
        charactersUsed = new boolean[DELAY_CHARACTERS.length];
        for (int i = 0; i < DELAY_CHARACTERS.length; i++)
            charactersUsed[i] = true;
        translation = null;
        threshold = null;
        each = null;
        smallestUnit = 0;
    }

    public DelayFormat(Object object) {

        // From a string. No translation, just a list of characters
        if (object instanceof String) {
            translation = null;
            threshold = null;
            each = null;
            charactersUsed = new boolean[DELAY_CHARACTERS.length];
            smallestUnit = determineCharactersUsed(object.toString(), null);
        }

        // Anything is possible
        else if (object instanceof ConfigurationSection) {
            ConfigurationSection config = (ConfigurationSection) object;
            charactersUsed = new boolean[DELAY_CHARACTERS.length];
            translation = new String[DELAY_CHARACTERS.length];
            threshold = config.getString("threshold");
            each = config.getString("each");
            smallestUnit = determineCharactersUsed(Objects.requireNonNull(config.getString("format"), "Could not find format"), config.getString("translate"));
        }

        // Error
        else throw new IllegalArgumentException("Provide either a config section or string");
    }

    private String[] loadTranslation(String input) {
        if (input == null) return null;

        if (input.contains(" ")) return input.split("\\ ");

        String[] stringArray = new String[input.length()];
        char[] charArray = input.toCharArray();
        for (int i = 0; i < charArray.length; i++)
            stringArray[i] = String.valueOf(charArray[i]);
        return stringArray;
    }

    private int determineCharactersUsed(@NotNull String chars, @Nullable String translation) {
        Validate.isTrue(!chars.isEmpty(), "Format cannot be empty");
        String[] translations = loadTranslation(translation);
        char[] tokens = chars.toCharArray();
        if (translations != null)
            Validate.isTrue(tokens.length == translations.length, "Format and translation don't have the same size");

        int smallest = DELAY_CHARACTERS.length - 1;
        int ref = 0;
        for (char token : tokens) {
            while (ref < DELAY_CHARACTERS.length && token != DELAY_CHARACTERS[ref]) ref++;
            if (ref >= DELAY_CHARACTERS.length)
                throw new IllegalArgumentException(String.format("Unknown token %s", token));
            charactersUsed[ref] = true;
            if (smallest > ref) smallest = ref; // Save lowest index

            // Save token translation
            if (translations != null) this.translation[ref] = translations[ref];
        }

        return smallest;
    }

    private String each(String stringToken, long value) {
        if (each == null) return value + stringToken + ' ';
        return String.format(each, value, stringToken);
    }

    public String format(long millis) {

        // Avoid displaying 0
        if (millis <= DELAY_AMOUNTS[smallestUnit])
            return threshold != null ? threshold : each(String.valueOf(DELAY_CHARACTERS[smallestUnit]), 1);

        // Offset to improve dynamic delay formatting
        millis += DELAY_AMOUNTS[smallestUnit] - 1;

        StringBuilder builder = new StringBuilder();
        for (int j = DELAY_CHARACTERS.length - 1; j >= 0; j--) {
            if (!charactersUsed[j]) continue; // Skip unused characters

            long divisor = DELAY_AMOUNTS[j];
            if (millis < divisor) continue;

            long quotient = millis / divisor;
            String stringToken = translation != null ? translation[j] : String.valueOf(DELAY_CHARACTERS[j]);
            builder.append(new StringBuilder(each(stringToken, quotient)).reverse());
            millis = millis % divisor;
        }

        return builder.reverse().toString();
    }
}
