package car.autoSpotterBot.repository.realEstate;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    List<ParkingSpace> findByStandort(Standort standort);
    List<ParkingSpace> findByUserId(Long id);
    Optional<ParkingSpace> findByIdAndUserId(Long adId, Long userId);
}
