package de.unistuttgart.fmi.osmfaprajava;


import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RestaurantRepository extends CrudRepository<Restaurant, UUID> {
}
