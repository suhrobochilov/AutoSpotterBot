package car.autoSpotterBot.service.realEstate;

import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.ParkingSpace;
import car.autoSpotterBot.model.transport.Favorit;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.StandortRepository;
import car.autoSpotterBot.repository.realEstate.ParkingSpaceRepository;
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
public class ParkingSpaceService {
    private static final Logger log = LoggerFactory.getLogger(ApartmentService.class);
    private final ParkingSpaceRepository parkingSpaceRepository;
    @Autowired
    private StandortRepository standortRepository;
    @Autowired
    private BotUserRepository botUserRepository;
    @Autowired
    private FavoritRepository favoritRepository;

    public ParkingSpaceService(ParkingSpaceRepository parkingSpaceRepository) {
        this.parkingSpaceRepository = parkingSpaceRepository;
    }

    public List<ParkingSpace> findAll() {
        return parkingSpaceRepository.findAll();
    }

    public ParkingSpace findById(Long id) {
        return parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));
    }

    public ParkingSpace saveGarage(ParkingSpace automobile) {
        return parkingSpaceRepository.save(automobile);
    }

    public List<ParkingSpace> findByStandort(String stadtName) {
        Standort standort = standortRepository.findByName(stadtName);
        if (standort != null) {
            return parkingSpaceRepository.findByStandort(standort);
        }
        return new ArrayList<>();
    }

    public List<ParkingSpace> findByUserId(Long userId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user != null) {
            return parkingSpaceRepository.findByUserId(user.getId());
        }
        return new ArrayList<>();
    }

    public void addFavorite(Long userId, Long adId) {
        log.info("UserId: " + userId);
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        ParkingSpace ad = findById(adId);
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


    public List<ParkingSpace> getFavoritesByUserId(Long chatId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        List<Favorit> favorites = favoritRepository.findByUserId(user.getId());
        List<ParkingSpace> automobiles = new ArrayList<>();
        for (Favorit favorite : favorites) {
            if (favorite.getTransport() instanceof ParkingSpace) {
                automobiles.add((ParkingSpace) favorite.getTransport());
            }
        }
        return automobiles;
    }

    public boolean deleteAdByUserIdAndAdId(Long chatId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        Optional<ParkingSpace> ad = parkingSpaceRepository.findByIdAndUserId(adId, user.getId());
        log.info("UserId: " + user.getId() + " adId: " + ad.get().getId());
        parkingSpaceRepository.deleteById(ad.get().getId());
        return true;
    }


    public boolean deleteById(Long id) {
        if (parkingSpaceRepository.existsById(id)) {
            parkingSpaceRepository.deleteById(id);
        } else {
            throw new AdNotFoundException(id);
        }
        return true;
    }
}
