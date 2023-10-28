package car.autoSpotterBot.service;

import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Stadt;
import car.autoSpotterBot.repository.AdRepository;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.StadtRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdService {

    private final AdRepository adRepository;
    @Autowired
    private StadtRepository stadtRepository;
    @Autowired
    private BotUserRepository botUserRepository;
    @Autowired
    public AdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    // Find all ads
    public List<Ad> findAll() {
        return adRepository.findAll();
    }

    public Ad findById(Long id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));
    }

    // Save a new ad
    public Ad save(Ad ad) {
        return adRepository.save(ad);
    }

    public void deleteById(Long id) {
        if (adRepository.existsById(id)) {
            adRepository.deleteById(id);
        } else {
            throw new AdNotFoundException(id);
        }
    }
    public List<Ad> findByStadt(String stadtName) {
        Stadt stadt = stadtRepository.findByName(stadtName);
        if (stadt != null) {
            return adRepository.findByStandort(stadt);
        }
        return new ArrayList<>();
    }
    public List<Ad> findByUserId(Long userId) {
        BotUser user = botUserRepository.findByTelegramId(userId);
        if (user != null){
            return adRepository.findByUserId(user.getId());
        }
        return new ArrayList<>();
    }
    public void addFavorite(Long userId, Long adId) {
        BotUser user = botUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("User nicht gefunden"));
        Ad ad = adRepository.findById(adId).orElseThrow(() -> new RuntimeException("Ad nicht gefunden"));

        if (!user.getFavoriteAds().contains(ad)) {
            user.getFavoriteAds().add(ad);
            botUserRepository.save(user);
        }
        if (!ad.getFavoritedByUsers().contains(user)) {
            ad.getFavoritedByUsers().add(user);
            adRepository.save(ad);
        }
    }
    public Page<Ad> findAds(Pageable pageable) {
        return adRepository.findAll(pageable);
    }


    public void removeFromFavorites(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId).orElseThrow(() -> new RuntimeException("Ad not found"));
        BotUser user = botUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        ad.getFavoritedByUsers().remove(user);
        adRepository.save(ad);
    }
}
