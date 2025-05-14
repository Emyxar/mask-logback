package ru.evg.mask_logback;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.evg.mask_logback.maskUtil.MaskProcessor;
import ru.evg.mask_logback.model.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static ru.evg.mask_logback.utils.LoggerUtil.toJson;

@Slf4j
public class MaskingProcessTest {

    private MaskProcessor maskProcessor;
    private MockData mockData;

    @BeforeEach
    void setUp() {
        mockData = new MockData();
        maskProcessor = new MaskProcessor();
    }

    @Test
    void test(){
        Response response = mockData.mockResponse(List.of(mockData.mockFirstEmployee()));
        assertThatCode(() -> maskProcessor.mask(response)).doesNotThrowAnyException();
    }

    @Test
    void test2(){
        Response response = mockData.mockResponse(List.of(mockData.mockFirstEmployee(), mockData.mockSecondEmployee()));
        logs(response);
        assertThatCode(() -> maskProcessor.mask(response)).doesNotThrowAnyException();
    }

    @Test
    void test3(){
        Response response = mockData.mockResponse(List.of(mockData.mockFirstEmployee(), mockData.mockSecondEmployee(), mockData.mockThirdEmployee()));
        logs(response);
        assertThatCode(() -> maskProcessor.mask(response)).doesNotThrowAnyException();
    }

    @Test
    void test4(){
        EmployeeTestDto employeeTestDto = new EmployeeTestDto();
        employeeTestDto.setId("test");
        employeeTestDto.setName("name");
        employeeTestDto.setData(Map.of("s", "s", "a", "a"));

        String originalJson = toJson(employeeTestDto);
        String maskedJson = maskProcessor.mask(employeeTestDto);

        log.info("Исходный JSON: {}", originalJson);
        log.info("Маскированный JSON: {}", maskedJson);

        assertThatCode(() -> maskProcessor.mask(employeeTestDto)).doesNotThrowAnyException();
    }


    @Test
    void test5(){
        Response response = mockData.mockResponse(List.of(mockData.mockFirstEmployee(), mockData.mockSecondEmployee(), mockData.mockThirdEmployee()));
        log.info("Данные {}", toJson(response));
        assertThatCode(() -> maskProcessor.mask(response)).doesNotThrowAnyException();
    }

    private void logs(Response response){
        String originalJson = toJson(response);
        String maskedJson = maskProcessor.mask(response);

        log.info("Исходный JSON: {}", originalJson);
        log.info("Маскированный JSON: {}", maskedJson);
    }
}
