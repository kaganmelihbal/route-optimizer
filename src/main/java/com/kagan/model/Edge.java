package com.kagan.model;

/**
 * Represents a directional connection (edge) between two nodes in the routing graph.
 * Stores the destination ID instead of a nested City object to prevent JSON recursion.
 */
public class Edge {
    
    private int targetId;
    
    /**
     * The weight or cost of traversing this edge (e.g., distance in kilometers).
     */
    private int distance;

    /**
     * Constructs an immutable graph edge.
     * @param targetId The ID of the destination city.
     * @param distance The traversal cost to reach the destination.
     */
    public Edge(int targetId, int distance) {
        this.targetId = targetId;
        this.distance = distance;
    }

    public int getTargetId() { return targetId; }
    public int getDistance() { return distance; }
}