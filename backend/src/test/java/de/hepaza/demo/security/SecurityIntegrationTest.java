package de.hepaza.demo.security;

import de.hepaza.demo.model.Restaurant;
import de.hepaza.demo.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // ---------- Gutfälle ----------

    @Test
    @DirtiesContext
    void getRestaurants_notLoggedIn_isAllowed_expect200() throws Exception {
        // Lesen ist für alle offen
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    void createUser_isSavedInDatabase() {
        // 1. Neuen User anlegen
        AppUser newUser = AppUser.builder()
                .id("456")
                .username("neuer-user")
                .avatarUrl("https://avatar.url/neu")
                .role("USER")
                .build();

        appUserRepository.save(newUser);

        // 2. Prüfen, ob er in der DB steht – mit allen Feldern
        assertThat(appUserRepository.count()).isEqualTo(1);
        assertThat(appUserRepository.findById("456")).contains(newUser);
    }

    @Test
    @DirtiesContext
    void createUser_thenGetMe_returnsNewUser() throws Exception {
        // 1. Neuen User mit Rolle ADMIN anlegen
        appUserRepository.save(new AppUser("789", "admin-user", "avatarUrl", "ADMIN"));

        // 2. Als dieser User eingeloggt (GitHub-ID 789)
        OAuth2User githubUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ADMIN")),
                Map.of("id", "789", "login", "admin-user"),
                "id"
        );

        // 3. /me muss genau diesen User aus der DB liefern
        mockMvc.perform(get("/api/auth/me")
                        .with(oauth2Login().oauth2User(githubUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("789"))
                .andExpect(jsonPath("$.username").value("admin-user"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(authorities = "USER")
    void deleteRestaurant_loggedInAsUser_existingId_expect204() throws Exception {
        // 1. Restaurant in die (Embedded-)DB speichern
        restaurantRepository.save(new Restaurant("r-1", "Testpizza"));

        // 2. Als USER löschen
        mockMvc.perform(delete("/api/restaurants/r-1"))
                .andExpect(status().isNoContent());

        // 3. Prüfen, dass es wirklich weg ist
        assertThat(restaurantRepository.existsById("r-1")).isFalse();
    }

    // ---------- Restaurant-Endpoints (POST / DELETE) ----------

    @Test
    @DirtiesContext
    void postRestaurant_notLoggedIn_expect401() throws Exception {
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext
    @WithMockUser
        // eingeloggt, aber nur "ROLE_USER"(gesetzt von mock) – nicht "USER"
    void postRestaurant_loggedInWithoutUserAuthority_expect403() throws Exception {
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DirtiesContext
    @WithMockUser(authorities = "USER")
    void postRestaurant_loggedInAsUser_passesSecurity_expect400() throws Exception {
        // Security lässt durch → Validierung meldet leeren Body mit 400
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext
    @WithMockUser(authorities = "USER")
    void deleteRestaurant_loggedInAsUser_unknownId_expect404() throws Exception {
        // Security lässt durch → Restaurant gibt es nicht → 404
        mockMvc.perform(delete("/api/restaurants/gibt-es-nicht"))
                .andExpect(status().isNotFound());
    }

    // ---------- /api/auth/me ----------

    @Test
    @DirtiesContext
    void getMe_notLoggedIn_expect401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DirtiesContext
    void getMe_returnsExistingDbUser() throws Exception {
        // 1. Existing user erstellen
        appUserRepository.save(new AppUser("123", "username", "avatarUrl", "USER"));

        // 2. Simulierter GitHub user
        OAuth2User githubUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("USER")),
                Map.of("id", "123", "login", "username", "avatar_url", "avatarUrl"),
                "id"
        );

        // 3. Request prüfen, ob gewünschte Antwort zurückkommt
        mockMvc.perform(get("/api/auth/me")
                        .with(oauth2Login().oauth2User(githubUser)))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                        "id": "123",
                        "username": "username",
                        "avatarUrl": "avatarUrl",
                        "role": "USER"
                        }
                        """));
    }

    @Test
    @DirtiesContext
    void getMe_userNotInDb_expect404() throws Exception {
        OAuth2User githubUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("USER")),
                Map.of("id", "999", "login", "unbekannt"),
                "id"
        );

        mockMvc.perform(get("/api/auth/me")
                        .with(oauth2Login().oauth2User(githubUser)))
                .andExpect(status().isNotFound());
    }
}