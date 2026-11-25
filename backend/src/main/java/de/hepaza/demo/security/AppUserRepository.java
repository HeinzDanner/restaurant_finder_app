package de.hepaza.demo.security;

import de.hepaza.demo.security.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends MongoRepository<AppUser, String> {
}
