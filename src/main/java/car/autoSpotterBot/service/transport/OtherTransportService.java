package car.autoSpotterBot.service.transport;

import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.Automobile;
import car.autoSpotterBot.model.transport.Favorit;
import car.autoSpotterBot.model.transport.OtherTransport;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.StandortRepository;
import car.autoSpotterBot.repository.transport.FavoritRepository;
import car.autoSpotterBot.repository.transport.OtherTransportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OtherTransportService {
    private static final Logger log = LoggerFactory.getLogger(AgroTechnologyService.class);
    private final OtherTransportRepository otherTransportRepository;
    @Autowired
    private StandortRepository standortRepository;
    @Autowired
    private BotUserRepository botUserRepository;
    @Autowired
    private FavoritRepository favoritRepository;

    public OtherTransportService(OtherTransportRepository otherTransportRepository) {
        this.otherTransportRepository = otherTransportRepository;
    }


    public List<OtherTransport> findAll() {
        return otherTransportRepository.findAll();
    }

    public OtherTransport findById(Long id) {
        return otherTransportRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));
    }
    public OtherTransport saveOtherTech(OtherTransport otherTransport) {
        return otherTransportRepository.save(otherTransport);
    }

    public List<OtherTransport> findByStandort(String stadtName) {
        Standort standort = standortRepository.findByName(stadtName);
        if (standort != null) {
            return otherTransportRepository.findByStandort(standort);
        }
        return new ArrayList<>();
    }

    public List<OtherTransport> findByUserId(Long userId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user != null) {
            return otherTransportRepository.findByUserId(user.getId());
        }
        return new ArrayList<>();
    }

    public void addFavorite(Long userId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        OtherTransport ad = findById(adId);
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

    public List<OtherTransport> getFavoritesByUserId(Long chatId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        List<Favorit> favorites = favoritRepository.findByUserId(user.getId());
        List<OtherTransport> otherTransports = new ArrayList<>();
        for (Favorit favorite : favorites) {
            if (favorite.getTransport() instanceof OtherTransport) {
                otherTransports.add((OtherTransport) favorite.getTransport());
            }
        }
        return otherTransports;
    }

    public boolean deleteAdByUserIdAndAdId(Long chatId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        Optional<OtherTransport> ad = otherTransportRepository.findByIdAndUserId(adId, user.getId());
        otherTransportRepository.deleteById(ad.get().getId());
        return true;
    }

    public void deleteById(Long id) {
        if (otherTransportRepository.existsById(id)) {
            otherTransportRepository.deleteById(id);
        } else {
            throw new AdNotFoundException(id);
        }
    }
}
