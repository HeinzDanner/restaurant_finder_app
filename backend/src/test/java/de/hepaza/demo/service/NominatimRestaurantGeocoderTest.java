package de.hepaza.demo.service;

import de.hepaza.demo.model.Address;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class NominatimRestaurantGeocoderTest {
    @Test
    void geocodeReadsCoordinatesFromResponseWithBoundingBox() {
        var clientBuilder = RestClient.builder().baseUrl("https://example.test");
        var server = MockRestServiceServer.bindTo(clientBuilder).build();
        var geocoder = new NominatimRestaurantGeocoder(clientBuilder.build());

        server.expect(queryParam("format", "jsonv2"))
                .andRespond(withSuccess("""
                        [
                          {
                            "place_id": 123,
                            "lat": "48.77",
                            "lon": "9.15",
                            "boundingbox": ["48.76", "48.78", "9.14", "9.16"]
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        assertThat(geocoder.geocode(new Address("Teststrasse", "2", "70176", "Stuttgart")))
                .contains(new GeoJsonPoint(9.15, 48.77));
        server.verify();
    }

    @Test
    void geocodeSkipsRequestWhenAddressIsMissingOrBlank() {
        var clientBuilder = RestClient.builder().baseUrl("https://example.test");
        var server = MockRestServiceServer.bindTo(clientBuilder).build();
        var geocoder = new NominatimRestaurantGeocoder(clientBuilder.build());

        assertThat(geocoder.geocode(null)).isEmpty();
        assertThat(geocoder.geocode(new Address(null, " ", " ", null))).isEmpty();

        // Ohne registrierte Erwartung würde jeder HTTP-Aufruf den Test fehlschlagen lassen.
        server.verify();
    }

    @Test
    void geocodeCachesNoMatchAndDoesNotSendAnotherRequest() {
        var clientBuilder = RestClient.builder().baseUrl("https://example.test");
        var server = MockRestServiceServer.bindTo(clientBuilder).build();
        var geocoder = new NominatimRestaurantGeocoder(clientBuilder.build());
        var address = new Address("Teststrasse", "2", "70176", "Stuttgart");

        server.expect(queryParam("street", "Teststrasse%202"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(geocoder.geocode(address)).isEmpty();
        assertThat(geocoder.geocode(address)).isEmpty();

        server.verify();
    }

    @Test
    void geocodeReturnsNoCoordinatesWhenNominatimFails() {
        var clientBuilder = RestClient.builder().baseUrl("https://example.test");
        var server = MockRestServiceServer.bindTo(clientBuilder).build();
        var geocoder = new NominatimRestaurantGeocoder(clientBuilder.build());
        var address = new Address("Teststrasse", "2", "70176", "Stuttgart");

        server.expect(queryParam("street", "Teststrasse%202"))
                .andRespond(withServerError());

        assertThat(geocoder.geocode(address)).isEmpty();

        server.verify();
    }
}
