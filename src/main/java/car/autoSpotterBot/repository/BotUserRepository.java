package car.autoSpotterBot.repository;

import car.autoSpotterBot.model.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotUserRepository extends JpaRepository<BotUser, Long> {
        BotUser findByTelegramId(Long telegramId);

}
