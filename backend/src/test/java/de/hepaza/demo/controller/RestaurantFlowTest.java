package de.hepaza.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.hepaza.demo.model.Address;
import de.hepaza.demo.model.Restaurant;
import de.hepaza.demo.repository.RestaurantRepository;
import de.hepaza.demo.service.RestaurantService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RestaurantFlowTest {

    private RestaurantRepository repository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository = mock(RestaurantRepository.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RestaurantController(new RestaurantService(repository)))
                .build();
    }

    @Test
    void postValidatesAndPassesTrimmedNameToRepository() throws Exception {
        when(repository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String response = mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  Café am Markt  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Café am Markt"))
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(repository).save(captor.capture());
        Restaurant saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Café am Markt");
        assertThat(UUID.fromString(saved.getId()).toString()).isEqualTo(saved.getId());
        assertThat(response).contains(saved.getId());
    }

    @Test
    void invalidRequestDoesNotReachRepository() throws Exception {
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void getReturnsAllRepositoryRestaurants() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
                new Restaurant("id-1", "Pizza"),
                new Restaurant("id-2", "Sushi")
        ));

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].name").value("Pizza"))
                .andExpect(jsonPath("$[1].id").value("id-2"))
                .andExpect(jsonPath("$[1].name").value("Sushi"));

        verify(repository).findAll();
    }

    @Test
    void createdRestaurantsAppearInSubsequentList() throws Exception {
        List<Restaurant> stored = new ArrayList<>();
        when(repository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant restaurant = invocation.getArgument(0);
            stored.add(restaurant);
            return restaurant;
        });
        when(repository.findAll()).thenAnswer(invocation -> List.copyOf(stored));

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  Pizza Haus  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pizza Haus"));
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sushi\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(stored.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value("Pizza Haus"))
                .andExpect(jsonPath("$[1].id").value(stored.get(1).getId()))
                .andExpect(jsonPath("$[1].name").value("Sushi"));
    }

    @Test
    void postPreservesSpecialCharactersAndInternalSpaces() throws Exception {
        when(repository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  Café \\\"Zum Markt\\\"  & Bistro  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Café \"Zum Markt\"  & Bistro"));

        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Café \"Zum Markt\"  & Bistro");
    }

    @Test
    void postStoresAddressAndCuisinesAndReturnsFlatResponse() throws Exception {
        when(repository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

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
                                  "website": "https://example.org"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cuisines[1]").value("vegetarisch"))
                .andExpect(jsonPath("$.street").value("Hauptstraße"))
                .andExpect(jsonPath("$.buildingNumber").value("12a"))
                .andExpect(jsonPath("$.postcode").value("70173"))
                .andExpect(jsonPath("$.city").value("Stuttgart"))
                .andExpect(jsonPath("$.website").value("https://example.org"))
                .andExpect(jsonPath("$.address").doesNotExist())
                .andExpect(jsonPath("$.location").doesNotExist());

        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(repository).save(captor.capture());
        Restaurant saved = captor.getValue();
        assertThat(saved.getAddress())
                .isEqualTo(new Address("Hauptstraße", "12a", "70173", "Stuttgart"));
        assertThat(saved.getCuisines()).containsExactly("italienisch", "vegetarisch");
        assertThat(saved.getSource()).isEqualTo("manual");
    }

    @Test
    void postIgnoresClientSuppliedIdAndGeneratesUuid() throws Exception {
        when(repository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"\",\"name\":\"Pizza\",\"cuisines\":[]}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(repository).save(captor.capture());
        String id = captor.getValue().getId();
        assertThat(UUID.fromString(id).toString()).isEqualTo(id);
    }
}
