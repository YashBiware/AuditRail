package com.auditrail.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public final class CanonicalJsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CanonicalJsonUtil() {}

    public static String canonicalize(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty() || jsonString.trim().equals("{}")) {
            return "{}";
        }
        try {
            JsonNode rootNode = MAPPER.readTree(jsonString);
            return canonicalizeNode(rootNode);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON provided for canonicalization", e);
        }
    }

    private static String canonicalizeNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return "null";
        } else if (node.isObject()) {
            Map<String, JsonNode> sortedMap = new TreeMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                sortedMap.put(entry.getKey(), entry.getValue());
            }

            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, JsonNode> entry : sortedMap.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(escapeString(entry.getKey())).append(":").append(canonicalizeNode(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        } else if (node.isArray()) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (JsonNode item : node) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(canonicalizeNode(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        } else if (node.isTextual()) {
            return escapeString(node.asText());
        } else if (node.isNumber()) {
            return node.numberValue().toString();
        } else if (node.isBoolean()) {
            return node.asBoolean() ? "true" : "false";
        }
        return node.toString();
    }

    private static String escapeString(String s) {
        try {
            return MAPPER.writeValueAsString(s);
        } catch (JsonProcessingException e) {
            return "\"" + s.replace("\"", "\\\"") + "\"";
        }
    }
}
