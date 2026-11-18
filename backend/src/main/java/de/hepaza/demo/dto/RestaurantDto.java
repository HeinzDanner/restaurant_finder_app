package de.hepaza.demo.dto;

import java.util.List;

public record RestaurantDto(
        String id,
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
