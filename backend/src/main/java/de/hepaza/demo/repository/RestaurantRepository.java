package de.hepaza.demo.repository;

import de.hepaza.demo.model.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
}