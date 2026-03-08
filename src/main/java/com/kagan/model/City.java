package com.kagan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a city node (vertex) in the routing graph.
 * Also holds the state required for pathfinding algorithms.
 */
public class City implements Comparable<City> {
    
    private int id;
    private String name;
    private double lat;
    private double lng;
    
    /**
     * Adjacency list representing outbound routes from this city.
     */
    private List<Edge> connections;

    /**
     * The shortest distance from the starting point to this city.
     * Ignored during JSON serialization.
     */
    @JsonIgnore
    private transient double minDistance = Double.MAX_VALUE;

    /**
     * The preceding city in the calculated shortest path.
     * Used to reconstruct the final route.
     */
    @JsonIgnore
    private transient City previousCity;

    public City(int id, String name) {
        this.id = id;
        this.name = name;
        this.connections = new ArrayList<>();
    }

    public City() {}

    /**
     * Adds a directed edge (route) to another city.
     * * @param targetId The ID of the destination city.
     * @param distance The distance (weight) of the route.
     */
    public void addConnection(int targetId, int distance) {
        if (connections == null) connections = new ArrayList<>();
        connections.add(new Edge(targetId, distance));
    }

    // --- Standard Domain Getters & Setters ---
    
    public int getId() { return id; }
    public String getName() { return name; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public List<Edge> getConnections() { return connections; }
    
    // --- Algorithmic State Getters & Setters ---
    
    @JsonIgnore
    public double getMinDistance() { return minDistance; }
    public void setMinDistance(double minDistance) { this.minDistance = minDistance; }
    
    @JsonIgnore
    public City getPreviousCity() { return previousCity; }
    public void setPreviousCity(City previousCity) { this.previousCity = previousCity; }

    /**
     * Compares cities by their minimum distance.
     * Used by the PriorityQueue to retrieve the closest node during graph traversal.
     */
    @Override
    public int compareTo(City other) {
        return Double.compare(this.minDistance, other.minDistance);
    }
    
    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}