package at.alirezamoh.whisperer_for_laravel.support.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class StrUtils {
    /**
     * Removes single and double quotes from a string
     * @param text The string to remove quotes from
     * @return     The string without quotes
     */
    public static String removeQuotes(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.replace("\"", "")
            .replace("'", "").trim();
    }

    /**
     * Converts a string with a specified delimiter into camelCase
     *
     * @param text      The input string to convert
     * @param delimiter The character delimiter used to split words
     * @return A camelCase version of the input string (e.g., "helloWorld"), or an empty string if the input is null or empty
     */
    public static String camel(@Nullable String text, char delimiter) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String[] words = StringUtils.split(text.toLowerCase(), delimiter);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {

                String word = words[i];
                if (i == 0) {
                    result.append(StringUtils.lowerCase(word));
                }
                else {
                    result.append(StringUtils.capitalize(word));
                }
            }
        }

        return result.toString();
    }

    /**
     * Converts a camelCase or PascalCase string into snake_case
     *
     * @param text The input string to convert
     * @return A snake_case version of the input string (e.g., "hello_world"), or an empty string if the input is null or empty
     */
    public static String snake(@Nullable String text, String delimiter) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String capitalizedWord = StringUtils.capitalize(text);

        return capitalizedWord
            .replaceAll("([a-z])([A-Z])", "$1" + delimiter + "$2")
            .replaceAll("\\s+", delimiter)
            .toLowerCase();
    }

    /**
     * Capitalizes the first character of the string
     *
     * @param text The input string
     * @return The string with the first character capitalized (e.g., "Hello"), or an empty string if the input is null or empty
     */
    public static String ucFirst(@Nullable String text) {
        if(text == null || text.isEmpty()) {
            return "";
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Converts the first character of the input string to lowercase
     *
     * @param text The input string
     * @return The string with the first character in lowercase (e.g., "hello"), or an empty string if the input is null or empty
     */
    public static String lcFirst(@Nullable String text) {
        if(text == null || text.isEmpty()) {
            return "";
        }

        return Character.toLowerCase(text.charAt(0)) + (text.length() > 1 ? text.substring(1) : "");
    }

    /**
     * Converts double forward slashes (except leading ones) to a single forward slash
     * and double backslashes to a single backslash.
     *
     * @param text The text
     * @return A string where double forward slashes are converted to single slashes
     * or an empty string if the input is null or empty
     */
    public static String removeDoubleSlashes(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        text = text.replaceAll("/+", "/");
        text = text.replaceAll("\\\\+", "\\");

        return text;
    }

    /**
     * Converts double forward slashes (except leading ones) to a single forward slash
     *
     * @param input The input string
     * @return A string where double forward slashes are converted to a single slash,
     *         preserving leading double slashes (e.g., "example.com/path/to/file")
     */
    public static String removeDoubleForwardSlashes(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        return input.replaceAll("//+", "/");
    }

    /**
     * Converts double backslashes into single backslashes
     *
     * @param input The input string
     * @return A string where double backslashes are converted to single backslashes
     *         (e.g., "C:\\Users\\Documents"), or null if the input is null
     */
    public static String removeDoubleBackSlashes(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        return input.replaceAll("\\\\{2,}", "\\");
    }

    /**
     * Replaces all backslashes in the text with forward slashes, ensures the text starts
     * and ends with a forward slash, and optionally removes slashes from the start or end
     *
     * @param text The string
     * @param removeSlashFromStart If true, removes the leading slash after ensuring it starts with one
     * @param removeSlashFromEnd If true, removes the trailing slash after ensuring it ends with one
     * @return The modified text with slashes adjusted as specified
     * <p>
     * Example:
     * Input: "example\\path"
     * Output: "/example/path/" (default behavior)
     */
    public static String addSlashes(String text, boolean removeSlashFromStart, boolean removeSlashFromEnd) {
        if (text == null || text.isEmpty()) {
            return "";
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

    /**
     * Adjusts slashes in the text without removing the starting or ending slash
     *
     * @param text The string
     * @return The modified text with slashes adjusted
     * <p>
     * Example:
     * Input: "example\\path"
     * Output: "/example/path/"
     */
    public static String addSlashes(String text) {
        return addSlashes(text, false, false);
    }

    /**
     * Replaces all forward slashes in the text with backslashes, ensures the text starts
     * and ends with a backslash, and optionally removes backslashes from the start or end
     *
     * @param text The input text.
     * @param removeSlashFromStart If true, removes the leading backslash after ensuring it starts with one
     * @param removeSlashFromEnd If true, removes the trailing backslash after ensuring it ends with one
     * @return The modified text with backslashes adjusted as specified
     * <p>
     * Example:
     * Input: "example/path"
     * Output: "\\example\\path\\" (default behavior)
     */
    public static String addBackSlashes(String text, boolean removeSlashFromStart, boolean removeSlashFromEnd) {
        if (text == null || text.isEmpty()) {
            return "";
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

    /**
     * Removes the ".php" extension from the text if it exists
     *
     * @param text The input text
     * @return The text without the ".php" extension
     */
    public static String removePhpExtension(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        if (text.endsWith(".php")) {
            text = text.substring(0, text.length() - 4);
        }
        return text;
    }

    /**
     * Returns the current date formatted as "yyyy_MM_dd"
     *
     * @return The current date as a string
     * <p>
     * Example:
     * If today is January 5, 2025:
     * Output: "2025_01_05"
     */
    public static String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");

        return currentDate.format(formatter);
    }

    /**
     * Generates a random 6-digit ID with a trailing underscore
     *
     * @return A randomly generated 6-digit ID as a string
     * <p>
     * Example:
     * Output: "483920_"
     */
    public static String generateRandomId() {
        Random random = new Random();

        return String.format("%06d_", random.nextInt(1000000));
    }

    /**
     * Checks if a string is in camel case
     * @param str The string to check
     * @return    True if the string is in camel case, false otherwise
     */
    public static boolean isCamelCase(String str) {
        return str.matches(".*[A-Z].*");
    }
}
