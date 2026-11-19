package de.hepaza.demo.service;

import de.hepaza.demo.dto.CreateRestaurantRequest;
import de.hepaza.demo.dto.RestaurantDto;
import de.hepaza.demo.model.Address;
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

import de.hepaza.demo.dto.LocationDto;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;


    private CreateRestaurantRequest request(String name) {
        return new CreateRestaurantRequest(name, null, null, null, null, null, null, null);
    }

    private RestaurantDto response(String id, String name) {
        return new RestaurantDto(id, name, null, null, null, null, null, null, null, null);
    }

    private Restaurant savedRestaurant() {
        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void getAllReturnsRestaurantDtos() {
        when(restaurantRepository.findAll())
                .thenReturn(List.of(new Restaurant("restaurant-1", "Demo Pizza")));

        assertThat(restaurantService.getAll())
                .containsExactly(response("restaurant-1", "Demo Pizza"));
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
                response("id-1", "Café am Markt"),
                response("id-2", "Sushi Haus")
        );
    }

    @Test
    void getAllFlattensAddressAndMapsAllFields() {
        Restaurant restaurant = new Restaurant("id-1", "Demo Bistro");
        restaurant.setCuisines(List.of("italienisch", "vegetarisch"));
        restaurant.setAddress(new Address("Hauptstraße", "12a", "70173", "Stuttgart"));
        restaurant.setWebsite("https://example.org");
        restaurant.setOpeningHours("Mo-Fr 11:00-22:00");
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));

        assertThat(restaurantService.getAll()).containsExactly(new RestaurantDto(
                "id-1",
                "Demo Bistro",
                List.of("italienisch", "vegetarisch"),
                "Hauptstraße",
                "12a",
                "70173",
                "Stuttgart",
                "https://example.org",
                "Mo-Fr 11:00-22:00",
                null
        ));
    }

    @Test
    void createGeneratesUuidTrimsNameAndSetsManualSource() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantDto result = restaurantService.create(request("  Demo Pizza  "));

        Restaurant saved = savedRestaurant();
        assertThat(saved.getName()).isEqualTo("Demo Pizza");
        assertThat(UUID.fromString(saved.getId()).toString()).isEqualTo(saved.getId());
        assertThat(saved.getSource()).isEqualTo("manual");
        assertThat(saved.getSourceId()).isNull();
        assertThat(saved.getLocation()).isNull();

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.name()).isEqualTo("Demo Pizza");
    }

    @Test
    void createMapsCuisinesAddressAndOptionalFields() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

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

        RestaurantDto result = restaurantService.create(request);

        Restaurant saved = savedRestaurant();
        assertThat(saved.getCuisines()).containsExactly("italienisch", "vegetarisch");
        assertThat(saved.getAddress())
                .isEqualTo(new Address("Hauptstraße", "12a", "70173", "Stuttgart"));
        assertThat(saved.getWebsite()).isEqualTo("https://example.org");
        assertThat(saved.getOpeningHours()).isEqualTo("Mo-Fr 11:00-22:00");

        assertThat(result).isEqualTo(new RestaurantDto(
                saved.getId(),
                "Demo Bistro",
                List.of("italienisch", "vegetarisch"),
                "Hauptstraße",
                "12a",
                "70173",
                "Stuttgart",
                "https://example.org",
                "Mo-Fr 11:00-22:00",
                null
        ));
    }

    @Test
    void createReturnsValuesActuallySavedByRepository() {
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenReturn(new Restaurant("persisted-id", "Gespeicherter Name"));

        assertThat(restaurantService.create(request("Eingabe")))
                .isEqualTo(response("persisted-id", "Gespeicherter Name"));
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

        CreateRestaurantRequest request = request("Demo Pizza");

        assertThatThrownBy(() -> restaurantService.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Database unavailable");
    }

    @Test
    void getAllMapsLocationAsGeoJson() {
        Restaurant restaurant = new Restaurant("id-1", "Demo Bistro");
        restaurant.setLocation(new GeoJsonPoint(9.18, 48.78));
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));

        assertThat(restaurantService.getAll())
                .singleElement()
                .extracting(RestaurantDto::location)
                .isEqualTo(new LocationDto("Point", List.of(9.18, 48.78)));
    }
}
