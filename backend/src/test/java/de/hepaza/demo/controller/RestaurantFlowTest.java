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

import de.hepaza.demo.model.Restaurant;
import de.hepaza.demo.repository.RestaurantRepository;
import de.hepaza.demo.service.RestaurantService;
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
}
