package com.cred.search.server.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Component
public class ApiEndpointLogger implements ApplicationListener<ApplicationReadyEvent> {

    private final List<RequestMappingHandlerMapping> handlerMappings;

    public ApiEndpointLogger(List<RequestMappingHandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        handlerMappings.forEach(
                handlerMapping -> handlerMapping.getHandlerMethods().forEach((key, method) -> {
            String endpointMethods = key.getMethodsCondition().getMethods().stream()
                    .map(RequestMethod::name)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("GET");

                    assert key.getPathPatternsCondition() != null;
                    String endpointPaths = key.getPathPatternsCondition().getPatternValues().toString();

            System.out.println("\t" + endpointMethods + " " + endpointPaths);
        }));
    }
}