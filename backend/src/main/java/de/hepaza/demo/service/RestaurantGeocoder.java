package de.hepaza.demo.service;

import de.hepaza.demo.model.Address;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.Optional;

public interface RestaurantGeocoder {
    Optional<GeoJsonPoint> geocode(Address address);
}