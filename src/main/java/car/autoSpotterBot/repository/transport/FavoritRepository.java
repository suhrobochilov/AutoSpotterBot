package car.autoSpotterBot.repository.transport;

import car.autoSpotterBot.model.transport.Favorit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritRepository extends JpaRepository<Favorit, Long> {
    void deleteByUserIdAndTransportId(Long id, Long adId);

    List<Favorit> findByUserId(Long id);

    List<Favorit> findByTransportId(Long id);
}
