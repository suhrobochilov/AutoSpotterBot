package car.autoSpotterBot.repository.transport;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TruckRepository extends JpaRepository<Truck, Long> {
    List<Truck> findByStandort(Standort standort);
    List<Truck> findByUserId(Long id);
    Optional<Truck> findByIdAndUserId(Long adId, Long userId);
}
