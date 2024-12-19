package at.alirezamoh.whisperer_for_laravel.support.strUtil;

import org.atteo.evo.inflector.English;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Provides utility methods for working with strings
 */
public class StrUtil {
    /**
     * Removes single and double quotes from a string
     * @param text The string to remove quotes from
     * @return     The string without quotes
     */
    public static String removeQuotes(String text) {
        return text.replace("\"", "")
                .replace("'", "").trim();
    }

    /**
     * Checks if a string is in camel case
     * @param str The string to check
     * @return    True if the string is in camel case, false otherwise
     */
    public static boolean isCamelCase(String str) {
        return str.matches(".*[A-Z].*");
    }

    public static String addSlashes(String text, boolean removeSlashFromStart, boolean removeSlashFromEnd) {
        if (text == null) {
            return text;
        }

        text = text.replaceAll("\\\\", "/");

        if (!text.startsWith("/")) {
            text = "/" + text;
        }

        if (!text.endsWith("/")) {
            text = text + "/";
        }

        if (removeSlashFromStart) {
            text = text.substring(1);
        }
        if (removeSlashFromEnd) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public static String addBackSlashes(String text, boolean removeSlashFromStart, boolean removeSlashFromEnd) {
        if (text == null) {
            return text;
        }

        text = text.replaceAll("/", "\\");

        if (!text.startsWith("\\")) {
            text = "\\" + text;
        }

        if (!text.endsWith("\\")) {
            text = text + "\\";
        }

        if (removeSlashFromStart) {
            text = text.substring(1);
        }
        if (removeSlashFromEnd) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public static String addSlashes(String text) {
        return addSlashes(text, false, false);
    }

    public static String removeExtension(String text) {
        if (text.endsWith(".php")) {
            text = text.substring(0, text.length() - 4);
        }
        return text;
    }

    public static String getLastWord(String text) {
        int lastSlashIndex = text.lastIndexOf('/');
        String afterSlash = text.substring(lastSlashIndex + 1);

        int lastSpaceIndex = afterSlash.lastIndexOf(' ');
        if (lastSpaceIndex == -1) {
            return afterSlash;
        }

        return afterSlash.substring(lastSpaceIndex + 1);
    }

    public static String snake(String value) {
        if (!value.equals(value.toLowerCase())) {
            value = value.replaceAll("([a-z])([A-Z])", "$1" + "_" + "$2").toLowerCase();
        }

        return value;
    }

    public static String snake(String value, String delimiter) {
        if (!value.equals(value.toLowerCase())) {
            value = value.replaceAll("([a-z])([A-Z])", "$1" + delimiter + "$2").toLowerCase();
        }

        return value;
    }

    public static String camel(String value) {
        String camelCaseValue = toStudlyCase(value);

        return Character.toLowerCase(camelCaseValue.charAt(0)) + camelCaseValue.substring(1);
    }

    public static String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");

        return currentDate.format(formatter);
    }

    public static String generateRandomId() {
        Random random = new Random();

        return String.format("%06d_", random.nextInt(1000000));
    }

    public static String plural(String text) {
        return English.plural(text);
    }

    public static String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String toLowerFirstLetter(String input) {
        char firstChar = input.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            return input;
        }
        return Character.toLowerCase(firstChar) + input.substring(1);
    }

    public static String removeDoubleSlashes(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.replaceAll("//+", "/");
    }

    private static String toStudlyCase(String value) {
        String[] words = value.split("[\\s_-]+");

        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }
}
