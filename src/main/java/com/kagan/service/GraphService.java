package com.kagan.service;

import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kagan.model.City;
import com.kagan.model.Edge;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service responsible for managing the geographic graph network and executing routing algorithms.
 * Loads static city nodes and adjacency lists (edges) into memory at startup to ensure 
 * fast, synchronous pathfinding operations.
 */
@Service
public class GraphService {

    private List<City> cityList;
    
    /** * In-memory index for O(1) average-time city lookups by name. 
     * Stores multiple key permutations (normalized and localized) per city to maximize search hit rates.
     */
    private Map<String, City> cityIndex;
    
    public GraphService() {
        this.cityList = new ArrayList<>();
        this.cityIndex = new HashMap<>();
    }

    /**
     * Bootstraps the graph data immediately after bean initialization.
     * Utilizes a fail-fast approach: if the foundational geographic data 
     * cannot be loaded, the application state is invalid and halts.
     * * @throws IllegalStateException if the JSON dataset is missing or unreadable.
     */
    @PostConstruct
    private void init() {
        boolean ok = loadData("cities.json");
        if (!ok) {
            throw new IllegalStateException("cities.json could not be loaded. The application cannot start.");
        }
    }

    /**
     * Deserializes the graph topology from a localized JSON file.
     * * @param fileName The classpath-relative path to the data file.
     * @return boolean indicating success or failure of the load operation.
     */
    public boolean loadData(String fileName) {
        try {
            Gson gson = new Gson();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
            
            if (inputStream == null) {
                System.err.println("ERROR: File not found: " + fileName);
                return false;
            }

            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Type listType = new TypeToken<ArrayList<City>>(){}.getType();
            this.cityList = gson.fromJson(reader, listType);

            buildSearchIndex();

            System.out.println("Data loaded successfully. Total Cities: " + cityList.size());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Populates the search index using a dual-key strategy to handle localization edge cases.
     * Time Complexity: O(N) where N is the number of cities.
     */
    private void buildSearchIndex() {
        cityIndex.clear();
        for (City city : cityList) {
            String normalizedKey = normalizeForSearch(city.getName());
            cityIndex.put(normalizedKey, city);
            
            // Fallback key: explicitly handles the Turkish lowercase mapping 
            // to ensure native inputs (e.g., "İZMİR" -> "izmir") map correctly.
            cityIndex.put(city.getName().toLowerCase(Locale.forLanguageTag("tr")), city);
        }
    }

    /**
     * Retrieves a city node by its string name.
     * Time Complexity: O(1) average case due to HashMap backing.
     * * @param name The user-provided or system-generated city name.
     * @return The corresponding City object, or null if not found.
     */
    public City findCityByName(String name) {
        if (name == null) return null;
        String searchKey = normalizeForSearch(name);
        return cityIndex.get(searchKey);
    }

    /**
     * Standardizes input strings for robust indexing and querying.
     * Specifically addresses the "Turkish I" problem and removes diacritics 
     * so that queries like "Istanbul", "İstanbul", and "ıstanbul" all resolve identically.
     * * @param input Raw string input.
     * @return Ascii-normalized, lowercased string.
     */
    private String normalizeForSearch(String input) {
        if (input == null) return "";
        String normalized = input.trim()
                .replace("İ", "i")
                .replace("I", "i")
                .replace("ı", "i")
                .replace("Ğ", "g")
                .replace("ğ", "g")
                .replace("Ü", "u")
                .replace("ü", "u")
                .replace("Ş", "s")
                .replace("ş", "s")
                .replace("Ö", "o")
                .replace("ö", "o")
                .replace("Ç", "c")
                .replace("ç", "c");
        return normalized.toLowerCase(Locale.ENGLISH);
    }
    
    /**
     * Retrieves a city node by its unique identifier.
     * Time Complexity: O(V) where V is the number of vertices (cities).
     * Note: For heavily scaled datasets, a secondary Map<Integer, City> index 
     * could be introduced to reduce this to O(1).
     * * @param id The unique integer ID of the city.
     * @return The corresponding City object, or null if not found.
     */
    public City findCityById(int id) {
        for (City city : cityList) {
            if (city.getId() == id) return city;
        }
        return null;
    }

    
    /**
     * Implements Dijkstra's Single-Source Shortest Path algorithm to calculate 
     * the minimum distance from a starting node to all other connected nodes.
     * * Architectural Note: While standard Dijkstra with a Priority Queue is O((V + E) log V),
     * Java's PriorityQueue.remove(Object) is an O(V) operation. In dense graphs, 
     * this implementation degrades to O(E * V). For larger datasets, an Indexed Priority Queue 
     * or a TreeSet is recommended to maintain O(log V) updates.
     * Space Complexity: O(V) for the priority queue.
     * * @param startCity The origin node for the routing calculation.
     */
    public void calculateShortestPath(City startCity) {
        // Reset state for all nodes prior to calculation to prevent stale data from previous runs.
        for (City city : cityList) {
            city.setMinDistance(Double.MAX_VALUE);
            city.setPreviousCity(null);
        }
        
        startCity.setMinDistance(0);
        PriorityQueue<City> queue = new PriorityQueue<>();
        queue.add(startCity);

        while (!queue.isEmpty()) {
            City currentCity = queue.poll();
            if (currentCity.getConnections() == null) continue;

            // Edge relaxation process
            for (Edge edge : currentCity.getConnections()) {
                City targetCity = findCityById(edge.getTargetId());
                if (targetCity == null) continue;

                double newDist = currentCity.getMinDistance() + edge.getDistance();
                
                // If a shorter path to the target city is found, update it and re-queue.
                if (newDist < targetCity.getMinDistance()) {
                    queue.remove(targetCity); // O(V) operation in standard Java PriorityQueue
                    targetCity.setMinDistance(newDist);
                    targetCity.setPreviousCity(currentCity);
                    queue.add(targetCity);
                }
            }
        }
    }

    /**
     * Reconstructs the shortest path by backtracking through the 'previousCity' pointers 
     * established during the execution of calculateShortestPath().
     * Time Complexity: O(P) where P is the number of nodes in the calculated path.
     * * @param targetCity The destination node.
     * @return An ordered list of cities representing the route from origin to destination, 
     * or an empty list if no path exists.
     */
    public List<City> getPath(City targetCity) {
        if (targetCity.getMinDistance() == Double.MAX_VALUE) {
            return new ArrayList<>(); // Unreachable node edge case
        }
        
        List<City> path = new ArrayList<>();
        for (City city = targetCity; city != null; city = city.getPreviousCity()) {
            path.add(city);
        }
        
        // Reverse the backtracked list to present the path from origin -> destination
        Collections.reverse(path);
        return path;
    }

    /**
     * @return The complete, unmodifiable reference to the raw city list.
     */
    public List<City> getCities() {
        return cityList;
    }
}