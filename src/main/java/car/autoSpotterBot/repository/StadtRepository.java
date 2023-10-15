package car.autoSpotterBot.repository;

import car.autoSpotterBot.model.Stadt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadtRepository extends JpaRepository<Stadt, Long> {
    Stadt findByName(String stadtName);
}
