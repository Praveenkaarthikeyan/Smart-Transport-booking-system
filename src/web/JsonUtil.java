package web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small, hand-written JSON helper. No external JSON library is used,
 * to keep the project free of any build tool (Maven/Gradle) or dependency.
 *
 * toJson() supports Strings, Numbers, Booleans, null, Map<String,Object>
 * and List<Object> - enough to build every response this project sends.
 *
 * parseFlatObject() reads a simple, non-nested JSON object (the only
 * kind our forms ever send) into a Map<String,String>.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String s) {
            return "\"" + escape(s) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":")
                        .append(toJson(entry.getValue()));
            }
            return sb.append("}").toString();
        }
        if (value instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(toJson(item));
            }
            return sb.append("]").toString();
        }
        return "\"" + escape(value.toString()) + "\"";
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Parses a flat (non-nested) JSON object into a Map<String,String>.
     * Example input: {"name":"Rahul Sharma","age":28,"gender":"Male"}
     * This is deliberately simple - it only needs to handle the small
     * request bodies this project's own frontend sends.
     */
    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null) {
            return map;
        }
        int i = 0;
        int n = json.length();
        while (i < n) {
            while (i < n && json.charAt(i) != '"') {
                i++;
            }
            if (i >= n) {
                break;
            }
            i++; // skip opening quote of key
            StringBuilder key = new StringBuilder();
            while (i < n && json.charAt(i) != '"') {
                key.append(json.charAt(i));
                i++;
            }
            i++; // skip closing quote of key
            while (i < n && json.charAt(i) != ':') {
                i++;
            }
            i++; // skip colon
            while (i < n && Character.isWhitespace(json.charAt(i))) {
                i++;
            }
            StringBuilder value = new StringBuilder();
            if (i < n && json.charAt(i) == '"') {
                i++; // skip opening quote of value
                while (i < n && json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\' && i + 1 < n) {
                        i++;
                    }
                    value.append(json.charAt(i));
                    i++;
                }
                i++; // skip closing quote
            } else {
                while (i < n && json.charAt(i) != ',' && json.charAt(i) != '}') {
                    value.append(json.charAt(i));
                    i++;
                }
            }
            map.put(key.toString(), value.toString().trim());
        }
        return map;
    }
}