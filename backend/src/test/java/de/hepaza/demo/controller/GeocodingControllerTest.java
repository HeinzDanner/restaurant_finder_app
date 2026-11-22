package de.hepaza.demo.controller;

import de.hepaza.demo.model.Address;
import de.hepaza.demo.service.NominatimRestaurantGeocoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GeocodingControllerTest {

    @Mock
    private NominatimRestaurantGeocoder geocoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GeocodingController(geocoder))
                .build();
    }

    @Test
    void returnsGeoJsonCoordinatesForFoundAddress() throws Exception {
        Address address = new Address("Schlossplatz", "", "70173", "Stuttgart");

        when(geocoder.geocodeStartAddress(address))
                .thenReturn(Optional.of(new GeoJsonPoint(9.18, 48.78)));

        mockMvc.perform(post("/api/geocoding/start-address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "street": "Schlossplatz",
                                  "buildingNumber": "",
                                  "postcode": "70173",
                                  "city": "Stuttgart"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Point"))
                .andExpect(jsonPath("$.coordinates[0]").value(9.18))
                .andExpect(jsonPath("$.coordinates[1]").value(48.78));
    }

    @Test
    void returnsNotFoundWhenAddressHasNoMatch() throws Exception {
        Address address = new Address("Unbekannt", "", "", "Stuttgart");

        when(geocoder.geocodeStartAddress(address))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/geocoding/start-address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "street": "Unbekannt",
                                  "buildingNumber": "",
                                  "postcode": "",
                                  "city": "Stuttgart"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsAddressWithoutCityAndPostcode() throws Exception {
        mockMvc.perform(post("/api/geocoding/start-address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "street": "Schlossplatz",
                                  "buildingNumber": "",
                                  "postcode": "",
                                  "city": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(geocoder);
    }
}