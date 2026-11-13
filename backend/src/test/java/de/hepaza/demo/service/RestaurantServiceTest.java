package de.hepaza.demo.service;

import de.hepaza.demo.dto.CreateRestaurantRequest;
import de.hepaza.demo.dto.RestaurantDto;
import de.hepaza.demo.model.Restaurant;
import de.hepaza.demo.repository.RestaurantRepository;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    @Test
    void getAllReturnsRestaurantDtos() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("restaurant-1");
        restaurant.setName("Demo Pizza");

        when(restaurantRepository.findAll())
                .thenReturn(List.of(restaurant));

        List<RestaurantDto> result = restaurantService.getAll();

        assertThat(result).containsExactly(
                new RestaurantDto("restaurant-1", "Demo Pizza")
        );
    }

    @Test
    void getAllReturnsEmptyListWhenNoRestaurantsExist() {
        when(restaurantRepository.findAll()).thenReturn(List.of());

        assertThat(restaurantService.getAll()).isEmpty();
    }

    @Test
    void getAllMapsEveryRestaurantInRepositoryOrder() {
        when(restaurantRepository.findAll()).thenReturn(List.of(
                new Restaurant("id-1", "Café am Markt"),
                new Restaurant("id-2", "Sushi Haus")
        ));

        assertThat(restaurantService.getAll()).containsExactly(
                new RestaurantDto("id-1", "Café am Markt"),
                new RestaurantDto("id-2", "Sushi Haus")
        );
    }

    @Test
    void createGeneratesUuidTrimsNameAndReturnsSavedRestaurant() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantDto result = restaurantService.create(
                new CreateRestaurantRequest("  Demo Pizza  ")
        );

        ArgumentCaptor<Restaurant> captor =
                ArgumentCaptor.forClass(Restaurant.class);

        verify(restaurantRepository).save(captor.capture());

        Restaurant saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Demo Pizza");
        assertThat(saved.getId()).isNotBlank();
        assertThat(UUID.fromString(saved.getId()).toString())
                .isEqualTo(saved.getId());

        assertThat(result).isEqualTo(
                new RestaurantDto(saved.getId(), "Demo Pizza")
        );
    }

    @Test
    void createReturnsValuesActuallySavedByRepository() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenReturn(new Restaurant("persisted-id", "Gespeicherter Name"));

        assertThat(restaurantService.create(new CreateRestaurantRequest("Eingabe")))
                .isEqualTo(new RestaurantDto("persisted-id", "Gespeicherter Name"));
    }

    @Test
    void createGeneratesDifferentIdsForSeparateRestaurants() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantDto first = restaurantService.create(new CreateRestaurantRequest("Pizza"));
        RestaurantDto second = restaurantService.create(new CreateRestaurantRequest("Pizza"));

        assertThat(first.id()).isNotEqualTo(second.id());
    }

    @Test
    void getAllDoesNotHideDatabaseErrors() {
        when(restaurantRepository.findAll())
                .thenThrow(new IllegalStateException("Database unavailable"));

        assertThatThrownBy(() -> restaurantService.getAll())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Database unavailable");
    }

    @Test
    void createDoesNotHideDatabaseErrors() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenThrow(new IllegalStateException("Database unavailable"));

        assertThatThrownBy(() -> restaurantService.create(
                new CreateRestaurantRequest("Demo Pizza")
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Database unavailable");
    }
}