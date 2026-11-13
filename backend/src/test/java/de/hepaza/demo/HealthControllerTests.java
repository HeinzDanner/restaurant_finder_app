package de.hepaza.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HealthControllerTests {

    @Test
    void reportsConnectedDatabaseWhenPingSucceeds() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        when(mongoTemplate.executeCommand(new Document("ping", 1))).thenReturn(new Document("ok", 1));

        ResponseEntity<Map<String, String>> response = new HealthController(mongoTemplate).health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("connected", response.getBody().get("database"));
    }

    @Test
    void reportsUnavailableDatabaseWhenPingFails() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        when(mongoTemplate.executeCommand(new Document("ping", 1)))
                .thenThrow(new DataAccessResourceFailureException("MongoDB is unavailable"));

        ResponseEntity<Map<String, String>> response = new HealthController(mongoTemplate).health();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("unavailable", response.getBody().get("database"));
    }

    @Test
    void healthEndpointReturnsConnectedStatus() throws Exception {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        when(mongoTemplate.executeCommand(new Document("ping", 1)))
                .thenReturn(new Document("ok", 1));

        MockMvcBuilders.standaloneSetup(new HealthController(mongoTemplate)).build()
                .perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.database").value("connected"));

        verify(mongoTemplate).executeCommand(new Document("ping", 1));
    }

    @Test
    void healthEndpointReturns503WhenDatabaseIsUnavailable() throws Exception {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        when(mongoTemplate.executeCommand(new Document("ping", 1)))
                .thenThrow(new DataAccessResourceFailureException("MongoDB is unavailable"));

        MockMvcBuilders.standaloneSetup(new HealthController(mongoTemplate)).build()
                .perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.database").value("unavailable"));
    }
}
