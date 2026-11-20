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
}
