package de.hepaza.demo.dto;

import jakarta.validation.constraints.AssertTrue;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public record LocationDto(
        String type,
        List<Double> coordinates
) {
    @AssertTrue(message = "location muss ein GeoJSON-Point mit gültigen Koordinaten sein")
    @JsonIgnore
    @AssertTrue(message = "location muss ein GeoJSON-Point mit gültigen Koordinaten sein")
    public boolean isValidPoint() {
        if (type == null || !type.equals("Point")
                || coordinates == null || coordinates.size() != 2
                || coordinates.get(0) == null || coordinates.get(1) == null) {
            return false;
        }

        double longitude = coordinates.get(0);
        double latitude = coordinates.get(1);

        return longitude >= -180 && longitude <= 180
                && latitude >= -90 && latitude <= 90;
    }
}
