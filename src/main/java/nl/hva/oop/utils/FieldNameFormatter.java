package nl.hva.oop.utils;

/**
 * This class provides a static method to convert a CSV field name to a human-readable format.
 * Uses Dutch grammar rules to format the field names.
 * Makes everything lowercase and inserts spaces before each uppercase character.
 * Handles special cases like "'s avonds", "'s-hertogenbosch", and leading invalid characters.
 * And by default, it capitalises on the first character of the field name.
 *
 * @author Remzi Cavdar - ict@remzi.info - <a href="https://remzi.info">remzi.info</a>
 */
public class FieldNameFormatter {
    public static String toHumanReadable(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Trim leading and trailing spaces
        input = input.trim();

        // Insert spaces before each uppercase character
        StringBuilder spaced = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                spaced.append(' ');
            }
            spaced.append(c);
        }

        // Convert everything to lowercase
        String lower = spaced.toString().toLowerCase();

        // Special Dutch grammar rule: Handle cases starting with "'s"
        if (lower.startsWith("'s ")) {
            // Example: "'s avonds"
            if (lower.length() > 3) {
                char afterSpace = Character.toUpperCase(lower.charAt(3));
                lower = "'S " + afterSpace + lower.substring(4);
            } else {
                lower = "'S";
            }

        } else if (lower.startsWith("'s-")) {
            // Example: "'s-hertogenbosch"
            if (lower.length() > 3) {
                char afterDash = Character.toUpperCase(lower.charAt(3));
                lower = "'S-" + afterDash + lower.substring(4);
            } else {
                lower = "'S-";
            }

        } else {
            // Handle leading invalid or empty characters
            int firstValidCharIndex = -1;
            for (int i = 0; i < lower.length(); i++) {
                if (Character.isLetterOrDigit(lower.charAt(i))) {
                    firstValidCharIndex = i;
                    break;
                }
            }

            if (firstValidCharIndex != -1) {
                lower = lower.substring(0, firstValidCharIndex) +
                        lower.substring(firstValidCharIndex, firstValidCharIndex + 1).toUpperCase() +
                        lower.substring(firstValidCharIndex + 1);
            }
        }

        return lower;
    }
}