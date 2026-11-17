package de.hepaza.demo.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class LocationDtoTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @ParameterizedTest
    @MethodSource("validLocations")
    void acceptsValidLocations(LocationDto location) {
        assertThat(validator.validate(location)).isEmpty();
    }

    static Stream<LocationDto> validLocations() {
        return Stream.of(
                new LocationDto("Point", List.of(9.18, 48.78)),
                new LocationDto("Point", List.of(-180.0, -90.0)),
                new LocationDto("Point", List.of(180.0, 90.0)),
                new LocationDto("Point", List.of(0.0, 0.0))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidLocations")
    void rejectsInvalidLocations(LocationDto location) {
        assertThat(validator.validate(location))
                .anySatisfy(violation ->
                        assertThat(violation.getPropertyPath().toString())
                                .isEqualTo("validPoint")
                );
    }

    static Stream<LocationDto> invalidLocations() {
        return Stream.of(
                new LocationDto(null, List.of(9.18, 48.78)),
                new LocationDto("LineString", List.of(9.18, 48.78)),
                new LocationDto("Point", null),
                new LocationDto("Point", List.of()),
                new LocationDto("Point", List.of(9.18)),
                new LocationDto("Point", List.of(9.18, 48.78, 1.0)),
                new LocationDto("Point", Arrays.asList(null, 48.78)),
                new LocationDto("Point", Arrays.asList(9.18, null)),
                new LocationDto("Point", List.of(-180.01, 48.78)),
                new LocationDto("Point", List.of(180.01, 48.78)),
                new LocationDto("Point", List.of(9.18, -90.01)),
                new LocationDto("Point", List.of(9.18, 90.01))
        );
    }
}