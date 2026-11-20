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

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private RestaurantGeocoder restaurantGeocoder;

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

    @Test
    void getByIdReturnsRestaurantDto() {
        Restaurant restaurant = new Restaurant("id-1", "Demo Pizza");
        when(restaurantRepository.findById("id-1")).thenReturn(Optional.of(restaurant));

        assertThat(restaurantService.getById("id-1"))
                .isEqualTo(response("id-1", "Demo Pizza"));
    }

    @Test
    void getByIdReturnsNotFoundWhenRestaurantDoesNotExist() {
        when(restaurantRepository.findById("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.getById("missing"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateChangesRestaurantAndPreservesLocationWhenAddressIsUnchanged() {
        Restaurant restaurant = new Restaurant("id-1", "Old Name");
        restaurant.setAddress(new Address("Hauptstraße", "1", "70173", "Stuttgart"));
        restaurant.setLocation(new GeoJsonPoint(9.18, 48.78));
        restaurant.setSource("osm");

        when(restaurantRepository.findById("id-1")).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "  New Name  ",
                List.of("japanisch"),
                " Hauptstraße ",
                "1",
                "70173",
                "Stuttgart",
                "https://example.org",
                "Mo-Fr 11:00-22:00"
        );

        RestaurantDto result = restaurantService.update("id-1", request);

        assertThat(result.id()).isEqualTo("id-1");
        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.cuisines()).containsExactly("japanisch");
        assertThat(result.location())
                .isEqualTo(new LocationDto("Point", List.of(9.18, 48.78)));

        assertThat(restaurant.getSource()).isEqualTo("osm");
        assertThat(restaurant.getId()).isEqualTo("id-1");
        verify(restaurantRepository).save(restaurant);
        verify(restaurantGeocoder, never()).geocode(any(Address.class));
    }

    @Test
    void updateClearsLocationWhenAddressChanges() {
        Restaurant restaurant = new Restaurant("id-1", "Demo Pizza");
        restaurant.setAddress(new Address("Altstraße", "1", "70173", "Stuttgart"));
        restaurant.setLocation(new GeoJsonPoint(9.18, 48.78));

        when(restaurantRepository.findById("id-1")).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Demo Pizza",
                null,
                "Neustraße",
                "2",
                "70173",
                "Stuttgart",
                null,
                null
        );

        when(restaurantGeocoder.geocode(any(Address.class)))
                .thenReturn(Optional.empty());

        RestaurantDto result = restaurantService.update("id-1", request);

        assertThat(restaurant.getAddress())
                .isEqualTo(new Address("Neustraße", "2", "70173", "Stuttgart"));
        assertThat(restaurant.getLocation()).isNull();
        assertThat(result.location()).isNull();
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void updateReturnsNotFoundWhenRestaurantDoesNotExist() {
        when(restaurantRepository.findById("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.update("missing", request("Pizza")))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void deleteRemovesRestaurant() {
        Restaurant restaurant = new Restaurant("id-1", "Demo Pizza");
        when(restaurantRepository.findById("id-1")).thenReturn(Optional.of(restaurant));

        restaurantService.delete("id-1");

        verify(restaurantRepository).delete(restaurant);
    }

    @Test
    void deleteReturnsNotFoundWhenRestaurantDoesNotExist() {
        when(restaurantRepository.findById("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.delete("missing"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(restaurantRepository, never()).delete(any(Restaurant.class));
    }

    @Test
    void updateStoresNewLocationWhenGeocodingSucceeds() {
        Restaurant restaurant = new Restaurant("id-1", "Demo Pizza");
        restaurant.setAddress(new Address("Altstraße", "1", "70173", "Stuttgart"));
        restaurant.setLocation(new GeoJsonPoint(9.18, 48.78));

        Address newAddress = new Address("Neustraße", "2", "70173", "Stuttgart");
        GeoJsonPoint newLocation = new GeoJsonPoint(9.20, 48.79);

        when(restaurantRepository.findById("id-1"))
                .thenReturn(Optional.of(restaurant));
        when(restaurantGeocoder.geocode(newAddress))
                .thenReturn(Optional.of(newLocation));
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "Demo Pizza",
                null,
                "Neustraße",
                "2",
                "70173",
                "Stuttgart",
                null,
                null
        );

        RestaurantDto result = restaurantService.update("id-1", request);

        assertThat(restaurant.getAddress()).isEqualTo(newAddress);
        assertThat(restaurant.getLocation()).isEqualTo(newLocation);
        assertThat(result.location())
                .isEqualTo(new LocationDto("Point", List.of(9.20, 48.79)));

        verify(restaurantGeocoder).geocode(newAddress);
        verify(restaurantRepository).save(restaurant);
    }
}
