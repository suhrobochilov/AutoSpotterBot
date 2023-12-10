package car.autoSpotterBot.repository.realEstate;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.BusinessPremise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessPremiseRepository extends JpaRepository<BusinessPremise, Long> {
    List<BusinessPremise> findByStandort(Standort standort);

    List<BusinessPremise> findByUserId(Long id);

    Optional<BusinessPremise> findByIdAndUserId(Long adId, Long userId);
}
