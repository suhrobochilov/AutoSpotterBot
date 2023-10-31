package car.autoSpotterBot.repository.transport;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.OtherTransport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtherTransportRepository extends JpaRepository<OtherTransport, Long> {
    List<OtherTransport> findByStandort(Standort standort);
    List<OtherTransport> findByUserId(Long id);
    Optional<OtherTransport> findByIdAndUserId(Long adId, Long userId);
}
