package car.autoSpotterBot.service;

import car.autoSpotterBot.exception.UserNotFoundException;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.repository.BotUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotUserService {

    private final BotUserRepository botUserRepository;

    @Autowired
    public BotUserService(BotUserRepository userRepository) {
        this.botUserRepository = userRepository;
    }

    // Methode, um einen neuen Benutzer hinzuzufügen
    public BotUser addUser(BotUser user) {
        return botUserRepository.save(user);
    }
    public BotUser findByTelegramId(Long telegramId) {
        return botUserRepository.findByTelegramId(telegramId);
    }

    public BotUser save(BotUser botUser) {
        return botUserRepository.save(botUser);
    }
    // Methode, um einen bestimmten Benutzer anhand seiner ID zu finden
    public BotUser findById(Long id) {
        return botUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // Methode, um alle Benutzer abzurufen
    public List<BotUser> findAll() {
        return botUserRepository.findAll();
    }

    public BotUser updateUser(BotUser user) {
        if (botUserRepository.existsById(user.getId())) {
            return botUserRepository.save(user);
        }
        throw new UserNotFoundException(user.getId());
    }

    // Methode, um einen Benutzer zu löschen
    public void deleteUser(Long id) {
        if (botUserRepository.existsById(id)) {
            botUserRepository.deleteById(id);
        } else {
            throw new UserNotFoundException(id);
        }
    }

}
