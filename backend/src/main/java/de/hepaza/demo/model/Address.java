package de.hepaza.demo.model;

public record Address(
        String street,
        String houseNumber,
        String postcode,
        String city
) {
}