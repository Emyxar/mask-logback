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
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Component;
import ru.evg.mask_logback.annotations.MaskingField;

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
        log.info("Получен объект {} и отформатирован в JSON {}", object.getClass(), jsonObject);

        Set<Field> annotatedFields = searchForAnnotatedFieldsForMasking(object);
        annotatedFields.forEach(i -> log.info("Найдено поле {}", i));

        try {
            JsonNode rootNode = new ObjectMapper().readTree(jsonObject);
            maskingJson(rootNode, object, annotatedFields);

            return Optional.ofNullable(new GsonBuilder().serializeNulls().setPrettyPrinting().create()
                    .toJson(JsonParser.parseString(rootNode.toString()))).orElse("Пусто");
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    private void maskingJson(JsonNode node, Object currentObject, Set<Field> fields) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fieldsIterator = objectNode.fields();

            while (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                try {
                    Field field = currentObject.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);

                    if (fields.contains(field)){
                        if (fieldValue.isNull()){
                            objectNode.put(fieldName, "null");
                        } else {
                            objectNode.put(fieldName, "*".repeat(fieldValue.asText().length()));
                        }
                    }

                    Object nestedObject = field.get(currentObject);
                    if (nestedObject != null && (fieldValue.isObject() || fieldValue.isArray())) {
                        maskingJson(fieldValue, nestedObject, fields);
                    }

                } catch (NoSuchFieldException | IllegalAccessException e){
                    continue;
                }
            }
        } else if (node.isArray()) {
            if (currentObject instanceof Collection<?>) {
                Iterator<JsonNode> elements = node.elements();
                Iterator<?> objs = ((Collection<?>) currentObject).iterator();
                while (elements.hasNext() && objs.hasNext()) {
                    maskingJson(elements.next(), objs.next(), fields);
                }
            }
        }
    }

    private Set<Field> searchForAnnotatedFieldsForMasking(Object object) {
        Set<Field> fields = new HashSet<>();

        Class<?> clazz = object.getClass();

        for(Field field : FieldUtils.getAllFieldsList(clazz)) {
            try {
                field.setAccessible(true);
                if(isPrimitiveAndOtherType(field.get(object)) && field.isAnnotationPresent(MaskingField.class)) {
                    fields.add(field);
                } else if (field.get(object) != null){
                    if (field.get(object) instanceof Collection<?> collection) {
                        for (Object item : collection) {
                            fields.addAll(searchForAnnotatedFieldsForMasking(item));
                        }
                    } else if (!isPrimitiveAndOtherType(field.get(object))){
                        fields.addAll(searchForAnnotatedFieldsForMasking(field.get(object)));
                    }
                }
            } catch (IllegalAccessException e){
                throw new RuntimeException(e);
            } finally {
                field.setAccessible(false);
            }
        }
        return fields;
    }

    private boolean isPrimitiveAndOtherType(Object object){
        return object == null ||
                ClassUtils.isPrimitiveOrWrapper(object.getClass()) ||
                object instanceof String ||
                object instanceof Number ||
                object instanceof Enum ||
                object instanceof java.util.Date ||
                object instanceof java.time.temporal.Temporal ||
                object instanceof UUID ||
                object instanceof URL ||
                object instanceof URI ||
                object.getClass().getName().startsWith("java.lang.reflect.") ||
                (object.getClass().getName().startsWith("java.util.") && !(object instanceof Collection || object instanceof Map));
    }
}