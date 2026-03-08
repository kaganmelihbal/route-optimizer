package com.kagan.controller;

import com.kagan.dto.RouteResponse;
import com.kagan.model.City;
import com.kagan.service.GraphService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller responsible for exposing the graph routing capabilities to client applications.
 * Orchestrates the translation of string-based geographic queries into complex pathfinding
 * operations and appends business-specific metrics (like fuel costs) to the algorithmic results.
 */
@RestController
@RequestMapping("/api/route")
public class RouteController {

    private final GraphService graphService;

    /**
     * @param graphService The service layer handling graph-based city data operations.
     */
    public RouteController(GraphService graphService) {
        this.graphService = graphService;
    }

    /**
     * Calculates the shortest path between two points, optionally forcing the route through a specific waypoint.
     * Also computes the estimated financial cost of the trip based on vehicle consumption metrics.
     * * Architectural Note: 
     * If no waypoint is provided, this triggers a single execution of Dijkstra's algorithm.
     * If a waypoint ('via') is provided, the algorithm runs twice (Start -> Via, then Via -> End). 
     * While this doubles the execution time constant, the overall asymptotic time complexity 
     * remains bounded by the underlying graph traversal implementation (e.g., O(V log V + E)).
     * @param from        Origin city name.
     * @param to          Destination city name.
     * @param via         Optional waypoint city name to route through.
     * @param consumption Vehicle fuel consumption metric (assumed liters per 100km).
     * @param fuelPrice   Cost per unit of fuel.
     * @return ResponseEntity containing a RouteResponse DTO on success, or an error string on bad input.
     */
    @GetMapping("/calculate")
    public ResponseEntity<?> calculate(
            @RequestParam String from, 
            @RequestParam String to,
            @RequestParam(required = false) String via,
            @RequestParam(defaultValue = "0") double consumption,
            @RequestParam(defaultValue = "0") double fuelPrice) {

        City start = graphService.findCityByName(from);
        City end = graphService.findCityByName(to);

        // Fail-fast validation: Ensure core routing nodes exist in the graph index before proceeding.
        if (start == null || end == null) {
            return ResponseEntity.badRequest().body("Error: City not found.");
        }

        int totalDistance;
        List<City> fullItinerary;

        // Partitioned routing logic: Handle the presence of a mandatory midpoint.
        if (via != null && !via.trim().isEmpty()) {
            City midpoint = graphService.findCityByName(via);
            if (midpoint == null) return ResponseEntity.badRequest().body("Hata: Intermediate stop not found.");

            // Segment 1: Origin to Waypoint
            graphService.calculateShortestPath(start);
            List<City> firstLeg = new ArrayList<>(graphService.getPath(midpoint));
            int firstDist = (int) midpoint.getMinDistance();

            // Segment 2: Waypoint to Destination
            // Note: The graph service mutates internal state during calculation, 
            // so this second call effectively overwrites the distance data from the first leg.
            graphService.calculateShortestPath(midpoint);
            List<City> secondLeg = graphService.getPath(end);
            int secondDist = (int) end.getMinDistance();

            // Aggregation: Combine the two segments into a single contiguous route.
            totalDistance = firstDist + secondDist;
            fullItinerary = new ArrayList<>(firstLeg);
            
            // Start iteration at index 1 to prevent duplicating the midpoint in the final itinerary.
            for (int i = 1; i < secondLeg.size(); i++) {
                fullItinerary.add(secondLeg.get(i));
            }
        } else {
            // Standard point-to-point routing using a single algorithm pass.
            graphService.calculateShortestPath(start);
            List<City> path = graphService.getPath(end);
            
            totalDistance = (int) end.getMinDistance();
            fullItinerary = path;
        }

        // Business Logic: Compute estimated trip cost. 
        // Formula assumes standard L/100km metric (Distance / 100 * Consumption * Price).
        double fuelCost = (totalDistance / 100.0) * consumption * fuelPrice;
        
        // Round to two decimal places to enforce standard currency/fiat formatting and prevent floating-point drift.
        fuelCost = Math.round(fuelCost * 100.0) / 100.0;

        return ResponseEntity.ok(new RouteResponse(fullItinerary, totalDistance, fuelCost));
    }
}