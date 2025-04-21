package ru.evg.mask_logback.maskUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.stereotype.Component;
import ru.evg.mask_logback.maskUtil.finder.MaskingField;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static ru.evg.mask_logback.utils.LoggerUtil.toJson;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaskProcessor {

    public String mask(Object object) {
        String jsonObject = toJson(object);
        Map<String, Object> annotationFields = findAnnotationFields(object, new HashSet<>());

        try {
            JsonNode rootNode = objectMapper.readTree(jsonObject);
            maskRecursive(rootNode, annotationFields);

            return Optional.ofNullable(new GsonBuilder().serializeNulls().setPrettyPrinting().create()
                    .toJson(JsonParser.parseString(rootNode.toString()))).orElse("Пусто");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void maskRecursive(JsonNode node, Map<String, Object> annotationFields) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                if (annotationFields.containsKey(fieldName)) {
                    if (fieldValue.isTextual()) {
                        objectNode.put(fieldName, "*".repeat(fieldValue.asText().length()));
                    } else if (fieldValue.isNumber()) {
                        objectNode.put(fieldName, "*".repeat(fieldValue.toString().length()));
                    } else if (fieldValue.isNull()) {
                        objectNode.put(fieldName, "null");
                    } else {
                        objectNode.put(fieldName, "****");
                    }
                } else {
                    maskRecursive(fieldValue, annotationFields);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                maskRecursive(arrayItem, annotationFields);
            }
        }
    }

    private Map<String, Object> findAnnotationFields(Object object, Set<Integer> visited) {
        Map<String, Object> annotationFields = new HashMap<>();

        if (object == null) return annotationFields;

        int identity = System.identityHashCode(object);
        if (visited.contains(identity)) return annotationFields;

        visited.add(identity);

        Class<?> clazz = object.getClass();
        if (clazz.getName().startsWith("java.") || clazz.isPrimitive()) {
            return annotationFields;
        }

        for (Field field : clazz.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);
                String fieldName = field.getName();

                if (field.isAnnotationPresent(MaskingField.class) && isPrimitiveAndOtherType(fieldValue)) {
                    annotationFields.put(fieldName, fieldValue == null ? "null" : fieldValue);
                } else if (fieldValue != null) {
                    if (fieldValue instanceof Collection<?> collection) {
                        for (Object item : collection) {
                            annotationFields.putAll(findAnnotationFields(item, visited));
                        }
                    } else {
                        annotationFields.putAll(findAnnotationFields(fieldValue, visited));
                    }
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } finally {
                field.setAccessible(false);
            }
        }

        return annotationFields;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean isPrimitiveAndOtherType(Object object){
        return object == null || ClassUtils.isPrimitiveOrWrapper(object.getClass()) ||
                object instanceof String ||
                object instanceof Number ||
                object instanceof Enum ||
                object instanceof java.util.Date ||
                object instanceof java.time.temporal.Temporal ||
                object instanceof UUID ||
                object instanceof URL ||
                object instanceof URI ||
                object.getClass().getName().startsWith("java.lang.reflect.") ||
                object.getClass().getName().startsWith("java.util.") && !(object instanceof Collection || object instanceof Map);
    }
}