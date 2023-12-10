package car.autoSpotterBot.repository.realEstate;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.RentalHome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalHomeRepository extends JpaRepository<RentalHome, Long> {
    List<RentalHome> findByStandort(Standort standort);
    List<RentalHome> findByUserId(Long id);
    Optional<RentalHome> findByIdAndUserId(Long adId, Long userId);
}
