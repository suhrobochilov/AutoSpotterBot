package car.autoSpotterBot.repository.transport;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.SpareParts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SparePartsRepository extends JpaRepository<SpareParts, Long> {
    List<SpareParts> findByStandort(Standort standort);
    List<SpareParts> findByUserId(Long id);
    Optional<SpareParts> findByIdAndUserId(Long adId, Long userId);
}
