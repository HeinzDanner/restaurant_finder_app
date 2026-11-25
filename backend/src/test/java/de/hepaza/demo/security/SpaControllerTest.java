package de.hepaza.demo.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void addNewRestaurant_forwardsToIndex() throws Exception {
        mockMvc.perform(get("/addNewRestaurant"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void editRestaurant_forwardsToIndex() throws Exception {
        mockMvc.perform(get("/editRestaurant/abc"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }
}