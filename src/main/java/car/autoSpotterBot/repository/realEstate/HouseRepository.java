package car.autoSpotterBot.repository.realEstate;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.House;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HouseRepository extends JpaRepository<House, Long> {
    List<House> findByStandort(Standort standort);

    List<House> findByUserId(Long id);

    Optional<House> findByIdAndUserId(Long adId, Long userId);
}
