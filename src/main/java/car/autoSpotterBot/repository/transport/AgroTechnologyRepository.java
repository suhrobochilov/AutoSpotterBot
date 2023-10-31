package car.autoSpotterBot.repository.transport;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.AgroTechnology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgroTechnologyRepository extends JpaRepository<AgroTechnology, Long> {
    List<AgroTechnology> findByStandort(Standort standort);
    List<AgroTechnology> findByUserId(Long id);
    Optional<AgroTechnology> findByIdAndUserId(Long adId, Long userId);
}
