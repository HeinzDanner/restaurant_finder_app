package de.hepaza.demo.dto;

import java.util.List;

public record LocationDto(
        String type,
        List<Double> coordinates
) {
}
