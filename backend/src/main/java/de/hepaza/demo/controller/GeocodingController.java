package de.hepaza.demo.controller;

import de.hepaza.demo.dto.LocationDto;
import de.hepaza.demo.model.Address;
import de.hepaza.demo.service.NominatimRestaurantGeocoder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/geocoding")
public class GeocodingController {

    private final NominatimRestaurantGeocoder geocoder;

    public GeocodingController(NominatimRestaurantGeocoder geocoder) {
        this.geocoder = geocoder;
    }

    public record StartAddressRequest(
            @NotBlank String street,
            String buildingNumber,
            String postcode,
            String city
    ) {
    }

    @PostMapping("/start-address")
    public LocationDto findStartAddress(
            @Valid @RequestBody StartAddressRequest request
    ) {
        boolean noPostcode = request.postcode() == null || request.postcode().isBlank();
        boolean noCity = request.city() == null || request.city().isBlank();

        if (noPostcode && noCity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bitte Stadt oder Postleitzahl angeben."
            );
        }

        Address address = new Address(
                request.street(),
                request.buildingNumber(),
                request.postcode(),
                request.city()
        );

        var point = geocoder.geocodeStartAddress(address)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Die Adresse wurde nicht gefunden."
                ));

        return new LocationDto(
                "Point",
                List.of(point.getX(), point.getY())
        );
    }
}