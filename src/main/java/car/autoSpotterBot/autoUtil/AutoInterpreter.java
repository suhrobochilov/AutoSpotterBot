package car.autoSpotterBot.autoUtil;

import car.autoSpotterBot.MyBot;
import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Stadt;
import car.autoSpotterBot.service.AdService;
import car.autoSpotterBot.service.BotUserService;
import car.autoSpotterBot.service.StadtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static car.autoSpotterBot.autoUtil.UserStateInAuto.*;

@Component
public class AutoInterpreter {
    private static final Logger log = LoggerFactory.getLogger(MyBot.class);
    private final BotUserService userService;
    private final Button button;
    private final StadtService stadtService;
    private final AdService adService;
    private final BotCallback botCallback;
    private final Map<Long, Ad> currentAdsMap = new ConcurrentHashMap<>();
    private final UserStateManager userStateManager;
    private Map<Long, Integer> photoIndexMap = new HashMap<>();

    public AutoInterpreter(BotCallback botCallback, BotUserService userService, Button buttonService, StadtService stadtService, AdService adService, UserStateManager userStateManager) {
        this.userService = userService;
        this.button = buttonService;
        this.stadtService = stadtService;
        this.adService = adService;
        this.botCallback = botCallback;
        this.userStateManager = userStateManager;
    }

    public void autoInterpret(Long chatId, String text, String photoUrl, String videoUrl, Integer messageId) {
        Ad currentAd = currentAdsMap.getOrDefault(chatId, new Ad());

        if (text != null) {
            log.info("Text: " + text);
            switch (text) {
                case "Toshkent", "Andijon", "Buxoro", "Farg'ona", "Jizzax", "Sirdaryo", "Namangan", "Samarqand", "Xorazm", "Surxandaryo", "Qashqadaryo", "Qoraqalpog'iston", "Navoi", "Hammasini ko'rsatish" ->
                        validateUserState(chatId, text, currentAd);
                case "Tasdiqlash" -> finalizeAndSaveAd(chatId, currentAd);
                case "Bekor qilish" -> cancelAutoAd(chatId);
                case ButtonConstants.nextPage -> nextPage(chatId);
                case ButtonConstants.previousPage -> previousPage(chatId);
                case ButtonConstants.auto -> auto(chatId);
                case ButtonConstants.placeAutoAd -> initializeNewAd(chatId);
                case ButtonConstants.autoSearch -> autoSearch(chatId);
                case ButtonConstants.mayAutoAds -> getMyAds(chatId);
                case ButtonConstants.backInAutoAd -> back(chatId);
            }
            if (text.startsWith(ButtonConstants.nextPhoto) || text.startsWith(ButtonConstants.previousPhoto) || text.startsWith(ButtonConstants.video) || text.startsWith(ButtonConstants.favorite)) {
                getNextPhoto(chatId, text, messageId);
            }
            if (text.startsWith("favorite")) {
                addToFavorite(chatId, text);
            }
        }

        if (photoUrl != null) {
            saveUrl(text, photoUrl, null, currentAd);
            userStateManager.setUserState(chatId, WAITING_FOR_CONFIRMATION);
            if (text != null) {
                botCallback.sendPhotoWithInlKeyboard(chatId, currentAd.getDescription(), photoUrl, button.inlKeyboardConfirmation());
            }
        }
        if (videoUrl != null) {
            log.info("Videourl for Ad: " + videoUrl);
            saveUrl(text, null, videoUrl, currentAd);
            userStateManager.setUserState(chatId, WAITING_FOR_CONFIRMATION);
        }
    }

    private void addToFavorite(Long chatId, String text) {
        BotUser user = userService.findByTelegramId(chatId);
        String[] parts = text.split("_");
        Long adId = Long.valueOf(parts[1]);
        adService.addFavorite(user.getId(), adId);
        userService.save(user);
    }

