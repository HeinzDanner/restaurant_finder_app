package de.hepaza.demo.security;

import lombok.Builder;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@With
@Document("user")
public record AppUser(
        String id,
        String username,
        String avatarUrl,
        String role
) {
}
