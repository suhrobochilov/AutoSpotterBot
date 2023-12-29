package car.autoSpotterBot.service;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.*;
import car.autoSpotterBot.model.transport.*;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.transport.FavoritRepository;
import car.autoSpotterBot.repository.transport.TransportRepository;
import car.autoSpotterBot.service.realEstate.*;
import car.autoSpotterBot.service.transport.*;
import car.autoSpotterBot.util.MessageId;
import car.autoSpotterBot.util.MessageType;
import car.autoSpotterBot.util.transportUtils.AutoInterpreter;
import car.autoSpotterBot.util.transportUtils.BotCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeneralService {
    private static final Logger log = LoggerFactory.getLogger(AutoInterpreter.class);
    private static final int PAGE_SIZE = 10;
    private final BotUserService userService;
    private final Button button;
    private final StandortService standortService;
    private final AutomobileService automobileService;
    private final TruckService truckService;
    private final AgroTechnologyService agroTechService;
    private final OtherTransportService otherTransService;
    private final SparePartsService sparePartsService;
    private final ApartmentService apartmentService;
    private final BuildingLotService buildingLotService;
    private final BusinessPremiseService businessPremiseService;
    private final HouseService houseService;
    private final ParkingSpaceService parkingSpaceService;
    private final RentalHomeService rentalHomeService;
    private final BotCallback botCallback;
    private final MessageId messageIds;
    private final TransportRepository transportRepository;
    private final FavoritRepository favoritRepository;
    private final BotUserRepository botUserRepository;
    private final MessageId messageId;

    private final Map<Long, Integer> photoIndexMap = new HashMap<>();
    public Map<Long, Integer> userPageState = new HashMap<>();

    public GeneralService(BotUserService userService, Button button, StandortService standortService, AutomobileService automobileService, TruckService truckService, AgroTechnologyService agroTechService, OtherTransportService otherTransService, SparePartsService sparePartsService, ApartmentService apartmentService, BuildingLotService buildingLotService, BusinessPremiseService businessPremiseService, HouseService houseService, ParkingSpaceService parkingSpaceService, RentalHomeService rentalHomeService, BotCallback botCallback, MessageId messageIds, TransportRepository transportRepository, FavoritRepository favoritRepository, BotUserRepository botUserRepository, MessageId messageId) {
        this.userService = userService;
        this.button = button;
        this.standortService = standortService;
        this.automobileService = automobileService;
        this.truckService = truckService;
        this.agroTechService = agroTechService;
        this.transportRepository = transportRepository;
        this.favoritRepository = favoritRepository;
        this.botUserRepository = botUserRepository;
        this.otherTransService = otherTransService;
        this.sparePartsService = sparePartsService;
        this.apartmentService = apartmentService;
        this.buildingLotService = buildingLotService;
        this.businessPremiseService = businessPremiseService;
        this.houseService = houseService;
        this.parkingSpaceService = parkingSpaceService;
        this.rentalHomeService = rentalHomeService;
        this.botCallback = botCallback;
        this.messageIds = messageIds;
        this.messageId = messageId;
    }

    public <T extends Ad> void searchAd(Long chatId, String searchText, Class<T> adClass) {
        List<T> ads = findAds(adClass, searchText);
        log.info("SearchText is {}", searchText);
        if (ads.isEmpty()) {
            Message message = botCallback.sendMessageWithInlKeyboard(chatId, "Birorta ham e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
            botCallback.deleteMessageLater(chatId, message.getMessageId(), 5);
            return;
        }
        displayAdsAtSearch(chatId, ads);
    }

    private <T extends Ad> List<T> findAds(Class<T> adClass, String stadtName) {
        if (Automobile.class.equals(adClass)) {
            return castList(automobileService.findByStandort(stadtName), adClass);
        } else if (Truck.class.equals(adClass)) {
            return castList(truckService.findByStandort(stadtName), adClass);
        } else if (AgroTechnology.class.equals(adClass)) {
            return castList(agroTechService.findByStandort(stadtName), adClass);
        } else if (SpareParts.class.equals(adClass)) {
            return castList(sparePartsService.findByStandort(stadtName), adClass);
        } else if (OtherTransport.class.equals(adClass)) {
            return castList(otherTransService.findByStandort(stadtName), adClass);
        } else if (Apartment.class.equals(adClass)) {
            return castList(apartmentService.findByStandort(stadtName), adClass);
        } else if (ParkingSpace.class.equals(adClass)) {
            return castList(parkingSpaceService.findByStandort(stadtName), adClass);
        } else if (House.class.equals(adClass)) {
            return castList(houseService.findByStandort(stadtName), adClass);
        } else if (BuildingLot.class.equals(adClass)) {
            return castList(buildingLotService.findByStandort(stadtName), adClass);
        } else if (RentalHome.class.equals(adClass)) {
            return castList(rentalHomeService.findByStandort(stadtName), adClass);
        } else if (BusinessPremise.class.equals(adClass)) {
            return castList(businessPremiseService.findByStandort(stadtName), adClass);
        }
        return new ArrayList<>();
    }

    private <T> List<T> castList(List<?> list, Class<T> clazz) {
        return list.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public void setStandort(String cityName, Ad currentTransport) {
        Standort standort = standortService.saveCity(cityName);
        currentTransport.setStandort(standort);
    }

    public void getNextPhoto(Long chatId, String text, Integer messageId, Class<? extends Ad> adClass) {
        Long adId = getAdId(text);
        Ad ad = getAdById(adId, adClass);
        assert ad != null;
        String description = ad.getDescription();
        List<String> imageUrls = ad.getImageUrl();
        String videoUrl = ad.getVideoUrl();
        int currentIndex = photoIndexMap.getOrDefault(chatId, 0);
        int nextIndex = (currentIndex + 1) % imageUrls.size();
        int previousIndex = (currentIndex - 1) % imageUrls.size();
        if (text.startsWith(ButtonConstant.nextPhoto)) {
            if (imageUrls.size() >= 2) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, nextIndex);
                botCallback.editImageMessage(chatId, messageId, "E'lon id raqami: " + ad.getId() + "\n" + description, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(ButtonConstant.previousPhoto)) {
            if (currentIndex != 0) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, previousIndex);
                botCallback.editImageMessage(chatId, messageId, "E'lon id raqami: " + ad.getId() + "\n" + description, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(ButtonConstant.video)) {
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, nextIndex);
            botCallback.editImageMessage(chatId, messageId, "E'lon id raqami: " + ad.getId() + "\n" + description, null, videoUrl, newKeyboard);
            photoIndexMap.put(chatId, nextIndex);
        } else if (text.startsWith(ButtonConstant.favorite)) {
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardAddFav(adId, nextIndex);
            botCallback.editImageMessage(chatId, messageId, "E'lon id raqami: " + ad.getId() + "\n" + description, null, null, newKeyboard);
        }
    }
    public void getNextPhoto(Long chatId, String text, Integer messageId) {
        Long adId = getAdId(text);
        String className = getClassName(text);
        log.info("ClassName in getNextPhoto in GenerelService: " + className + " text: " + text);
        Ad ad = getAdById(adId, className);
        assert ad != null;
        String description = ad.getDescription();
        List<String> imageUrls = ad.getImageUrl();
        String videoUrl = ad.getVideoUrl();
        int currentIndex = photoIndexMap.getOrDefault(chatId, 0);
        int nextIndex = (currentIndex + 1) % imageUrls.size();
        int previousIndex = (currentIndex - 1) % imageUrls.size();
        if (text.startsWith(ButtonConstant.nextPhoto)) {
            if (imageUrls.size() >= 2) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForMyAds(adId, nextIndex, className);
                botCallback.editImageMessage(chatId, messageId, "E'lon id raqami: " + ad.getId() + "\n" + description, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(ButtonConstant.previousPhoto)) {
            if (currentIndex != 0) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForMyAds(adId, previousIndex, className);
                botCallback.editImageMessage(chatId, messageId, "E'lon id raqami: " + ad.getId() + "\n" + description, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(ButtonConstant.video)) {
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardForMyAds(adId, nextIndex, className);
            botCallback.editImageMessage(chatId, messageId, "E'lon id raqami: " + ad.getId() + "\n" + description, null, videoUrl, newKeyboard);
            photoIndexMap.put(chatId, nextIndex);
        }
    }

    public void saveUrl(String text, String photoUrl, String videoUrl, Ad currentAd) {
        if (photoUrl != null) {
            List<String> photoUrls = currentAd.getImageUrl();
            if (photoUrls == null) {
                photoUrls = new ArrayList<>();
                currentAd.setImageUrl(photoUrls);
            }
            photoUrls.add(photoUrl);
        } else {
            currentAd.setVideoUrl(videoUrl);
        }
        if (text != null) {
            currentAd.setDescription(text);
        }
    }

    private <T extends Ad> T getAdById(Long adId, Class<T> adClass) {
        if (adClass.equals(Automobile.class)) {
            return (T) automobileService.findById(adId);
        } else if (adClass.equals(Truck.class)) {
            return (T) truckService.findById(adId);
        } else if (adClass.equals(AgroTechnology.class)) {
            return (T) agroTechService.findById(adId);
        } else if (adClass.equals(OtherTransport.class)) {
            return (T) otherTransService.findById(adId);
        } else if (adClass.equals(SpareParts.class)) {
            return (T) sparePartsService.findById(adId);
        } else if (adClass.equals(Apartment.class)) {
            return (T) apartmentService.findById(adId);
        } else if (adClass.equals(BuildingLot.class)) {
            return (T) buildingLotService.findById(adId);
        } else if (adClass.equals(BusinessPremise.class)) {
            return (T) businessPremiseService.findById(adId);
        } else if (adClass.equals(House.class)) {
            return (T) houseService.findById(adId);
        } else if (adClass.equals(ParkingSpace.class)) {
            return (T) parkingSpaceService.findById(adId);
        } else if (adClass.equals(RentalHome.class)) {
            return (T) rentalHomeService.findById(adId);
        }
        return null;
    }

    public <T extends Ad> T getAdById(Long adId, String className) {
        return switch (className) {
            case "Automobile" -> (T) automobileService.findById(adId);
            case "Truck" -> (T) truckService.findById(adId);
            case "AgroTechnology" -> (T) agroTechService.findById(adId);
            case "OtherTransport" -> (T) otherTransService.findById(adId);
            case "SpareParts" -> (T) sparePartsService.findById(adId);
            case "Apartment" -> (T) apartmentService.findById(adId);
            case "BuildingLot" -> (T) buildingLotService.findById(adId);
            case "BusinessPremise" -> (T) businessPremiseService.findById(adId);
            case "House" -> (T) houseService.findById(adId);
            case "ParkingSpace" -> (T) parkingSpaceService.findById(adId);
            case "RentalHome" -> (T) rentalHomeService.findById(adId);
            default -> null;
        };
    }

    private <T extends Ad> void displayAdsAtSearch(Long chatId, List<T> ads) {
        int totalAds = ads.size();
        int totalPages = (totalAds + PAGE_SIZE - 1) / PAGE_SIZE;
        int currentPage = userPageState.getOrDefault(chatId, 1);
        int endIndex = totalAds - (currentPage - 1) * PAGE_SIZE;
        int startIndex = Math.max(endIndex - PAGE_SIZE, 0);
        for (int i = startIndex; i < endIndex; i++) {
            T ad = ads.get(i);
            getAd(chatId, ad);
        }
        InlineKeyboardMarkup inlineKeyboard = button.createInlineKeyboardForPages(currentPage, totalPages);
        if (!ads.isEmpty()) {
            Message message = botCallback.sendMessageWithInlKeyboard(chatId, "Keyingi e'lonlarni ko'rish uchun \uD83D\uDC47", inlineKeyboard);
            messageIds.setMessageId(chatId, MessageType.ID_OF_SEARCH_BUTTON, message.getMessageId());
        } else {
            Message message = botCallback.sendMessageWithInlKeyboard(chatId, "Birorta ham e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
            botCallback.deleteMessageLater(chatId, message.getMessageId(), 5);
        }
    }

    public void displayNextPage(Long chatId, Class<? extends Ad> adClass, int requestedPage) {
        List<? extends Ad> ads = getAdsByClass(adClass);
        userPageState.put(chatId, requestedPage);
        displayAdsAtSearch(chatId, ads);
    }

    private <T extends Ad> void getAd(Long chatId, T ad) {
        if (ad.getImageUrl() != null) {
            List<String> photoUrls = ad.getImageUrl();
            String photoUrl = ad.getImageUrl().isEmpty() ? null : ad.getImageUrl().get(0);
            String description = ad.getDescription();
            if (photoUrls != null && !photoUrls.isEmpty()) {
                int id = botCallback.sendPhotoWithInlKeyboard(chatId, "E'lon id raqami: " + ad.getId() + "\n" + description, photoUrl, button.inlKeyboardForAd(ad.getId(), null));
                messageIds.setMessageId(chatId, MessageType.ID_OF_SEARCHED_ADS, id);
            }
        } else if (ad.getVideoUrl() != null) {
            String videoUrl = ad.getVideoUrl();
            String description = ad.getDescription();
            int id = botCallback.sendVideoWithInlKeyboard(chatId, "E'lon id raqami: " + ad.getId() + "\n" + description, videoUrl, button.inlKeyboardForAd(ad.getId(), null));
            messageIds.setMessageId(chatId, MessageType.ID_OF_SEARCHED_ADS, id);
        }
    }

    public void finalizeAndSaveAd(Long chatId, Ad currentAd) {
        BotUser user = userService.findByTelegramId(chatId);
        currentAd.setUser(user);
        if (currentAd instanceof Automobile) {
            automobileService.saveAutomobile((Automobile) currentAd);
        } else if (currentAd instanceof Truck) {
            truckService.saveTruck((Truck) currentAd);
        } else if (currentAd instanceof AgroTechnology) {
            agroTechService.saveAgroTech((AgroTechnology) currentAd);
        } else if (currentAd instanceof SpareParts) {
            sparePartsService.saveSpareParts((SpareParts) currentAd);
        } else if (currentAd instanceof OtherTransport) {
            otherTransService.saveOtherTech((OtherTransport) currentAd);
        } else if (currentAd instanceof Apartment) {
            apartmentService.saveApartment((Apartment) currentAd);
        } else if (currentAd instanceof ParkingSpace) {
            parkingSpaceService.saveGarage((ParkingSpace) currentAd);
        } else if (currentAd instanceof House) {
            houseService.saveHouse((House) currentAd);
        } else if (currentAd instanceof BuildingLot) {
            buildingLotService.savePlot((BuildingLot) currentAd);
        } else if (currentAd instanceof BusinessPremise) {
            businessPremiseService.savePremise((BusinessPremise) currentAd);
        } else if (currentAd instanceof RentalHome) {
            rentalHomeService.saveRentHouse((RentalHome) currentAd);
        }
        Message message = botCallback.sendMessageWithInlKeyboard(chatId, "E'lon muvaffaqiyatli joylandi \uD83D\uDC4F" + "\n" + "yana e'lon joylash uchun " +
                "boshidan boshlang \uD83D\uDE1C", null);
        botCallback.deleteMessageLater(chatId, message.getMessageId(), 5);
    }

    public void addToFavorite(Long chatId, String callbackData, Class<? extends Ad> adClass) {
        BotUser user = userService.findByTelegramId(chatId);
        String[] parts = callbackData.split("_");
        Long adId = Long.valueOf(parts[1]);
        if (adClass.equals(Automobile.class)) {
            automobileService.addFavorite(chatId, adId);
        } else if (adClass.equals(Truck.class)) {
            truckService.addFavorite(chatId, adId);
        } else if (adClass.equals(AgroTechnology.class)) {
            agroTechService.addFavorite(chatId, adId);
        } else if (adClass.equals(OtherTransport.class)) {
            otherTransService.addFavorite(chatId, adId);
        } else if (adClass.equals(SpareParts.class)) {
            sparePartsService.addFavorite(chatId, adId);
        } else if (adClass.equals(Apartment.class)) {
            apartmentService.addFavorite(chatId, adId);
        } else if (adClass.equals(ParkingSpace.class)) {
            parkingSpaceService.addFavorite(chatId, adId);
        } else if (adClass.equals(House.class)) {
            houseService.addFavorite(chatId, adId);
        } else if (adClass.equals(BuildingLot.class)) {
            buildingLotService.addFavorite(chatId, adId);
        } else if (adClass.equals(BusinessPremise.class)) {
            businessPremiseService.addFavorite(chatId, adId);
        } else if (adClass.equals(RentalHome.class)) {
            rentalHomeService.addFavorite(chatId, adId);
        }
        userService.save(user);
    }

    public void deleteAd(Long chatId, String callbackData, int messageId) {
        String[] parts = callbackData.split("_");
        Long adId = Long.parseLong(parts[1]);
        boolean deleted = deleteById(adId);

        if (deleted) {
            botCallback.deleteMessage(chatId, messageId);
        } else {
            Message message = botCallback.sendMessageWithInlKeyboard(chatId, "Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.", null);
            botCallback.deleteMessageLater(chatId, message.getMessageId(), 5);
        }
    }

    public boolean deleteById(Long id) {
        try {
            if (!transportRepository.existsById(id)) {
                throw new AdNotFoundException(id);
            }
            List<Favorit> favoriten = favoritRepository.findByTransportId(id);
            favoritRepository.deleteAll(favoriten);
            transportRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting the ad: " + e.getMessage(), e);
        }
    }

    private List<? extends Ad> getAdsByClass(Class<? extends Ad> adClass) {
        if (adClass.equals(Automobile.class)) {
            return automobileService.findAll();
        } else if (adClass.equals(Truck.class)) {
            return truckService.findAll();
        } else if (adClass.equals(AgroTechnology.class)) {
            return agroTechService.findAll();
        } else if (adClass.equals(OtherTransport.class)) {
            return otherTransService.findAll();
        } else if (adClass.equals(SpareParts.class)) {
            return sparePartsService.findAll();
        } else if (adClass.equals(Apartment.class)) {
            return apartmentService.findAll();
        } else if (adClass.equals(BuildingLot.class)) {
            return buildingLotService.findAll();
        } else if (adClass.equals(BusinessPremise.class)) {
            return businessPremiseService.findAll();
        } else if (adClass.equals(House.class)) {
            return houseService.findAll();
        } else if (adClass.equals(ParkingSpace.class)) {
            return parkingSpaceService.findAll();
        } else if (adClass.equals(RentalHome.class)) {
            return rentalHomeService.findAll();
        }
        return Collections.emptyList();
    }

    public void deleteAdFromFavorite(long chatId, String callbackData, int messageId) {
        String[] parts = callbackData.split("_");
        Long adId = Long.parseLong(parts[1]);
        removeFromFavorite(chatId, adId);
        botCallback.deleteMessage(chatId, messageId);
    }

    public void removeFromFavorite(long chatId, Long adId) {
        try {
            BotUser user = botUserRepository.findByTelegramId(chatId);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            favoritRepository.deleteByUserIdAndTransportId(user.getId(), adId);
        } catch (Exception e) {
            log.error("Error removing from favorites: " + e.getMessage());
        }
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

        List<OtherTransport> otherTrans = otherTransService.findByUserId(chatId);
        displayMyAd(chatId, otherTrans);
        List<Apartment> apartments = apartmentService.findByUserId(chatId);
        displayMyAd(chatId, apartments);
        List<House> houses = houseService.findByUserId(chatId);
        displayMyAd(chatId, houses);
    }

    private <T extends Ad> void displayMyAd(Long chatId, List<T> ads) {
        if (!ads.isEmpty()) {
            for (T ad : ads) {
                String className = ad.getClass().getSimpleName();
                log.info("ClassName in DisplayMyAd: " + className);
                String photoUrl = ad.getImageUrl().isEmpty() ? null : ad.getImageUrl().get(0);
                String description = ad.getDescription();
                int id = botCallback.sendPhotoWithInlKeyboard(chatId, description, photoUrl, button.inlKeyboardForMyAds(ad.getId(), null, className));
                messageId.setMessageId(chatId, MessageType.ID_OF_MY_ADS, id);
            }
        }
    }

    public void getMyFavorite(Long chatId) {
        List<Ad> allFavorites = new ArrayList<>();
        allFavorites.addAll(automobileService.getFavoritesByUserId(chatId));
        allFavorites.addAll(truckService.getFavoritesByUserId(chatId));
        allFavorites.addAll(agroTechService.getFavoritesByUserId(chatId));
        allFavorites.addAll(sparePartsService.getFavoritesByUserId(chatId));
        allFavorites.addAll(otherTransService.getFavoritesByUserId(chatId));
        displayMyFavorites(chatId, allFavorites);
    }

    private void displayMyFavorites(Long chatId, List<? extends Ad> ads) {
        if (!ads.isEmpty()) {
            for (Ad ad : ads) {
                String photoUrl = ad.getImageUrl().isEmpty() ? null : ad.getImageUrl().get(0);
                String description = ad.getDescription();
                int id = botCallback.sendPhotoWithInlKeyboard(chatId, description, photoUrl, button.inlKeyboardMyFavorite(ad.getId(), null));
                messageId.setMessageId(chatId, MessageType.ID_OF_FAVORITED_ADS, id);
            }
        } else {
            Message message = botCallback.sendMessageWithInlKeyboard(chatId, "Sizda saralangan e'lon jo'qku \uD83D\uDE05", null);
            botCallback.deleteMessageLater(chatId, message.getMessageId(), 5);
        }
    }


    private Long getAdId(String text) {
        String[] parts = text.split("_");
        return Long.parseLong(parts[1]);
    }
    private String getClassName(String text) {
        String[] parts = text.split("_");
        return parts[3];  // Returns the third part of the split string
    }
}
