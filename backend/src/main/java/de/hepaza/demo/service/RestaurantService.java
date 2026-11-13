package de.hepaza.demo.service;

import de.hepaza.demo.dto.CreateRestaurantRequest;
import de.hepaza.demo.dto.RestaurantDto;
import de.hepaza.demo.model.Restaurant;
import de.hepaza.demo.repository.RestaurantRepository;

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
        Restaurant restaurant = new Restaurant(
                UUID.randomUUID().toString(),
                request.name().trim()
        );

        Restaurant saved = restaurantRepository.save(restaurant);
        return toDto(saved);
    }

    private RestaurantDto toDto(Restaurant restaurant) {
        return new RestaurantDto(restaurant.getId(), restaurant.getName());
    }
}
