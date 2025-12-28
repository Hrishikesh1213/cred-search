package com.cred.search.server.resource;

import com.cred.search.core.manager.IInventoryManager;
import com.cred.search.core.manager.ISearchManager;
import com.cred.search.models.commons.Route;
import com.cred.search.models.request.PhysicalRouteRequest;
import com.cred.search.models.response.wrapper.ApiResponse;
import com.cred.search.server.handler.ApiResponseHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/inventory/v1")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class InventoryResource {

    private final IInventoryManager inventoryManager;
    private final ISearchManager searchManager;

    @PostMapping(value = "/addPhysicalRoute", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Route>> addPhysicalRoute(@RequestBody @Valid PhysicalRouteRequest request) {
        return ApiResponseHandler.handle("addPhysicalRoute", request, () -> {
            Route route = inventoryManager.addPhysicalRoute(request);
            try {
                searchManager.recalculateRoutes(Collections.singletonList(route));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return route;
        });
    }

}