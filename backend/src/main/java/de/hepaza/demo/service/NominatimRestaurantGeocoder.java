package de.hepaza.demo.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.hepaza.demo.model.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Component
public class NominatimRestaurantGeocoder implements RestaurantGeocoder {
    private static final Logger logger =
            LoggerFactory.getLogger(NominatimRestaurantGeocoder.class);

    private final RestClient restClient;
    private final Map<String, Optional<GeoJsonPoint>> cache = new HashMap<>();
    private long lastRequestNanos;

    public NominatimRestaurantGeocoder() {
        this(createRestClient());
    }

    NominatimRestaurantGeocoder(RestClient restClient) {
        this.restClient = restClient;
    }

    private static RestClient createRestClient() {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .requestFactory(requestFactory)
                .defaultHeader(
                        HttpHeaders.USER_AGENT,
                        "RestaurantFinder/1.0 (https://github.com/HeinzDanner/restaurant_finder_app)"
                )
                .build();
    }

    @Override
    public synchronized Optional<GeoJsonPoint> geocode(Address address) {
        return lookup(address, false);
    }

    public synchronized Optional<GeoJsonPoint> geocodeStartAddress(Address address) {
        return lookup(address, true);
    }

    private Optional<GeoJsonPoint> lookup(Address address, boolean reportErrors) {
        if (address == null) {
            return Optional.empty();
        }

        String street = Stream.of(address.street(), address.buildingNumber())
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));

        String postcode = address.postcode() == null ? "" : address.postcode().trim();
        String city = address.city() == null ? "" : address.city().trim();

        if (street.isBlank() && postcode.isBlank() && city.isBlank()) {
            return Optional.empty();
        }

        String query = Stream.of(street, postcode, city, "Germany")
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(", "));

        String cacheKey = query.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();

        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        waitForRateLimit();

        try {
            List<NominatimResult> results = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("street", street)
                            .queryParam("postalcode", postcode)
                            .queryParam("city", city)
                            .queryParam("country", "Germany")
                            .queryParam("countrycodes", "de")
                            .queryParam("format", "jsonv2")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (results == null || results.isEmpty()) {
                cache.put(cacheKey, Optional.empty());
                return Optional.empty();
            }

            String latitude = results.getFirst().lat();
            String longitude = results.getFirst().lon();

            if (latitude == null || longitude == null) {
                cache.put(cacheKey, Optional.empty());
                return Optional.empty();
            }

            // GeoJSON erwartet zuerst Längengrad, dann Breitengrad.
            Optional<GeoJsonPoint> point = Optional.of(
                    new GeoJsonPoint(
                            Double.parseDouble(longitude),
                            Double.parseDouble(latitude)
                    )
            );
            cache.put(cacheKey, point);
            return point;
        } catch (RestClientException | NumberFormatException exception) {
            logger.warn("Nominatim konnte die Adresse nicht geocodieren.", exception);

            if (reportErrors) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Der Geocoding-Dienst ist derzeit nicht erreichbar.",
                        exception
                );
            }

            return Optional.empty();
        }
    }

    private void waitForRateLimit() {
        try {
            if (lastRequestNanos != 0) {
                long elapsed = System.nanoTime() - lastRequestNanos;
                long waitNanos = TimeUnit.SECONDS.toNanos(1) - elapsed;
                if (waitNanos > 0) {
                    TimeUnit.NANOSECONDS.sleep(waitNanos);
                }
            }
            lastRequestNanos = System.nanoTime();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logger.warn("Warten auf Nominatim wurde unterbrochen.");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResult(String lat, String lon) {
    }
}
