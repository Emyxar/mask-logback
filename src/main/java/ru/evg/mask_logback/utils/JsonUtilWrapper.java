package ru.evg.mask_logback.utils;

public class JsonUtilWrapper {

    public String toJson(Object obj) {
        return LoggerUtil.toJson(obj);
    }
}
