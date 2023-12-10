package car.autoSpotterBot.repository.transport;

import car.autoSpotterBot.model.transport.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportRepository extends JpaRepository<Ad,Long> {
}
