package com.kagan.dto;

import com.kagan.model.City;
import java.util.List;

/**
 * Data Transfer Object (DTO) for the routing API response.
 * Encapsulates the algorithmic output (path) and business metrics (distance, cost) 
 * into a single JSON payload for the client.
 */
public class RouteResponse {
    
    
    // The ordered sequence of cities forming the route, from origin to destination.
    private List<City> itinerary;
    
    // The total physical distance of the route.
    private int totalDistance;
    
    //The estimated monetary cost of the trip.
    private double fuelCost;

    /**
     * @param itinerary     The ordered list of cities to traverse.
     * @param totalDistance The total calculated distance.
     * @param fuelCost      The computed fuel cost for the journey.
     */
    public RouteResponse(List<City> itinerary, int totalDistance, double fuelCost) {
        this.itinerary = itinerary;
        this.totalDistance = totalDistance;
        this.fuelCost = fuelCost;
    }

    public List<City> getItinerary() { return itinerary; }
    public int getTotalDistance() { return totalDistance; }
    public double getFuelCost() { return fuelCost; }
}