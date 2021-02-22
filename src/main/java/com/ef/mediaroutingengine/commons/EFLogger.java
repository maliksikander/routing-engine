package com.ef.mediaroutingengine.commons;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component

/**
 *
 * Centralized logging using Spring AOP
 */
public class EFLogger {

    Logger logger = LoggerFactory.getLogger(EFLogger.class);

    @Pointcut(value = "execution(* com.ef.mediaroutingengine.controllers.*.*(..) )")
    public void appPointcut() {

    }

    @Around("appPointcut()")
    public Object topicManagerLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        ObjectMapper objectMapper = new ObjectMapper();
        String className = proceedingJoinPoint.getTarget().getClass().toString();
        String methodName = proceedingJoinPoint.getSignature().getName();
        Object[] args = proceedingJoinPoint.getArgs();
        logger.info(className + " : " + methodName + "() Invoked ");
        Object response = proceedingJoinPoint.proceed();
        logger.info(className + " : " + methodName + "() Response : " + objectMapper
                .writeValueAsString(response));
        return response;

    }


}