package car.autoSpotterBot.service;

import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.Stadt;
import car.autoSpotterBot.repository.AdRepository;
import car.autoSpotterBot.repository.StadtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdService {

    private final AdRepository adRepository;
    @Autowired
    private StadtRepository stadtRepository;
    @Autowired
    public AdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    // Find all ads
    public List<Ad> findAll() {
        return adRepository.findAll();
    }

    // Find ad by ID
    public Ad findById(Long id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));
    }

    // Save a new ad
    public Ad save(Ad ad) {
        return adRepository.save(ad);
    }

    // Update an existing ad
    public Ad updateAd(Ad ad) {
        if (adRepository.existsById(ad.getId())) {
            return adRepository.save(ad);
        }
        throw new AdNotFoundException(ad.getId());
    }

    // Delete an ad by ID
    public void deleteById(Long id) {
        if (adRepository.existsById(id)) {
            adRepository.deleteById(id);
        } else {
            throw new AdNotFoundException(id);
        }
    }
    public void saveAdWithLocation(Long adId, String stadtName) {
        // Eine Stadt aus der Datenbank anhand ihres Namens abrufen
        Stadt stadt = stadtRepository.findByName(stadtName);

        // Wenn die Stadt nicht existiert, erstellen Sie eine neue
        if(stadt == null) {
            stadt = new Stadt();
            stadt.setName(stadtName);
            stadtRepository.save(stadt);
        }

        // Die Anzeige aus der Datenbank anhand ihrer ID abrufen
        Ad ad = adRepository.findById(adId).orElse(null);

        if(ad != null) {
            // Die Stadt als Standort für die Anzeige festlegen
            ad.setStandort(stadt);

            // Die aktualisierte Anzeige in der Datenbank speichern
            adRepository.save(ad);
        }
    }
    // Finden Sie Anzeigen basierend auf einer bestimmten Stadt
    public List<Ad> findByStadt(String stadtName) {
        Stadt stadt = stadtRepository.findByName(stadtName);
        if (stadt != null) {
            return adRepository.findByStandort(stadt);
        }
        return new ArrayList<>();
    }

    public Ad updateAdLocation(Long adId, String newStadtName) {
        Ad ad = adRepository.findById(adId).orElseThrow(() -> new AdNotFoundException(adId));
        Stadt newStadt = stadtRepository.findByName(newStadtName);
        if (newStadt == null) {
            newStadt = new Stadt();
            newStadt.setName(newStadtName);
            stadtRepository.save(newStadt);
        }
        ad.setStandort(newStadt);
        return adRepository.save(ad);
    }
}
