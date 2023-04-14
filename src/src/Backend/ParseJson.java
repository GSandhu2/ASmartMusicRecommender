package Backend;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts data from Json objects.
 */
public class ParseJson {
    // "main" method.
    // bounds are "" for strings, [] for arrays, and {} for objects.
    private static String getJsonValue(String jsonObject, String key, String bounds) {
        int start = jsonObject.indexOf("\"" + key + "\"") + key.length() + 3;
        if (start == -1)
            throw new IllegalArgumentException("ParseJson: Json value not found - " + start);
        int end = findEnd(jsonObject, start, bounds);

        return jsonObject.substring(start, end);
    }

    // Finds the end of a json value.
    private static int findEnd(String jsonObject, int start, String bounds) {
        // Check types.
        if (jsonObject.charAt(start) != bounds.charAt(0))
            throw new IllegalArgumentException("ParseJson: Json type mismatch - " +
                    jsonObject.charAt(start) + " != " + bounds.charAt(0));

        // Find end of string value.
        if (bounds.charAt(1) == '\"') {
            int result = jsonObject.indexOf("\"", start + 1);
            if (result == -1)
                throw new IllegalArgumentException("ParseJson: Could not find end of string value.");
            return result;
        }

        // Find end of array or object value.
        int numBounds = 1;
        for (int i = start + 1; i < jsonObject.length(); i++) {
            if (jsonObject.charAt(i) == bounds.charAt(0))
                numBounds++;
            if (jsonObject.charAt(i) == bounds.charAt(1))
                numBounds--;
            if (numBounds == 0)
                return i;
        }

        throw new IllegalArgumentException("ParseJson: Could not find end of " +
                (bounds.charAt(0) == '[' ? "array" : "object") + " value.");
    }

    // Gets a json value key:{object} from a json object.
    public static String getJsonObject(String jsonObject, String key) {
        return getJsonValue(jsonObject, key, "{}");
    }

    // Gets a json value key:[array] from a json object.
    public static String[] getJsonArray(String jsonObject, String key) {
        String array = getJsonValue(jsonObject, key, "[]");
        List<String> result = new ArrayList<>();

        // Split array into elements.
        int start = 0, end = 0;
        int numBounds = 0;
        boolean inString = false;
        for (int i = 0; i < array.length(); i++) {
            // Found end of element.
            if (array.charAt(i) == ',' && numBounds == 0 && !inString) {
                result.add(array.substring())
            }
        }
    }

    // Gets a json value key:"string" from a json object.
    public static String getJsonString(String jsonObject, String key) {
        return getJsonValue(jsonObject, key, "\"\"");
    }

    private static String getJsonNumber(String jsonObject, String key) {
        return getJsonValue(jsonObject, key, "");
    }

    private static boolean getJsonBool(String jsonObject, String key) {
        return Boolean.parseBoolean(getJsonNumber(jsonObject, key));
    }

    // Gets a json value key:integer from a json object.
    public static int getJsonInt(String jsonObject, String key) {
        return Integer.parseInt(getJsonNumber(jsonObject, key));
    }

    // Gets a json value key.double from a json object.
    public static double getJsonDouble(String jsonObject, String key) {
        return Double.parseDouble(getJsonNumber(jsonObject, key));
    }
}
