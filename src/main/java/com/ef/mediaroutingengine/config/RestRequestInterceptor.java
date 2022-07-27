package com.ef.mediaroutingengine.config;

import com.ef.mediaroutingengine.global.commons.Constants;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
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

        if (StringUtils.isBlank(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(Constants.MDC_CORRELATION_ID, correlationId);

        response.addHeader(Constants.MDC_CORRELATION_ID, correlationId);

        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                @NotNull Object handler, Exception ex) {
        MDC.clear();
    }
}
