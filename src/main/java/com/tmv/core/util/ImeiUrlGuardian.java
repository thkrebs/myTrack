package com.tmv.core.util;

import com.tmv.core.service.ImeiValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Slf4j
@Component
public class ImeiUrlGuardian implements HandlerInterceptor {

    @Autowired
    public final ImeiValidationService imeiValidationService;

    public ImeiUrlGuardian(ImeiValidationService imeiValidationService) {
        this.imeiValidationService = imeiValidationService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        final Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        log.debug("pre-handle pathVariables: {}", pathVariables);
        checkUrlVariables(pathVariables);
        return true;
    }

    protected void checkUrlVariables(Map<String, String> pathVariables) {
        if (shouldCheckImei(pathVariables)) {
            checkAccess(pathVariables.get("imei"));
        }
    }

    private boolean shouldCheckImei(Map<String, String> parameters) {
        return parameters.containsKey("imei");
    }

    void checkAccess(String imei) {
        if (!imeiValidationService.isActive(imei)) {
            throw new AccessViolationException("Inactive or unknown IMEI");
        }
    }

}
