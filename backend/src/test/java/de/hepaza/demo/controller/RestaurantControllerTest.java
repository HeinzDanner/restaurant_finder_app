package de.hepaza.demo.controller;

import de.hepaza.demo.dto.CreateRestaurantRequest;
import de.hepaza.demo.dto.RestaurantDto;
import de.hepaza.demo.service.RestaurantService;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

    @Mock
    private RestaurantService restaurantService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new RestaurantController(restaurantService)
                )
                .build();
    }

    @Test
    void getAllReturnsRestaurantsAsJson() throws Exception {
        when(restaurantService.getAll()).thenReturn(
                List.of(new RestaurantDto("restaurant-1", "Demo Pizza"))
        );

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("restaurant-1"))
                .andExpect(jsonPath("$[0].name").value("Demo Pizza"));

        verify(restaurantService).getAll();
    }

    @Test
    void getAllReturnsEmptyJsonArray() throws Exception {
        when(restaurantService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createReturns201AndCreatedRestaurant() throws Exception {
        CreateRestaurantRequest request =
                new CreateRestaurantRequest("Demo Pizza");

        when(restaurantService.create(request)).thenReturn(
                new RestaurantDto("restaurant-1", "Demo Pizza")
        );

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Demo Pizza"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("restaurant-1"))
                .andExpect(jsonPath("$.name").value("Demo Pizza"));

        verify(restaurantService).create(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":\"\"}",
            "{\"name\":\"   \"}",
            "{\"name\":null}",
            "{}"
    })
    void createRejectsInvalidNames(String body) throws Exception {
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(restaurantService);
    }

    @Test
    void createRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(restaurantService);
    }
}
