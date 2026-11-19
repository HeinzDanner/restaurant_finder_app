package de.hepaza.demo.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateRestaurantRequest(
        @NotBlank(message = "Der Name darf nicht leer sein")
        String name,
        List<String> cuisines,
        String street,
        String buildingNumber,
        String postcode,
        String city,
        String website,
        String openingHours
) {
}
