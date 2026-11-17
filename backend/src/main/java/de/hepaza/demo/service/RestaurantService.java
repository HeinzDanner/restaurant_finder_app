package de.hepaza.demo.service;

import de.hepaza.demo.dto.AddressDto;
import de.hepaza.demo.dto.CreateRestaurantRequest;
import de.hepaza.demo.dto.LocationDto;
import de.hepaza.demo.dto.RestaurantDto;
import de.hepaza.demo.model.Address;
import de.hepaza.demo.model.Restaurant;
import de.hepaza.demo.repository.RestaurantRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
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
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID().toString());
        restaurant.setName(request.name().trim());
        restaurant.setCuisine(request.cuisine());
        restaurant.setAddress(toAddress(request.address()));
        restaurant.setLocation(toGeoJsonPoint(request.location()));
        restaurant.setWebsite(request.website());
        restaurant.setOpeningHours(request.openingHours());
        restaurant.setSource("manual");
        restaurant.setSourceId(null);

        return toDto(restaurantRepository.save(restaurant));
    }

    private Address toAddress(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        return new Address(
                addressDto.street(),
                addressDto.houseNumber(),
                addressDto.postcode(),
                addressDto.city()
        );
    }

    private GeoJsonPoint toGeoJsonPoint(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }

        return new GeoJsonPoint(
                locationDto.coordinates().get(0),
                locationDto.coordinates().get(1)
        );
    }

    private RestaurantDto toDto(Restaurant restaurant) {
        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCuisine(),
                toAddressDto(restaurant.getAddress()),
                toLocationDto(restaurant.getLocation()),
                restaurant.getWebsite(),
                restaurant.getOpeningHours(),
                restaurant.getSource(),
                restaurant.getSourceId()
        );
    }

    private AddressDto toAddressDto(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressDto(
                address.street(),
                address.houseNumber(),
                address.postcode(),
                address.city()
        );
    }

    private LocationDto toLocationDto(GeoJsonPoint location) {
        if (location == null) {
            return null;
        }

        return new LocationDto(
                "Point",
                List.of(location.getX(), location.getY())
        );
    }
}