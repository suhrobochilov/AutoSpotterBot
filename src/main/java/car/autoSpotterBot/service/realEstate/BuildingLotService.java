package car.autoSpotterBot.service.realEstate;

import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.BuildingLot;
import car.autoSpotterBot.model.transport.Favorit;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.StandortRepository;
import car.autoSpotterBot.repository.realEstate.BuildingLotRepository;
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
public class BuildingLotService {
    private static final Logger log = LoggerFactory.getLogger(ApartmentService.class);
    private final BuildingLotRepository buildingLotRepository;
    @Autowired
    private StandortRepository standortRepository;
    @Autowired
    private BotUserRepository botUserRepository;
    @Autowired
    private FavoritRepository favoritRepository;

    public BuildingLotService(BuildingLotRepository buildingLotRepository) {
        this.buildingLotRepository = buildingLotRepository;
    }

    public List<BuildingLot> findAll() {
        return buildingLotRepository.findAll();
    }

    public BuildingLot findById(Long id) {
        return buildingLotRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));
    }

    public void savePlot(BuildingLot automobile) {
        buildingLotRepository.save(automobile);
    }

    public List<BuildingLot> findByStandort(String stadtName) {
        Standort standort = standortRepository.findByName(stadtName);
        if (standort != null) {
            return buildingLotRepository.findByStandort(standort);
        }
        return new ArrayList<>();
    }

    public List<BuildingLot> findByUserId(Long userId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user != null) {
            return buildingLotRepository.findByUserId(user.getId());
        }
        return new ArrayList<>();
    }

    public void addFavorite(Long userId, Long adId) {
        log.info("UserId: " + userId);
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        BuildingLot ad = findById(adId);
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


    public List<BuildingLot> getFavoritesByUserId(Long chatId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        List<Favorit> favorites = favoritRepository.findByUserId(user.getId());
        List<BuildingLot> automobiles = new ArrayList<>();
        for (Favorit favorite : favorites) {
            if (favorite.getTransport() instanceof BuildingLot) {
                automobiles.add((BuildingLot) favorite.getTransport());
            }
        }
        return automobiles;
    }

    public boolean deleteAdByUserIdAndAdId(Long chatId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        Optional<BuildingLot> ad = buildingLotRepository.findByIdAndUserId(adId, user.getId());
        log.info("UserId: " + user.getId() + " adId: " + ad.get().getId());
        buildingLotRepository.deleteById(ad.get().getId());
        return true;
    }

    public boolean deleteById(Long id) {
        if (buildingLotRepository.existsById(id)) {
            buildingLotRepository.deleteById(id);
        } else {
            throw new AdNotFoundException(id);
        }
        return true;
    }
}
