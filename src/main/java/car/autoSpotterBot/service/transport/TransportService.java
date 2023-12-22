package car.autoSpotterBot.service.transport;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.*;
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
public class TransportService {
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
    private final BotCallback botCallback;
    private final Map<Long, Integer> photoIndexMap = new HashMap<>();
    public Map<Long, Integer> userPageState = new HashMap<>();

    public TransportService(BotUserService userService, Button button, StandortService standortService, AutomobileService automobileService, TruckService truckService, AgroTechnologyService agroTechService, AdService adService, OtherTransportService otherTransService, SparePartsService sparePartsService, BotCallback botCallback) {
        this.userService = userService;
        this.button = button;
        this.standortService = standortService;
        this.automobileService = automobileService;
        this.truckService = truckService;
        this.agroTechService = agroTechService;
        this.adService = adService;
        this.otherTransService = otherTransService;
        this.sparePartsService = sparePartsService;
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
        }
        return new ArrayList<>();
    }

    private <T extends Ad> List<T> findAllAds(Class<T> adClass) {
        if (Automobile.class.equals(adClass)) {
            return castList(automobileService.findAll(), adClass);
        } else if (Truck.class.equals(adClass)) {
            return castList(truckService.findAll(), adClass);
        } else if (AgroTechnology.class.equals(adClass)) {
            return castList(agroTechService.findAll(), adClass);
        } else if (SpareParts.class.equals(adClass)) {
            return castList(sparePartsService.findAll(), adClass);
        } else if (OtherTransport.class.equals(adClass)) {
            return castList(otherTransService.findAll(), adClass);
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

    public void getMyFavorite(Long chatId) {
        List<Automobile> favoriteAutomobiles = automobileService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, favoriteAutomobiles);

        List<Truck> favoriteTrucks = truckService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, favoriteTrucks);

        List<AgroTechnology> favoriteAgroTechs = agroTechService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, favoriteAgroTechs);

        List<SpareParts> spareParts = sparePartsService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, spareParts);

        List<OtherTransport> otherTransports = otherTransService.getFavoritesByUserId(chatId);
        displayMyFavorites(chatId, otherTransports);
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

    private <T extends Ad> void displayMyFavorites(Long chatId, List<T> ads) {
        if (!ads.isEmpty()) {
            for (T ad : ads) {
                String photoUrl = ad.getImageUrl().isEmpty() ? null : ad.getImageUrl().get(0);
                String description = ad.getDescription();
                botCallback.sendPhotoWithInlKeyboard(chatId, description, photoUrl, button.inlKeyboardMyFavorite(ad.getId(), null));
            }
        }
    }

    private <T extends Ad> void displayAdsAtSearch(Long chatId, List<T> ads) {
        // Bestimme den aktuellen Index basierend auf der aktuellen Seite, die der Benutzer sieht.
        int currentPage = userPageState.getOrDefault(chatId, 1);
        int startIndex = (currentPage - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, ads.size());

        for (int i = startIndex; i < endIndex; i++) {
            T ad = ads.get(i);
            getAd(chatId, ad);
        }

        int totalAds = ads.size();
        int totalPages = (totalAds + PAGE_SIZE - 1) / PAGE_SIZE;

        InlineKeyboardMarkup inlineKeyboard = button.createInlineKeyboardForPages(currentPage, totalPages);

        if (!ads.isEmpty()) {
            botCallback.sendMessageWithInlKeyboard(chatId, "Keyingi e'lonlarni ko'rish uchun \uD83D\uDC47", inlineKeyboard);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Birorta ham e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
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
        }

        botCallback.sendMessageWithInlKeyboard(chatId, "E'lon muvaffaqiyatli joylandi \uD83D\uDC4F", null);
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
        List<? extends Ad> ads = getAdsByClass(adClass);
        int totalAds = ads.size();
        int totalPages = (int) Math.ceil((double) totalAds / PAGE_SIZE);
        int currentPage = userPageState.getOrDefault(chatId, 1);
        // Berechnen Sie die zu zeigenden Anzeigen basierend auf der aktuellen Seite
        int startIndex = (currentPage - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalAds);
        List<? extends Ad> adsToShow = ads.subList(startIndex, endIndex);

        log.info("totalPages: " + totalPages + " currentPage: " + currentPage + " startIndex: " + startIndex + " endInex: " + endIndex);
        displayAdsAtSearch(chatId, adsToShow);

        // InlineKeyboardMarkup inlineKeyboard = button.createInlineKeyboardForPages(currentPage, totalPages);
        //  botCallback.sendMessageWithInlKeyboard(chatId, "Wählen Sie eine Seite:", inlineKeyboard);
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


    public void displayPreviousPage(Long chatId, Class<? extends Ad> adClass) {
        List<? extends Ad> ads;

        if (adClass.equals(Automobile.class)) {
            ads = automobileService.findAll();
        } else if (adClass.equals(Truck.class)) {
            ads = truckService.findAll(); // TruckService Beispiel
        } else if (adClass.equals(AgroTechnology.class)) {
            ads = agroTechService.findAll(); // AgroTechService Beispiel
        } else if (adClass.equals(OtherTransport.class)) {
            ads = otherTransService.findAll();
        } else if (adClass.equals(SpareParts.class)) {
            ads = sparePartsService.findAll();
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
