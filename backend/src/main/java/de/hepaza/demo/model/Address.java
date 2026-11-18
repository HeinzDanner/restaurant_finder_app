package de.hepaza.demo.model;

public record Address(
        String street,
        String buildingNumber,
        String postcode,
        String city
) {
}