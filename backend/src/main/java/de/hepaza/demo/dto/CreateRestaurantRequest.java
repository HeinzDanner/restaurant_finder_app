package de.hepaza.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRestaurantRequest(
        @NotBlank(message = "Der Name darf nicht leer sein")
        String name
) {
}
