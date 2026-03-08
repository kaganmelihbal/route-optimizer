package com.kagan.service;

import com.kagan.model.City;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GraphServiceTest {

    private GraphService graphService;

    @BeforeEach
    void setUp() {
        graphService = new GraphService();
        // Düzeltilmiş test-cities.json dosyasını yükle
        boolean isLoaded = graphService.loadData("test-cities.json");
        assertTrue(isLoaded, "Test data could not be loaded! Check the file format or path.");
    }

    @Test
    void testLoadData() {
        assertEquals(6, graphService.getCities().size());
    }

    @Test
    void testFindCityByName_TurkishChars() {
        City city = graphService.findCityByName("NIGDE");
        assertNotNull(city);
        assertEquals("Niğde", city.getName());
    }

    @Test
    void testShortestPath_AdanaToNigde() {
        // Adana -> Mersin (70) -> Niğde (200) = 270 KM
        City start = graphService.findCityByName("Adana");
        City end = graphService.findCityByName("Niğde");
        
        graphService.calculateShortestPath(start);
        
        assertEquals(270.0, end.getMinDistance());
    }
}