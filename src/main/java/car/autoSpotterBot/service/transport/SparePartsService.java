package car.autoSpotterBot.service.transport;

import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.SpareParts;
import car.autoSpotterBot.model.transport.Favorit;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.StandortRepository;
import car.autoSpotterBot.repository.transport.SparePartsRepository;
import car.autoSpotterBot.repository.transport.FavoritRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SparePartsService {
    private static final Logger log = LoggerFactory.getLogger(SparePartsService.class);

    private final SparePartsRepository sparePartsRepository;
    @Autowired
    private StandortRepository standortRepository;
    @Autowired
    private BotUserRepository botUserRepository;
    @Autowired
    private FavoritRepository favoritRepository;

    @Autowired
    public SparePartsService(SparePartsRepository sparePartsRepository) {
        this.sparePartsRepository = sparePartsRepository;
    }

    public List<SpareParts> findAll() {
        return sparePartsRepository.findAll();
    }

    public SpareParts findById(Long id) {
        return sparePartsRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));
    }

    public SpareParts saveSpareParts(SpareParts automobile) {
        return sparePartsRepository.save(automobile);
    }

    public List<SpareParts> findByStandort(String stadtName) {
        Standort standort = standortRepository.findByName(stadtName);
        if (standort != null) {
            return sparePartsRepository.findByStandort(standort);
        }
        return new ArrayList<>();
    }

    public List<SpareParts> findByUserId(Long userId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user != null) {
            return sparePartsRepository.findByUserId(user.getId());
        }
        return new ArrayList<>();
    }

    public void addFavorite(Long userId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        SpareParts ad = findById(adId);
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


    public List<SpareParts> getFavoritesByUserId(Long chatId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        List<Favorit> favorites = favoritRepository.findByUserId(user.getId());
        List<SpareParts> automobiles = new ArrayList<>();
        for (Favorit favorite : favorites) {
            if (favorite.getTransport() instanceof SpareParts) {
                automobiles.add((SpareParts) favorite.getTransport());
            }
        }
        return automobiles;
    }

    public boolean deleteAdByUserIdAndAdId(Long chatId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        Optional<SpareParts> ad = sparePartsRepository.findByIdAndUserId(adId, user.getId());
        log.info("UserId: " + user.getId() + " adId: " + ad.get().getId());
        sparePartsRepository.deleteById(ad.get().getId());
        return true;
    }

    public void deleteById(Long id) {
        if (sparePartsRepository.existsById(id)) {
            sparePartsRepository.deleteById(id);
        } else {
            throw new AdNotFoundException(id);
        }
    }
}
