package at.alirezamoh.whisperer_for_laravel.translation.util;

import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.json.JsonUtil;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for flattening nested JSON translation files into dot-notation key-value pairs
 */
public class TranslationJsonFlattener {
    /**
     * Parses a JSON translation file and converts it to a flattened map
     *
     * @param jsonFile The JSON file to parse
     * @param forModule is a package translation file
     * @param namespace the namespace for the translation keys
     * @return A map of dot-notation keys to their corresponding values, or null if parsing fails
     */
    public static @Nullable Map<String, String> parseJsonTranslation(JsonFile jsonFile, boolean forModule, String namespace) {
        JsonObject topLevel = JsonUtil.getTopLevelObject(jsonFile);
        if (topLevel == null) {
            return null;
        }

        Map<String, String> result = new HashMap<>();
        flattenJsonObject(jsonFile, "", topLevel, result, forModule, namespace);
        return result;
    }

    /**
     * Recursively flattens a JSON object into dot-notation keys
     *
     * @param jsonObject The JSON object to flatten
     * @param prefix The current key prefix (for nested objects)
     * @param result The map to store the flattened key-value pairs
     * @param forModule is a package translation file
     * @param namespace the namespace for the translation keys
     */

    private static void flattenJsonObject(JsonFile jsonFile, String prefix, JsonObject jsonObject, Map<String, String> result, boolean forModule, String namespace) {
        for (JsonProperty property : jsonObject.getPropertyList()) {
            String key = property.getName();

            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            PsiElement valueElement = property.getValue();

            if (valueElement instanceof JsonObject nestedObject) {
                flattenJsonObject(jsonFile, fullKey, nestedObject, result, forModule, namespace);
            } else if (valueElement != null) {
                if (forModule) {
                    result.put(
                        namespace + "::" + fullKey,
                        StrUtils.removeQuotes(valueElement.getText()) + "|" + jsonFile.getVirtualFile().getPath()
                    );
                }
                else {
                    result.put(fullKey, StrUtils.removeQuotes(valueElement.getText()));
                }
            }
        }
    }
}
