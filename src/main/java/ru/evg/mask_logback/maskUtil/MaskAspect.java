package ru.evg.mask_logback.maskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MaskAspect {

    private final MaskProcessor maskProcessor;

    @Before("execution(* org.slf4j.Logger.info(..) && args(message, obj)")
    public void logAfter(JoinPoint joinPoint, String message, Object obj){
        String mask = maskProcessor.mask(obj);
        log.info(message, mask);
    }
}
