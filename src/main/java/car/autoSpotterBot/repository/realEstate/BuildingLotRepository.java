package car.autoSpotterBot.repository.realEstate;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.BuildingLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuildingLotRepository extends JpaRepository<BuildingLot, Long> {
    List<BuildingLot> findByStandort(Standort standort);
    List<BuildingLot> findByUserId(Long id);
    Optional<BuildingLot> findByIdAndUserId(Long adId, Long userId);
}
