package car.autoSpotterBot.repository.transport;

import car.autoSpotterBot.model.transport.Automobile;
import car.autoSpotterBot.model.Standort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutomobileRepository extends JpaRepository<Automobile, Long> {
    List<Automobile> findByStandort(Standort standort);
    List<Automobile> findByUserId(Long id);
    Optional<Automobile> findByIdAndUserId(Long adId, Long userId);

}
