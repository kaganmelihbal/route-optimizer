# Route Optimizer API

## Overview
This project is a RESTful web service built with **Spring Boot** that provides optimal routing solutions for urban networks. It utilizes a **graph-based infrastructure** to represent cities and connections, implementing **Dijkstra's Single-Source Shortest Path algorithm** for precise calculations. The system also incorporates business logic to estimate fuel costs based on vehicle consumption metrics.

---

## Core Technical Features

### Algorithmic Routing
Implements **Dijkstra's algorithm** using a **Priority Queue** to ensure efficient pathfinding.

### Intermediate Waypoint Support
Supports partitioned routing logic allowing users to specify a mandatory **"via" city**, which executes the shortest path algorithm in multiple segments while maintaining **O(V log V + E)** complexity.

### Robust Search Indexing
Features a **dual-key search index** with custom normalization to handle **Turkish-specific characters and diacritics**, ensuring **O(1)** average-time complexity for city lookups.

### In-Memory Graph Representation
Loads topological data from **JSON files at startup** into **memory-resident adjacency lists** for low-latency performance.

### Standardized API Responses
Utilizes **Data Transfer Objects (DTOs)** to provide structured **JSON payloads** containing itinerary details, total distance, and calculated fuel costs.

---

## Architecture

The application follows a standard **N-tier architecture**:

### Controller Layer
Manages REST endpoints and validates input parameters.

### Service Layer
Handles the business logic, graph traversal, and shortest path calculations.

### Data Model
Defines the graph structure through **City (Vertex)** and **Edge** objects.

---

## Installation and Execution

### Prerequisites
- Java Development Kit (JDK) **17 or higher**
- **Maven 3.x**

### Steps

#### Clone the Repository
```bash
git clone https://github.com/kaganmelihbal/route-optimizer
```

#### Build the Project
```bash
mvn clean install
```

#### Run with Maven Wrapper
```bash
./mvnw spring-boot:run
```

---

## API Specification

### Calculate Route

**GET**
```
/api/route/calculate
```

#### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| from | String | Yes | Origin city name |
| to | String | Yes | Destination city name |
| via | String | No | Intermediate city name |
| consumption | double | No | Liters per 100 km |
| fuelPrice | double | No | Cost per unit of fuel |

---

## Example Response

```json
{
  "itinerary": [
    { "id": 1, "name": "Adana", "lat": 37.0, "lng": 35.3213 },
    { "id": 33, "name": "Mersin", "lat": 36.8, "lng": 34.6333 },
    { "id": 51, "name": "Niğde", "lat": 37.9667, "lng": 34.6833 }
  ],
  "totalDistance": 270,
  "fuelCost": 27.0
}
```

---

## Testing

The system includes **comprehensive unit and integration tests**. Tests utilize a localized **test-cities.json dataset** to verify algorithmic correctness and API behavior.

```bash
mvn test
```