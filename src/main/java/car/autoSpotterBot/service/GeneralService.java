package car.autoSpotterBot.service;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.realeState.*;
import car.autoSpotterBot.model.transport.*;
import car.autoSpotterBot.service.realEstate.*;
import car.autoSpotterBot.service.transport.*;
import car.autoSpotterBot.util.MessageId;
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
    private final AdService adService;
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
    private final Map<Long, Integer> photoIndexMap = new HashMap<>();
    public Map<Long, Integer> userPageState = new HashMap<>();

    public GeneralService(BotUserService userService, Button button, StandortService standortService, AutomobileService automobileService, TruckService truckService, AgroTechnologyService agroTechService, AdService adService, OtherTransportService otherTransService, SparePartsService sparePartsService, ApartmentService apartmentService, BuildingLotService buildingLotService, BusinessPremiseService businessPremiseService, HouseService houseService, ParkingSpaceService parkingSpaceService, RentalHomeService rentalHomeService, BotCallback botCallback, MessageId messageIds) {
        this.userService = userService;
        this.button = button;
        this.standortService = standortService;
        this.automobileService = automobileService;
        this.truckService = truckService;
        this.agroTechService = agroTechService;
        this.adService = adService;
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
    }

    public <T extends Ad> void searchAd(Long chatId, String searchText, Class<T> adClass) {
        List<T> ads = findAds(adClass, searchText);

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
        String[] parts = text.split("_");
        Long adId = Long.parseLong(parts[1]);
        Ad ad = getAdById(adId, adClass);

        assert ad != null;
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
            return (T) agroTechService.findByUserId(adId);
        } else if (adClass.equals(OtherTransport.class)) {
            return (T) otherTransService.findByUserId(adId);
        } else if (adClass.equals(SpareParts.class)) {
            return (T) sparePartsService.findByUserId(adId);
        }
        return null;
    }

    private <T extends Ad> void displayAdsAtSearch(Long chatId, List<T> ads) {
        deleteMessageLater(chatId, messageIds.getIdOfButton());
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
            messageIds.setIdOfButton(message.getMessageId());
        } else {
            Message message = botCallback.sendMessageWithInlKeyboard(chatId, "Birorta ham e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
            botCallback.deleteMessageLater(chatId, message.getMessageId(), 3);
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
                messageIds.setIdOfAdsShown(id);
            }
        } else if (ad.getVideoUrl() != null) {
            String videoUrl = ad.getVideoUrl();
            String description = ad.getDescription();
            int id = botCallback.sendVideoWithInlKeyboard(chatId, "E'lon id raqami: " + ad.getId() + "\n" + description, videoUrl, button.inlKeyboardForAd(ad.getId(), null));
            messageIds.setIdOfAdsShown(id);
        } else {
            adService.deleteById(ad.getId());
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
        }else  if (currentAd instanceof Apartment) {
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
        }else if (adClass.equals(Apartment.class)) {
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
            Message message = botCallback.sendMessageWithInlKeyboard(chatId, "Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.", null);
            botCallback.deleteMessageLater(chatId, message.getMessageId(), 5);
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
        }
        return Collections.emptyList();
    }

    public void deleteAdFromFavorite(long chatId, String callbackData, int messageId) {
        String[] parts = callbackData.split("_");
        Long adId = Long.parseLong(parts[1]);
        adService.removeFromFavorite(chatId, adId);
        botCallback.deleteMessage(chatId, messageId);
    }

    private void deleteMessageLater(long chatId, Integer id) {
        if (id != 0) {
            botCallback.deleteMessageLater(chatId, id, 2);
        }
    }
}
