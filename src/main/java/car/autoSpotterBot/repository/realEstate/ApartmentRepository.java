package car.autoSpotterBot.repository.realEstate;

import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    List<Apartment> findByStandort(Standort standort);
    List<Apartment> findByUserId(Long id);
    Optional<Apartment> findByIdAndUserId(Long adId, Long userId);
}
