package at.alirezamoh.idea_whisperer_for_laravel.support.strUtil;

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
                .replace("'", "");
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
}
