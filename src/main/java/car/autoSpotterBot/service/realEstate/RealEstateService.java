package car.autoSpotterBot.service.realEstate;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.*;
import car.autoSpotterBot.service.AdService;
import car.autoSpotterBot.service.BotUserService;
import car.autoSpotterBot.service.StandortService;
import car.autoSpotterBot.util.transportUtils.AutoInterpreter;
import car.autoSpotterBot.util.transportUtils.BotCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RealEstateService {
    private static final Logger log = LoggerFactory.getLogger(AutoInterpreter.class);
    private static final int PAGE_SIZE = 10;
    private final BotUserService userService;
    private final Button button;
    private final StandortService standortService;
    private final ApartmentService apartmentService;
    private final ParkingSpaceService parkingSpaceService;
    private final HouseService houseService;
    private final BuildingLotService buildingLotService;
    private final BusinessPremiseService businessPremiseService;
    private final RentalHomeService rentalHomeService;
    private final AdService adService;
    private final BotCallback botCallback;
    private final Map<Long, Integer> photoIndexMap = new HashMap<>();
    public Map<Long, Integer> userPageState = new HashMap<>();

    public RealEstateService(BotUserService userService, Button button, StandortService standortService, ApartmentService apartmentService, ParkingSpaceService parkingSpaceService, HouseService houseService, BuildingLotService buildingLotService, BusinessPremiseService businessPremiseService, RentalHomeService rentalHomeService, AdService adService, BotCallback botCallback) {
        this.userService = userService;
        this.button = button;
        this.standortService = standortService;
        this.apartmentService = apartmentService;
        this.parkingSpaceService = parkingSpaceService;
        this.houseService = houseService;
        this.buildingLotService = buildingLotService;
        this.businessPremiseService = businessPremiseService;
        this.rentalHomeService = rentalHomeService;
        this.adService = adService;
        this.botCallback = botCallback;
    }
    public <T extends Ad> void searchAd(Long chatId, String searchText, Class<T> adClass) {
        List<T> ads;
        if ("Hammasini ko'rsatish".equals(searchText)) {
            ads = findAllAds(adClass);
        } else {
            ads = findAds(adClass, searchText);
        }
        if (ads.isEmpty()) {
            botCallback.sendMessageWithInlKeyboard(chatId, "Birorta ham e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
            return;
        }
        displayAdsAtSearch(chatId, ads);
    }

    private <T extends Ad> List<T> findAds(Class<T> adClass, String stadtName) {
        if (Apartment.class.equals(adClass)) {
            return castList(apartmentService.findByStandort(stadtName), adClass);
        } else if (ParkingSpace.class.equals(adClass)) {
            return castList(parkingSpaceService.findByStandort(stadtName), adClass);
        } else if (House.class.equals(adClass)) {
            return castList(houseService.findByStandort(stadtName), adClass);
        } else if (BuildingLot.class.equals(adClass)) {
            return castList(buildingLotService.findByStandort(stadtName), adClass);
        } else if (RentalHome.class.equals(adClass)) {
            return castList(rentalHomeService.findByStandort(stadtName), adClass);
        }else if (BusinessPremise.class.equals(adClass)) {
            return castList(businessPremiseService.findByStandort(stadtName), adClass);
        }
        return new ArrayList<>();
    }

    private <T extends Ad> List<T> findAllAds(Class<T> adClass) {
        if (Apartment.class.equals(adClass)) {
            return castList(apartmentService.findAll(), adClass);
        } else if (ParkingSpace.class.equals(adClass)) {
            return castList(parkingSpaceService.findAll(), adClass);
        } else if (House.class.equals(adClass)) {
            return castList(houseService.findAll(), adClass);
        } else if (BuildingLot.class.equals(adClass)) {
            return castList(buildingLotService.findAll(),adClass);
        } else if (BusinessPremise.class.equals(adClass)) {
            return castList(businessPremiseService.findAll(),adClass);
        }else if (RentalHome.class.equals(adClass)) {
            return castList(rentalHomeService.findAll(), adClass);
        }
        return new ArrayList<>();
    }

    private <T> List<T> castList(List<?> list, Class<T> clazz) {
        return list.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public void setStandort(String cityName, Ad ad) {
        Standort standort = standortService.saveCity(cityName);
        ad.setStandort(standort);
    }

    public void getMyAds(Long chatId) {
        List<Apartment> myApartments = apartmentService.findByUserId(chatId);
        displayMyAd(chatId, myApartments);

        List<ParkingSpace> myParkingSpace = parkingSpaceService.findByUserId(chatId);
        displayMyAd(chatId, myParkingSpace);

        List<House> myHouses = houseService.findByUserId(chatId);
        displayMyAd(chatId, myHouses);

        List<BuildingLot> myBuildingLots = buildingLotService.findByUserId(chatId);
        displayMyAd(chatId, myBuildingLots);

        List<BusinessPremise> myBusinessPremises = businessPremiseService.findByUserId(chatId);
        displayMyAd(chatId, myBusinessPremises);

        List<RentalHome> myRentalHome = rentalHomeService.findByUserId(chatId);
        displayMyAd(chatId, myRentalHome);
    }

    public void getMyFavorite(Long chatId) {
        List<Apartment> myApartments = apartmentService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, myApartments);

        List<ParkingSpace> myParkingSpace = parkingSpaceService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, myParkingSpace);

        List<House> myHouses = houseService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, myHouses);

        List<BuildingLot> myBuildingLots = buildingLotService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, myBuildingLots);

        List<BusinessPremise> myBusinessPremises = businessPremiseService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, myBusinessPremises);

        List<RentalHome> myRentalHome = rentalHomeService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, myRentalHome);
    }

    public void getNextPhoto(Long chatId, String text, Integer messageId, Class<? extends Ad> adClass) {
        String[] parts = text.split("_");
        Long adId = Long.parseLong(parts[1]);
        Ad ad = getAdById(adId, adClass);

        if (ad == null) {
            // Behandlung von fehlenden Anzeigen ...
            return;
        }

        String captionText = ad.getDescription();
        List<String> imageUrls = ad.getImageUrl();
        String videoUrl = ad.getVideoUrl();
        int currentIndex = photoIndexMap.getOrDefault(chatId, 0);
        int nextIndex = (currentIndex + 1) % imageUrls.size();
        int previousIndex = (currentIndex - 1) % imageUrls.size();
        if (text.startsWith(ButtonConstant.nextPhoto)) {
            if (imageUrls.size() >= 2) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, nextIndex);
                InlineKeyboardMarkup newKeyboard1 = button.inlKeyboardForMyAds(adId, nextIndex);
                botCallback.editImageMessage(chatId, messageId, captionText, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(ButtonConstant.previousPhoto)) {
            if (currentIndex != 0) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, previousIndex);
                botCallback.editImageMessage(chatId, messageId, captionText, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(ButtonConstant.video)) {
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, nextIndex);
            botCallback.editImageMessage(chatId, messageId, captionText, null, videoUrl, newKeyboard);
            photoIndexMap.put(chatId, nextIndex);
        } else if (text.startsWith(ButtonConstant.favorite)) {
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardAddFav(adId, nextIndex);
            botCallback.editImageMessage(chatId, messageId, captionText, null, null, newKeyboard);
        }
    }

    public void saveUrl(String text, String photoUrl, String videoUrl, Ad currentAd) {
        if (photoUrl != null) {
            List<String> photoUrls = currentAd.getImageUrl();
            if (photoUrl != null) {
                if (photoUrls == null) {
                    photoUrls = new ArrayList<>();
                    currentAd.setImageUrl(photoUrls);
                }
                photoUrls.add(photoUrl);
            }
            if (text != null) {
                currentAd.setDescription(text);
            }
        } else {
            currentAd.setVideoUrl(videoUrl);
            if (text != null) {
                currentAd.setDescription(text);
            }
        }
    }

    private <T extends Ad> T getAdById(Long adId, Class<T> adClass) {
        if (adClass.equals(Apartment.class)) {
            return (T) apartmentService.findById(adId);
        } else if (adClass.equals(ParkingSpace.class)) {
            return (T) parkingSpaceService.findById(adId);
        } else if (adClass.equals(House.class)) {
            return (T) houseService.findByUserId(adId);
        } else if (adClass.equals(BuildingLot.class)) {
            return (T) buildingLotService.findByUserId(adId);
        }else if (adClass.equals(BusinessPremise.class)) {
            return (T) businessPremiseService.findByUserId(adId);
        }else if (adClass.equals(RentalHome.class)) {
            return (T) rentalHomeService.findByUserId(adId);
        }
        return null;
    }

    private <T extends Ad> void displayMyFavorites(Long chatId, List<T> ads) {
        if (!ads.isEmpty()) {
            for (T ad : ads) {
                String photoUrl = ad.getImageUrl().isEmpty() ? null : ad.getImageUrl().get(0);
                String description = ad.getDescription();
                botCallback.sendPhotoWithInlKeyboard(chatId, description, photoUrl, button.inlKeyboardMyFavorite(ad.getId(), null));
            }
        }
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

    private <T extends Ad> void displayAdsAtSearch(Long chatId, List<T> ads) {
        int currentIndex = userPageState.getOrDefault(chatId, ads.size());
        int startIndex = Math.max(0, currentIndex - PAGE_SIZE);
        if (ads.size() > 10) {
            for (int i = startIndex; i < currentIndex; i++) {
                T ad = ads.get(i);
                getAd(chatId, ad);
            }
        } else {
            for (T ad : ads) {
                getAd(chatId, ad);
            }
        }
        if (startIndex == 0) {
            int endIndex = Math.max(0, currentIndex + PAGE_SIZE);
            userPageState.put(chatId, endIndex);
        } else {
            userPageState.put(chatId, startIndex);
        }

    }

    private <T extends Ad> void getAd(Long chatId, T ad) {
        List<String> photoUrls = ad.getImageUrl();
        String photoUrl = ad.getImageUrl().isEmpty() ? null : ad.getImageUrl().get(0);
        String description = ad.getDescription();
        if (photoUrls != null && !photoUrls.isEmpty()) {
            botCallback.sendPhotoWithInlKeyboard(chatId, description, photoUrl, button.inlKeyboardForAd(ad.getId(), null));
        }
    }

    public void finalizeAndSaveAd(Long chatId, Ad currentAd, BotCallback botCallback) {
        BotUser user = userService.findByTelegramId(chatId);
        currentAd.setUser(user);
        if (currentAd instanceof Apartment) {
            apartmentService.saveApartment((Apartment) currentAd);
        } else if (currentAd instanceof ParkingSpace) {
            parkingSpaceService.saveGarage((ParkingSpace) currentAd);
        } else if (currentAd instanceof House) {
            houseService.saveHouse((House) currentAd);
        }else if (currentAd instanceof BuildingLot) {
            buildingLotService.savePlot((BuildingLot) currentAd);
        }else if (currentAd instanceof BusinessPremise) {
            businessPremiseService.savePremise((BusinessPremise) currentAd);
        } else if (currentAd instanceof RentalHome) {
            rentalHomeService.saveRentHouse((RentalHome)currentAd);
        }

        botCallback.sendMessageWithInlKeyboard(chatId, "E'lon muvaffaqiyatli joylandi \uD83D\uDC4F", null);
    }

    public void addToFavorite(Long chatId, String callbackData, Class<? extends Ad> adClass) {
        BotUser user = userService.findByTelegramId(chatId);
        String[] parts = callbackData.split("_");
        Long adId = Long.valueOf(parts[1]);
        if (adClass.equals(Apartment.class)) {
            apartmentService.addFavorite(chatId, adId);
        } else if (adClass.equals(ParkingSpace.class)) {
            parkingSpaceService.addFavorite(chatId, adId);
        } else if (adClass.equals(House.class)) {
            houseService.addFavorite(chatId, adId);
        } else if (adClass.equals(BuildingLot.class)) {
            buildingLotService.addFavorite(chatId,adId);
        }else if (adClass.equals(BusinessPremise.class)) {
            businessPremiseService.addFavorite(chatId,adId);
        }else if (adClass.equals(RentalHome.class)) {
            rentalHomeService.addFavorite(chatId,adId);
        }
        userService.save(user);
    }

    public void deleteAd(Long chatId, String callbackData, int messageId) {
        String[] parts = callbackData.split("_");
        Long adId = Long.parseLong(parts[1]);
        boolean deleted = adService.deleteById(adId);

        if (deleted) {
            botCallback.deleteMessage(chatId, messageId);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.", null);
        }
    }

    public void displayNextPage(Long chatId, Class<? extends Ad> adClass) {
        List<? extends Ad> ads;

        if (adClass.equals(Apartment.class)) {
            ads = apartmentService.findAll();
        } else if (adClass.equals(ParkingSpace.class)) {
            ads = parkingSpaceService.findAll();
        } else if (adClass.equals(House.class)) {
            ads = houseService.findAll();
        } else if (adClass.equals(BuildingLot.class)) {
            ads = buildingLotService.findAll();
        } else if (adClass.equals(BusinessPremise.class)) {
            ads = businessPremiseService.findAll();
        }else if (adClass.equals(RentalHome.class)) {
            ads = rentalHomeService.findAll();
        } else {
            ads = Collections.emptyList();
        }
        int currentIndex = userPageState.getOrDefault(chatId, ads.size());
        if (currentIndex > 0) {
            displayAdsAtSearch(chatId, ads);
            botCallback.sendMessageWithReplyKeyboard(chatId, "Keyingi e'lonlarni ko'rish uchun \uD83D\uDC47", button.nextPage());
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Jo'q boshqa e'lon-pelon! \uD83D\uDE04", null);
        }
    }

    public void displayPreviousPage(Long chatId, Class<? extends Ad> adClass) {
        List<? extends Ad> ads;

        if (adClass.equals(Apartment.class)) {
            ads = apartmentService.findAll();
        } else if (adClass.equals(ParkingSpace.class)) {
            ads = parkingSpaceService.findAll();
        } else if (adClass.equals(House.class)) {
            ads = houseService.findAll();
        } else if (adClass.equals(BuildingLot.class)) {
            ads = buildingLotService.findAll();
        } else if (adClass.equals(BusinessPremise.class)) {
            ads = businessPremiseService.findAll();
        }else if (adClass.equals(RentalHome.class)) {
            ads = rentalHomeService.findAll();
        } else {
            ads = Collections.emptyList();
        }
        int currentIndex = userPageState.getOrDefault(chatId, ads.size());
        if (currentIndex > 0) {
            displayAdsAtSearch(chatId, ads);
            botCallback.sendMessageWithReplyKeyboard(chatId, "Keyingi e'lonlarni ko'rish uchun \uD83D\uDC47", button.nextPage());
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Jo'q boshqa e'lon-pelon! \uD83D\uDE04", null);
        }
    }

    public void deleteAdFromFavorite(long chatId, String callbackData, int messageId) {
        String[] parts = callbackData.split("_");
        Long adId = Long.parseLong(parts[1]);
        adService.removeFromFavorite(chatId, adId);
        botCallback.deleteMessage(chatId, messageId);
    }
}
