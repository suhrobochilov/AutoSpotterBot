package car.autoSpotterBot.service.realEstate;


import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.BusinessPremise;
import car.autoSpotterBot.model.transport.Favorit;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.StandortRepository;
import car.autoSpotterBot.repository.realEstate.BusinessPremiseRepository;
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
public class BusinessPremiseService {
    private static final Logger log = LoggerFactory.getLogger(ApartmentService.class);
    private final BusinessPremiseRepository businessPremiseRepository;
    @Autowired
    private StandortRepository standortRepository;
    @Autowired
    private BotUserRepository botUserRepository;
    @Autowired
    private FavoritRepository favoritRepository;

    public BusinessPremiseService(BusinessPremiseRepository businessPremiseRepository) {
        this.businessPremiseRepository = businessPremiseRepository;
    }

    public List<BusinessPremise> findAll() {
        return businessPremiseRepository.findAll();
    }

    public BusinessPremise findById(Long id) {
        return businessPremiseRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));
    }

    public void savePremise(BusinessPremise automobile) {
        businessPremiseRepository.save(automobile);
    }

    public List<BusinessPremise> findByStandort(String stadtName) {
        Standort standort = standortRepository.findByName(stadtName);
        if (standort != null) {
            return businessPremiseRepository.findByStandort(standort);
        }
        return new ArrayList<>();
    }

    public List<BusinessPremise> findByUserId(Long userId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user != null) {
            return businessPremiseRepository.findByUserId(user.getId());
        }
        return new ArrayList<>();
    }

    public void addFavorite(Long userId, Long adId) {
        log.info("UserId: " + userId);
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        BusinessPremise ad = findById(adId);
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
    
    public List<BusinessPremise> getFavoritesByUserId(Long chatId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        List<Favorit> favorites = favoritRepository.findByUserId(user.getId());
        List<BusinessPremise> automobiles = new ArrayList<>();
        for (Favorit favorite : favorites) {
            if (favorite.getTransport() instanceof BusinessPremise) {
                automobiles.add((BusinessPremise) favorite.getTransport());
            }
        }
        return automobiles;
    }

    public boolean deleteAdByUserIdAndAdId(Long chatId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        Optional<BusinessPremise> ad = businessPremiseRepository.findByIdAndUserId(adId, user.getId());
        log.info("UserId: " + user.getId() + " adId: " + ad.get().getId());
        businessPremiseRepository.deleteById(ad.get().getId());
        return true;
    }
    public boolean deleteById(Long id) {
        if (businessPremiseRepository.existsById(id)) {
            businessPremiseRepository.deleteById(id);
        } else {
            throw new AdNotFoundException(id);
        }
        return true;
    }
}
