package car.autoSpotterBot.repository;

import car.autoSpotterBot.model.Standort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandortRepository extends JpaRepository<Standort, Long> {
    Standort findByName(String stadtName);
}
