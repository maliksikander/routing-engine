package com.ef.mediaroutingengine.config;

import com.ef.mediaroutingengine.commons.Constants;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * The type Request correlation.
 */
public class RestRequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                             @NotNull Object handler) {

        String correlationId = request.getHeader(Constants.MDC_CORRELATION_ID);

        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(Constants.MDC_CORRELATION_ID, correlationId);

        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                @NotNull Object handler, Exception ex) {
        String correlationId = MDC.get(Constants.MDC_CORRELATION_ID);
        response.addHeader(Constants.MDC_CORRELATION_ID, correlationId);
//        MDC.remove(FIELD_NAME);
        MDC.clear();
    }
}
