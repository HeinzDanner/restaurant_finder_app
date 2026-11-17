package de.hepaza.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record CreateRestaurantRequest(
        @NotBlank(message = "Der Name darf nicht leer sein")
        String name,
        String cuisine,
        AddressDto address,
        @Valid LocationDto location,
        String website,
        String openingHours
) {
}
