package car.autoSpotterBot.service;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.realeState.Apartment;
import car.autoSpotterBot.model.realeState.House;
import car.autoSpotterBot.model.transport.*;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.transport.FavoritRepository;
import car.autoSpotterBot.repository.transport.TransportRepository;
import car.autoSpotterBot.service.realEstate.ApartmentService;
import car.autoSpotterBot.service.realEstate.HouseService;
import car.autoSpotterBot.service.transport.*;
import car.autoSpotterBot.util.transportUtils.BotCallback;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdService {
    private final TransportRepository transportRepository;
    private final FavoritRepository favoritRepository;
    private final BotUserRepository botUserRepository;
    private final BotCallback botCallback;
    private final Button button;
    private final AutomobileService automobileService;
    private final TruckService truckService;
    private final AgroTechnologyService agroTechService;
    private final SparePartsService sparePartsService;
    private final OtherTransportService otherTransportService;
    private final ApartmentService apartmentService;
    private final HouseService houseService;


    public AdService(TransportRepository transportRepository, FavoritRepository favoritRepository, BotUserRepository botUserRepository, BotCallback botCallback, Button button, AutomobileService automobileService, TruckService truckService, AgroTechnologyService agroTechService, SparePartsService sparePartsService, OtherTransportService otherTransportService, ApartmentService apartmentService, HouseService houseService) {
        this.transportRepository = transportRepository;
        this.favoritRepository = favoritRepository;
        this.botUserRepository = botUserRepository;
        this.botCallback = botCallback;
        this.button = button;
        this.automobileService = automobileService;
        this.truckService = truckService;
        this.agroTechService = agroTechService;
        this.sparePartsService = sparePartsService;
        this.otherTransportService = otherTransportService;
        this.apartmentService = apartmentService;
        this.houseService = houseService;
    }

    public boolean deleteById(Long id) {
        if (!transportRepository.existsById(id)) {
            throw new AdNotFoundException(id);
        }
        List<Favorit> favoriten = favoritRepository.findByTransportId(id);
        favoritRepository.deleteAll(favoriten);

        transportRepository.deleteById(id);
        return true;
    }

    @Transactional
    public void removeFromFavorite(long chatId, Long adId) {
        BotUser user = botUserRepository.findByTelegramId(chatId);
        if (user == null) {
            throw new RuntimeException("User nicht gefunden");
        }
        favoritRepository.deleteByUserIdAndTransportId(user.getId(), adId);
    }

    public void getMyAds(Long chatId) {
        List<Automobile> myAutomobiles = automobileService.findByUserId(chatId);
        displayMyAd(chatId, myAutomobiles);

        List<Truck> myTrucks = truckService.findByUserId(chatId);
        displayMyAd(chatId, myTrucks);

        List<AgroTechnology> myAgroTechs = agroTechService.findByUserId(chatId);
        displayMyAd(chatId, myAgroTechs);

        List<SpareParts> mySpareParts = sparePartsService.findByUserId(chatId);
        displayMyAd(chatId, mySpareParts);

        List<OtherTransport> otherTrans = otherTransportService.findByUserId(chatId);
        displayMyAd(chatId, otherTrans);
        List<Apartment> apartments = apartmentService.findByUserId(chatId);
        displayMyAd(chatId, apartments);
        List<House> houses = houseService.findByUserId(chatId);
        displayMyAd(chatId, houses);
    }

    private <T extends Ad> void displayMyAd(Long chatId, List<T> ads) {
        if (!ads.isEmpty()) {
            for (T ad : ads) {
                String photoUrl = ad.getImageUrl().isEmpty() ? null : ad.getImageUrl().get(0);
                String description = ad.getDescription();
                botCallback.sendPhotoWithInlKeyboard(chatId, description, photoUrl, button.inlKeyboardForMyAds(ad.getId(), null));
            }
        }
    }
}
