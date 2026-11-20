package de.hepaza.demo.controller;

import de.hepaza.demo.dto.CreateRestaurantRequest;
import de.hepaza.demo.dto.RestaurantDto;
import de.hepaza.demo.service.RestaurantService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public List<RestaurantDto> getAll() {
        return restaurantService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantDto create(@Valid @RequestBody CreateRestaurantRequest request) {
        return restaurantService.create(request);
    }

    @GetMapping("/{id}")
    public RestaurantDto getById(@PathVariable String id) {
        return restaurantService.getById(id);
    }

    @PutMapping("/{id}")
    public RestaurantDto update(
            @PathVariable String id,
            @Valid @RequestBody CreateRestaurantRequest request
    ) {
        return restaurantService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        restaurantService.delete(id);
    }
}