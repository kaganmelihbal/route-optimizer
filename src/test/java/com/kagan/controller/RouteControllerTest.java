package com.kagan.controller;

import com.kagan.service.GraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GraphService graphService;

    @BeforeEach
    void setup() {
        graphService.loadData("test-cities.json");
    }

    @Test
    void testCalculateRoute_AdanaToNigde_Success() throws Exception {
        // Distance 270, Consumption 10, Price 1 -> (270/100)*10*1 = 27.0
        mockMvc.perform(get("/api/route/calculate")
                .param("from", "Adana")
                .param("to", "Niğde")
                .param("consumption", "10.0")
                .param("fuelPrice", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDistance").value(270))
                .andExpect(jsonPath("$.fuelCost").value(27.0));
    }

    @Test
    void testCalculateRoute_WithWaypoint() throws Exception {
        // Adana -> Osmaniye (91) -> Kahramanmaraş (105) = 196 KM
        mockMvc.perform(get("/api/route/calculate")
                .param("from", "Adana")
                .param("to", "Kahramanmaraş")
                .param("via", "Osmaniye"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDistance").value(196));
    }
}