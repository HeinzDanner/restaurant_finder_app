package de.hepaza.demo.dto;

public record RestaurantDto(
        String id,
        String name,
        String cuisine,
        AddressDto address,
        LocationDto location,
        String website,
        String openingHours,
        String source,
        String sourceId
) {
}