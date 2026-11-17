package de.hepaza.demo.service;

import de.hepaza.demo.dto.AddressDto;
import de.hepaza.demo.dto.CreateRestaurantRequest;
import de.hepaza.demo.dto.LocationDto;
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

    private CreateRestaurantRequest request(String name) {
        return new CreateRestaurantRequest(name, null, null, null, null, null);
    }

    private RestaurantDto dto(String id, String name, String source) {
        return new RestaurantDto(
                id, name, null, null, null, null, null, source, null
        );
    }

    @Test
    void getAllReturnsRestaurantDtos() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("restaurant-1");
        restaurant.setName("Demo Pizza");

        when(restaurantRepository.findAll())
                .thenReturn(List.of(restaurant));

        List<RestaurantDto> result = restaurantService.getAll();

        assertThat(result).containsExactly(
                dto("restaurant-1", "Demo Pizza", null)
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
                dto("id-1", "Café am Markt", null),
                dto("id-2", "Sushi Haus", null)
        );
    }

    @Test
    void createGeneratesUuidTrimsNameAndReturnsSavedRestaurant() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantDto result = restaurantService.create(
                request("  Demo Pizza  ")
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
                dto(saved.getId(), "Demo Pizza", "manual")
        );
    }

    @Test
    void createMapsOptionalFieldsAndLocation() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AddressDto address = new AddressDto(
                "Hauptstraße", "12a", "70173", "Stuttgart"
        );
        LocationDto location = new LocationDto(
                "Point", List.of(9.18, 48.78)
        );

        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Demo Bistro",
                "italienisch",
                address,
                location,
                "https://example.org",
                "Mo-Fr 11:00-22:00"
        );

        RestaurantDto result = restaurantService.create(request);

        ArgumentCaptor<Restaurant> captor =
                ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepository).save(captor.capture());

        Restaurant saved = captor.getValue();
        assertThat(saved.getAddress().city()).isEqualTo("Stuttgart");
        assertThat(saved.getLocation().getX()).isEqualTo(9.18);
        assertThat(saved.getLocation().getY()).isEqualTo(48.78);

        assertThat(result.cuisine()).isEqualTo("italienisch");
        assertThat(result.address()).isEqualTo(address);
        assertThat(result.location()).isEqualTo(location);
        assertThat(result.website()).isEqualTo("https://example.org");
        assertThat(result.openingHours()).isEqualTo("Mo-Fr 11:00-22:00");
        assertThat(result.source()).isEqualTo("manual");
        assertThat(result.sourceId()).isNull();
    }

    @Test
    void createReturnsValuesActuallySavedByRepository() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenReturn(new Restaurant("persisted-id", "Gespeicherter Name"));

        assertThat(restaurantService.create(request("Eingabe")))
                .isEqualTo(dto("persisted-id", "Gespeicherter Name", null));
    }

    @Test
    void createGeneratesDifferentIdsForSeparateRestaurants() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantDto first = restaurantService.create(request("Pizza"));
        RestaurantDto second = restaurantService.create(request("Pizza"));

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

        assertThatThrownBy(() -> restaurantService.create(request("Demo Pizza")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Database unavailable");
    }
}