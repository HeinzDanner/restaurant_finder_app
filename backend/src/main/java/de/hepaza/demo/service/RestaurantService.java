package de.hepaza.demo.service;

import de.hepaza.demo.dto.CreateRestaurantRequest;
import de.hepaza.demo.dto.RestaurantDto;
import de.hepaza.demo.model.Address;
import de.hepaza.demo.model.Restaurant;
import de.hepaza.demo.repository.RestaurantRepository;
import de.hepaza.demo.dto.LocationDto;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;


import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;


@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantGeocoder restaurantGeocoder;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            RestaurantGeocoder restaurantGeocoder
    ) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantGeocoder = restaurantGeocoder;
    }

    public List<RestaurantDto> getAll() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public RestaurantDto getById(String id) {
        return toDto(findRestaurantOrThrow(id));
    }

    public RestaurantDto update(String id, CreateRestaurantRequest request) {
        Restaurant restaurant = findRestaurantOrThrow(id);

        Address newAddress = new Address(
                request.street(),
                request.buildingNumber(),
                request.postcode(),
                request.city()
        );

        boolean addressChanged = !Objects.equals(
                normalizeAddress(restaurant.getAddress()),
                normalizeAddress(newAddress)
        );

        if (addressChanged || restaurant.getLocation() == null) {
            restaurant.setLocation(
                    restaurantGeocoder.geocode(newAddress).orElse(null)
            );
        }

        restaurant.setName(request.name().trim());
        restaurant.setCuisines(request.cuisines());
        restaurant.setAddress(newAddress);
        restaurant.setWebsite(request.website());
        restaurant.setOpeningHours(request.openingHours());

        return toDto(restaurantRepository.save(restaurant));
    }

    public void delete(String id) {
        Restaurant restaurant = findRestaurantOrThrow(id);
        restaurantRepository.delete(restaurant);
    }

    private Restaurant findRestaurantOrThrow(String id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Restaurant nicht gefunden"
                ));
    }

    public RestaurantDto create(CreateRestaurantRequest request) {
        Address address = new Address(
                request.street(),
                request.buildingNumber(),
                request.postcode(),
                request.city()
        );

        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID().toString());
        restaurant.setName(request.name().trim());
        restaurant.setCuisines(request.cuisines());
        restaurant.setAddress(address);
        // location bleibt vorerst leer, bis die Adresse geokodiert wird
        restaurant.setWebsite(request.website());
        restaurant.setOpeningHours(request.openingHours());
        restaurant.setSource("manual");

        return toDto(restaurantRepository.save(restaurant));
    }

    private RestaurantDto toDto(Restaurant restaurant) {
        Address address = restaurant.getAddress();
        var point = restaurant.getLocation();

        LocationDto location = point == null
                ? null
                : new LocationDto(
                "Point",
                List.of(point.getX(), point.getY())
        );

        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCuisines(),
                address != null ? address.street() : null,
                address != null ? address.buildingNumber() : null,
                address != null ? address.postcode() : null,
                address != null ? address.city() : null,
                restaurant.getWebsite(),
                restaurant.getOpeningHours(),
                location
        );
    }

    private String normalizeAddressPart(String value) {
        return value == null ? "" : value.strip();
    }

    private Address normalizeAddress(Address address) {
        if (address == null) {
            return new Address("", "", "", "");
        }

        return new Address(
                normalizeAddressPart(address.street()),
                normalizeAddressPart(address.buildingNumber()),
                normalizeAddressPart(address.postcode()),
                normalizeAddressPart(address.city())
        );
    }

}
