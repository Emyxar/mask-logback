package ru.evg.mask_logback;

import lombok.Data;
import ru.evg.mask_logback.annotations.MaskingField;

import java.util.Map;

@Data
public class EmployeeTestDto {
    private String id;
    private String name;
    @MaskingField
    private Map<String, String> data;
}
