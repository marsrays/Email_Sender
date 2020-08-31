package email.service.component;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 紀錄每個 method 處理花費時長
 */
@Aspect
@Component
public class MethodInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodInterceptor.class);

    /**
     * AOP
     */
    @Pointcut("execution(public * email.service..*.*(..)) || execution(public * email.service.controller..*.*(..))")
    public void methodPoint(){}

    @Around("methodPoint()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        try {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            LOGGER.info("Method " + className + "." + methodName + " taken time : " + elapsedTime + " ms");

            return result;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Illegal argument " + Arrays.toString(joinPoint.getArgs()) + " in "
                + joinPoint.getSignature().getName() + "()");
            throw e;
        }
    }
}
