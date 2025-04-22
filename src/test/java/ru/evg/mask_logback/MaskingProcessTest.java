package ru.evg.mask_logback;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import ru.evg.mask_logback.maskUtil.MaskProcessor;
import ru.evg.mask_logback.model.Employee;
import ru.evg.mask_logback.model.Passport;
import ru.evg.mask_logback.model.Response;
import ru.evg.mask_logback.model.Sex;
import ru.evg.mask_logback.utils.JsonUtilWrapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static ru.evg.mask_logback.utils.LoggerUtil.toJson;

@Slf4j
@SpringBootTest
public class MaskingProcessTest extends SpringBootContextLoader {

    @Autowired
    private MaskProcessor maskProcessor;

    @Autowired
    private JsonUtilWrapper jsonUtilWrapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test(){
        Response response = mockResponse(List.of(mockFirstEmployee()));
        assertThatCode(() -> maskProcessor.mask(response)).doesNotThrowAnyException();
    }

    @Test
    void test2(){
        Response response = mockResponse(List.of(mockFirstEmployee(), mockSecondEmployee(), mockThirdEmployee()));
        assertThatCode(() -> maskProcessor.mask(response)).doesNotThrowAnyException();
    }

    @Test
    void test3(){
        Response response = mockResponse(List.of(mockFirstEmployee(), mockSecondEmployee(), mockThirdEmployee()));
        String originalJson = toJson(response);
        String maskedJson = jsonUtilWrapper.toJson(response);

        log.info("Исходный JSON: {}", originalJson);
        log.info("Маскированный JSON: {}", maskedJson);

        Employee employee = response.getEmployee().get(0);
        String login = employee.getLogin() != null ? employee.getLogin() : "";

        assertThat(maskedJson).contains("*".repeat(login.length()));
    }

    @Test
    void test4(){
        Response response = mockResponse(List.of(mockFirstEmployee(), mockSecondEmployee(), mockThirdEmployee()));
        System.out.println(maskProcessor.mask(response));
    }

    private Response mockResponse(List<Employee> employees){
        return new Response().id(UUID.randomUUID()).employee(employees);
    }

    private Employee mockFirstEmployee(){
        Employee employee = new Employee().id(UUID.randomUUID());
        Passport passport = new Passport();

        passport.series(null);
        passport.number(BigDecimal.valueOf(987654321));
        passport.birthDate(LocalDate.now());
        passport.sex(Sex.MALE);
        passport.placeOfIssue("Место выдачи");

        employee.login("login");
        employee.lastActive(OffsetDateTime.now());
        employee.passport(passport);

        return employee;
    }

    private Employee mockSecondEmployee(){
        Employee employee = new Employee().id(UUID.randomUUID());
        Passport passport = new Passport();

        passport.series(BigDecimal.valueOf(1111));
        passport.number(BigDecimal.valueOf(1111111111));
        passport.birthDate(LocalDate.now());
        passport.sex(Sex.FEMALE);
        passport.placeOfIssue("Обычное место выдачи");

        employee.login("login_female");
        employee.lastActive(OffsetDateTime.now());
        employee.passport(passport);

        return employee;
    }

    private Employee mockThirdEmployee(){
        Employee employee = new Employee().id(UUID.randomUUID());
        Passport passport = new Passport();

        passport.series(BigDecimal.valueOf(2222));
        passport.number(BigDecimal.valueOf(222222222));
        passport.birthDate(LocalDate.now());
        passport.sex(Sex.MALE);
        passport.placeOfIssue("Необычное место выдачи");

        employee.login("login_male_boss");
        employee.lastActive(OffsetDateTime.now());
        employee.passport(passport);

        return employee;
    }
}
