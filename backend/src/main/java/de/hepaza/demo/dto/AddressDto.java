package de.hepaza.demo.dto;

public record AddressDto(
        String street,
        String houseNumber,
        String postcode,
        String city
) {
}