package car.autoSpotterBot.repository;

import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Stadt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {
    List<Ad> findByStandort(Stadt stadt);
    List<Ad> findByUserId(Long id);

}