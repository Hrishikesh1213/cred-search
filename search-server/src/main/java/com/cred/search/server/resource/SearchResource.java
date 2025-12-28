package com.cred.search.server.resource;

import com.cred.search.core.manager.ISearchManager;
import com.cred.search.models.request.ItinerarySearchRequest;
import com.cred.search.models.response.ItinerarySearchResponse;
import com.cred.search.models.response.wrapper.ApiResponse;
import com.cred.search.server.handler.ApiResponseHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search/v1")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class SearchResource {

    private final ISearchManager searchManager;

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ItinerarySearchResponse>> findFlights(@RequestBody @Valid ItinerarySearchRequest request) {
        return ApiResponseHandler.handle("findFlights", request, () -> searchManager.findFlights(request));
    }

}