package com.kagan.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kagan.model.City;
import com.kagan.service.GraphService;

import java.util.List;

/**
 * REST Controller responsible for exposing urban data and topological information.
 * Acts as the entry point for clients to interact with city-related datasets
 * managed by the underlying graph infrastructure.
 */
@RestController
public class CitiesController {

    private final GraphService graphService;

    /**
     * @param graphService The service layer handling graph-based city data operations.
     */
    public CitiesController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping(value = "/cities.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<City>> getCities() {
        return ResponseEntity.ok(graphService.getCities());
    }
}
