package car.autoSpotterBot.service.transport;

import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.AgroTechnology;
import car.autoSpotterBot.model.transport.Favorit;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.StandortRepository;
import car.autoSpotterBot.repository.transport.AgroTechnologyRepository;
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
public class AgroTechnologyService {
    private static final Logger log = LoggerFactory.getLogger(AgroTechnologyService.class);
    private final AgroTechnologyRepository agroTechnologyRepository;
    @Autowired
    private StandortRepository standortRepository;
    @Autowired
    private BotUserRepository botUserRepository;
    @Autowired
    private FavoritRepository favoritRepository;

    public AgroTechnologyService(AgroTechnologyRepository agroTechnologyRepository) {
        this.agroTechnologyRepository = agroTechnologyRepository;
    }

    public List<AgroTechnology> findAll() {
        return agroTechnologyRepository.findAll();
    }

    public AgroTechnology findById(Long id) {
        return agroTechnologyRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));
    }

    public AgroTechnology saveAgroTech(AgroTechnology agroTech) {
        return agroTechnologyRepository.save(agroTech);
    }

    public List<AgroTechnology> findByStandort(String stadtName) {
        Standort standort = standortRepository.findByName(stadtName);
        if (standort != null) {
            return agroTechnologyRepository.findByStandort(standort);
        }
        return new ArrayList<>();
    }

    public List<AgroTechnology> findByUserId(Long userId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user != null) {
            return agroTechnologyRepository.findByUserId(user.getId());
        }
        return new ArrayList<>();
    }

    public void addFavorite(Long userId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        AgroTechnology ad = findById(adId);
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

    public List<AgroTechnology> getFavoritesByUserId(Long chatId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        List<Favorit> favorites = favoritRepository.findByUserId(user.getId());
        List<AgroTechnology> agroTechnologies = new ArrayList<>();
        for (Favorit favorite : favorites) {
            if (favorite.getTransport() instanceof AgroTechnology) {
                agroTechnologies.add((AgroTechnology) favorite.getTransport());
            }
        }
        return agroTechnologies;
    }

    public boolean deleteAdByUserIdAndAdId(Long chatId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        Optional<AgroTechnology> ad = agroTechnologyRepository.findByIdAndUserId(adId, user.getId());
        agroTechnologyRepository.deleteById(ad.get().getId());
        return true;
    }


    public void deleteById(Long id) {
        if (agroTechnologyRepository.existsById(id)) {
            agroTechnologyRepository.deleteById(id);
        } else {
            throw new AdNotFoundException(id);
        }
    }
}
