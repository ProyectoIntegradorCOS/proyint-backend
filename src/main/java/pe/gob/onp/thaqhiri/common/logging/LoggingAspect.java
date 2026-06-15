package pe.gob.onp.thaqhiri.common.logging;

import lombok.extern.slf4j.Slf4j;
import pe.gob.onp.thaqhiri.service.LocationService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
	private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(public * com.locationtracker..service..*(..)) || " +
            "execution(public * com.locationtracker..*Repository.*(..))")
    public Object logAroundServiceAndRepository(ProceedingJoinPoint pjp) throws Throwable {
        if (!log.isDebugEnabled()) {
            return pjp.proceed();
        }

        String method = pjp.getSignature().toShortString();
        String args = formatArgs(pjp.getArgs());
        long start = System.currentTimeMillis();
        log.debug(">> {} args={}", method, args);
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.debug("<< {} ok elapsed={}ms", method, elapsed);
            return result;
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("!! {} failed elapsed={}ms cause={}", method, elapsed, ex.toString());
            throw ex;
        }
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.stream(args)
                .map(this::stringify)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String stringify(Object arg) {
        if (arg == null) return "null";
        String value = String.valueOf(arg);
        return value.length() > 150 ? value.substring(0, 147) + "..." : value;
    }
}
