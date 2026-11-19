package de.hepaza.demo.service;

import de.hepaza.demo.dto.CreateRestaurantRequest;
import de.hepaza.demo.dto.RestaurantDto;
import de.hepaza.demo.model.Address;
import de.hepaza.demo.model.Restaurant;
import de.hepaza.demo.repository.RestaurantRepository;
import de.hepaza.demo.dto.LocationDto;


import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public List<RestaurantDto> getAll() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
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
}
