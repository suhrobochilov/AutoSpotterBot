package car.autoSpotterBot.service.realEstate;

import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.House;
import car.autoSpotterBot.model.transport.Favorit;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.StandortRepository;
import car.autoSpotterBot.repository.realEstate.HouseRepository;
import car.autoSpotterBot.repository.transport.FavoritRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HouseService {
    private static final Logger log = LoggerFactory.getLogger(ApartmentService.class);
    private final HouseRepository houseRepository;
    @Autowired
    private StandortRepository standortRepository;
    @Autowired
    private BotUserRepository botUserRepository;
    @Autowired
    private FavoritRepository favoritRepository;

    public HouseService(HouseRepository houseRepository) {
        this.houseRepository = houseRepository;
    }

    public List<House> findAll() {
        return houseRepository.findAll();
    }

    public House findById(Long id) {
        return houseRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));
    }

    public House saveHouse(House automobile) {
        return houseRepository.save(automobile);
    }

    public List<House> findByStandort(String stadtName) {
        Standort standort = standortRepository.findByName(stadtName);
        if (standort != null) {
            return houseRepository.findByStandort(standort);
        }
        return new ArrayList<>();
    }

    public List<House> findByUserId(Long userId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user != null) {
            return houseRepository.findByUserId(user.getId());
        }
        return new ArrayList<>();
    }

    public void addFavorite(Long userId, Long adId) {
        log.info("UserId: " + userId);
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        House ad = findById(adId);
        Favorit favorite = new Favorit();
        favorite.setUser(user);
        favorite.setTransport(ad);
        favoritRepository.save(favorite);
    }

    public void removeFromFavorites(Long adId, Long userId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        favoritRepository.deleteByUserIdAndTransportId(user.getId(), adId);
    }


    public List<House> getFavoritesByUserId(Long chatId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        List<Favorit> favorites = favoritRepository.findByUserId(user.getId());
        List<House> automobiles = new ArrayList<>();
        for (Favorit favorite : favorites) {
            if (favorite.getTransport() instanceof House) {
                automobiles.add((House) favorite.getTransport());
            }
        }
        return automobiles;
    }

    public boolean deleteAdByUserIdAndAdId(Long chatId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        Optional<House> ad = houseRepository.findByIdAndUserId(adId, user.getId());
        log.info("UserId: " + user.getId() + " adId: " + ad.get().getId());
        houseRepository.deleteById(ad.get().getId());
        return true;
    }


    public boolean deleteById(Long id) {
        if (houseRepository.existsById(id)) {
            houseRepository.deleteById(id);
        } else {
            throw new AdNotFoundException(id);
        }
        return true;
    }
}
