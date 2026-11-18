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
                List.of(new RestaurantDto(
                        "restaurant-1", "Demo Pizza",
                        null, null, null, null, null, null, null
                ))
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
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Demo Pizza", null, null, null, null, null, null, null
        );
        when(restaurantService.create(request)).thenReturn(
                new RestaurantDto(
                        "restaurant-1", "Demo Pizza",
                        null, null, null, null, null, null, null
                )
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

    @Test
    void createAcceptsFullRequestAndReturnsFlatResponse() throws Exception {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Demo Bistro",
                List.of("italienisch", "vegetarisch"),
                "Hauptstraße",
                "12a",
                "70173",
                "Stuttgart",
                "https://example.org",
                "Mo-Fr 11:00-22:00"
        );
        when(restaurantService.create(request)).thenReturn(new RestaurantDto(
                "restaurant-1",
                "Demo Bistro",
                List.of("italienisch", "vegetarisch"),
                "Hauptstraße",
                "12a",
                "70173",
                "Stuttgart",
                "https://example.org",
                "Mo-Fr 11:00-22:00"
        ));

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Demo Bistro",
                                  "cuisines": ["italienisch", "vegetarisch"],
                                  "street": "Hauptstraße",
                                  "buildingNumber": "12a",
                                  "postcode": "70173",
                                  "city": "Stuttgart",
                                  "website": "https://example.org",
                                  "openingHours": "Mo-Fr 11:00-22:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("restaurant-1"))
                .andExpect(jsonPath("$.cuisines.length()").value(2))
                .andExpect(jsonPath("$.cuisines[0]").value("italienisch"))
                .andExpect(jsonPath("$.street").value("Hauptstraße"))
                .andExpect(jsonPath("$.buildingNumber").value("12a"))
                .andExpect(jsonPath("$.postcode").value("70173"))
                .andExpect(jsonPath("$.city").value("Stuttgart"))
                .andExpect(jsonPath("$.website").value("https://example.org"))
                .andExpect(jsonPath("$.openingHours").value("Mo-Fr 11:00-22:00"));

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

    @Test
    void createRejectsMissingBody() throws Exception {
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(restaurantService);
    }

    @Test
    void createRejectsUnsupportedContentType() throws Exception {
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Demo Pizza"))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(restaurantService);
    }
}