    private void getNextPhoto(Long chatId, String text, Integer messageId) {

        String[] parts = text.split("_");
        Long adId = Long.parseLong(parts[1]);
        Ad ad = adService.findById(adId);
        String captionText = ad.getDescription();
        List<String> imageUrls = ad.getImageUrl();
        String videoUrl = ad.getVideoUrl();
        int currentIndex = photoIndexMap.getOrDefault(chatId, 0);
        int nextIndex = (currentIndex + 1) % imageUrls.size();
        int previousIndex = (currentIndex - 1) % imageUrls.size();
        if (text.startsWith(ButtonConstants.nextPhoto)) {
            if (imageUrls.size() >= 2) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, nextIndex);
                botCallback.editImageMessage(chatId, messageId, captionText, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(ButtonConstants.previousPhoto)) {
            if (currentIndex != 0) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, previousIndex);
                botCallback.editImageMessage(chatId, messageId, captionText, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(ButtonConstants.video)) {
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, nextIndex);
            botCallback.editImageMessage(chatId, messageId, captionText, null, videoUrl, newKeyboard);
            photoIndexMap.put(chatId, nextIndex);
        } else if (text.startsWith(ButtonConstants.favorite)) {
            log.info("text: " + text + "  " + ButtonConstants.favorite);
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardAddFav(adId, nextIndex);
            botCallback.editImageMessage(chatId, messageId, captionText, null, null, newKeyboard);

        }
    }

    private void getMyAds(Long chatId) {
        List<Ad> myAds = adService.findByUserId(chatId);
        for (Ad ad : myAds) {
            List<String> photoUrls = ad.getImageUrl();
            if (photoUrls != null && !photoUrls.isEmpty()) {
                botCallback.sendPhotoWithInlKeyboard(chatId, ad.getDescription(), photoUrls.get(0), button.inlKeyboardForMyAds());
            } else {
                botCallback.sendMessageWithInlKeyboard(chatId, "Sizda e'lon yo'q", null);
                return;
            }
        }
    }

    private void cancelAutoAd(Long chatId) {
        if (userStateManager.getUserState(chatId).equals(WAITING_FOR_CONFIRMATION)) {
            currentAdsMap.remove(chatId);
            userStateManager.setUserState(chatId, AUTO_AD_CANCELLED);
            botCallback.sendMessageWithInlKeyboard(chatId, "E'lon bekor qilindi", null);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "E'lon tasdiqlab bo'lindi, " + "Mening e'lonlarim bo'limidan o'chirishingiz mumkin", null);
        }

    }

    private void saveUrl(String text, String photoUrl, String videoUrl, Ad currentAd) {
        List<String> photoUrls = currentAd.getImageUrl();
        if (photoUrl != null) {
            if (photoUrls == null) {
                photoUrls = new ArrayList<>();
                currentAd.setImageUrl(photoUrls);
                currentAd.setDescription(text);
            }
            photoUrls.add(photoUrl);
        } else {
            currentAd.setVideoUrl(videoUrl);
            log.info("VideoUrl in saveUrl " + videoUrl);
        }

    }

    private void finalizeAndSaveAd(Long chatId, Ad currentAd) {
        if (userStateManager.getUserState(chatId).equals(WAITING_FOR_CONFIRMATION)) {
            BotUser user = userService.findByTelegramId(chatId);
            currentAd.setUser(user);
            currentAdsMap.put(chatId, currentAd);
            adService.save(currentAd);
            currentAdsMap.remove(chatId);
            userStateManager.setUserState(chatId, AUTO_AD_PLACED);
            botCallback.sendMessageWithInlKeyboard(chatId, "Avto E'loningiz joylandi", null);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Siz e'lonni bekor qilgansiz, qaytadan yuklang", null);
        }

    }

    private void initializeNewAd(Long chatId) {
        BotUser user = userService.findByTelegramId(chatId);
        Ad newAd = new Ad();
        newAd.setUser(user);
        currentAdsMap.put(chatId, newAd);
        botCallback.sendInlineKeyboardCites(chatId);
        userStateManager.setUserState(chatId, PLACE_AD_AUTO);
    }

    private void validateUserState(Long chatId, String text, Ad currentAd) {
        if (userStateManager.getUserState(chatId).equals(PLACE_AD_AUTO)) {
            botCallback.sendMessageWithInlKeyboard(chatId, MessageText.autoAdExample, null);
            Stadt stadt = stadtService.saveCity(text);
            currentAd.setStandort(stadt);
            userStateManager.setUserState(chatId, SENDING_PHOTO_FOR_AUTO);

        } else if (userStateManager.getUserState(chatId).equals(SEARCH_AD_AUTO)) {

            if (text.equals("Hammasini ko'rsatish")) {

                List<Ad> allAds = adService.findAll();
                if (allAds != null && !allAds.isEmpty()) {
                    getAd(chatId, allAds);
                    botCallback.sendMessageWithReplyKeyboard(chatId, "Navbatdagi e'lonlar", button.nextPage());

                } else {
                    botCallback.sendMessageWithInlKeyboard(chatId, "Birorta ham e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
                }
            } else {
                List<Ad> byStadt = adService.findByStadt(text);
                if (byStadt != null && !byStadt.isEmpty()) {
                    getAd(chatId, byStadt);
                    botCallback.sendMessageWithReplyKeyboard(chatId, "Navbatdagi e'lonlar", button.nextPage());

                } else {
                    botCallback.sendMessageWithInlKeyboard(chatId, text + "da e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
                }
            }
            userStateManager.setUserState(chatId, SEARCH_AD_AUTO);

        }
        photoIndexMap.clear();

    }

    Map<Long, Integer> userPageState = new HashMap<>();

    private static final int PAGE_SIZE = 10;
    private void getAd(Long chatId, List<Ad> ads) {
        int currentIndex = userPageState.getOrDefault(chatId, ads.size());  // Starte mit der letzten Anzeige
        int startIndex = Math.max(0, currentIndex - PAGE_SIZE);

        for (int i = startIndex; i < currentIndex; i++) {
            Ad ad = ads.get(i);
            List<String> photoUrls = ad.getImageUrl();
            if (photoUrls != null && !photoUrls.isEmpty()) {
                botCallback.sendPhotoWithInlKeyboard(chatId, "E'lon nomeri: " + ad.getId() + "\n " +
                        ad.getDescription(), photoUrls.get(0), button.inlKeyboardForAd(ad.getId(), null));
            }
        }
        userPageState.put(chatId, startIndex);
    }

    public void nextPage(Long chatId) {
        List<Ad> allAds = adService.findAll();
        int currentIndex = userPageState.getOrDefault(chatId, allAds.size());
        if(currentIndex > 0) {
            getAd(chatId, allAds);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId,"Jo'q boshqa e'lon-pelon! \uD83D\uDE04",null);
        }
    }

    public void previousPage(Long chatId) {
        int currentIndex = userPageState.getOrDefault(chatId, 0) + PAGE_SIZE;
        userPageState.put(chatId, Math.min(adService.findAll().size(), currentIndex));
        List<Ad> allAds = adService.findAll();
        getAd(chatId, allAds);
    }



    private void autoSearch(Long chatId) {
        botCallback.sendInKeyboardForSearch(chatId);
        userStateManager.setUserState(chatId, SEARCH_AD_AUTO);
        photoIndexMap.clear();

    }

    private void auto(Long chatId) {
        botCallback.sendAutoMenu(chatId);
        userStateManager.setUserState(chatId, AUTO);
        photoIndexMap.clear();
    }

    private void back(Long chatId) {
        botCallback.menu(chatId);
        userStateManager.setUserState(chatId, START);
        photoIndexMap.clear();

    }

}
