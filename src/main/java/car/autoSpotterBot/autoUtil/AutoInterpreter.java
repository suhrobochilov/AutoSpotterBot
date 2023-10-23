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
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public AutoInterpreter(BotCallback botCallback, BotUserService userService, Button buttonService, StadtService stadtService, AdService adService, UserStateManager userStateManager) {
        this.userService = userService;
        this.button = buttonService;
        this.stadtService = stadtService;
        this.adService = adService;
        this.botCallback = botCallback;
        this.userStateManager = userStateManager;
    }

    public void autoInterpret(Long chatId, String text, String photoUrl, Integer messageId) {
        Ad currentAd = currentAdsMap.getOrDefault(chatId, new Ad());


        if (text != null) {
            switch (text) {
                case "Toshkent", "Andijon", "Buxoro", "Farg'ona",
                        "Jizzax", "Sirdaryo", "Namangan", "Samarqand",
                        "Xorazm", "Surxandaryo", "Qashqadaryo",
                        "Qoraqalpog'iston", "Navoi", "Hammasini ko'rsatish" ->
                        validateUserState(chatId, text, currentAd);
                case "Tasdiqlash" -> finalizeAndSaveAd(chatId, currentAd);
                case "Bekor qilish" -> cancelAutoAd(chatId);
                case "morePhotos_" -> getAllPhotos(chatId, text);
                case ButtonConstants.auto -> auto(chatId);
                case ButtonConstants.placeAutoAd -> initializeNewAd(chatId);
                case ButtonConstants.autoSearch -> autoSearch(chatId);
                case ButtonConstants.mayAutoAds -> getMyAds(chatId);
                case ButtonConstants.backInAutoAd -> back(chatId);
            }
        }

        if (text.startsWith("nextPhoto")) {
           handleCallbackQuery(chatId,text,messageId);
        }

        if (photoUrl != null) {
            savePhotoUrl(text, photoUrl, currentAd);
            userStateManager.setUserState(chatId, WAITING_FOR_CONFIRMATION);
            if (text != null) {
                botCallback.sendPhotoWithInlKeyboard(chatId, currentAd.getDescription(), photoUrl, button.inlKeyboardConfirmation());

            }
        }
    }

    private void handleCallbackQuery(Long chatId, String callbackQuery, Integer messageId) {
        if(callbackQuery.startsWith("nextPhoto")){
            log.info(callbackQuery);
            String [] parts = callbackQuery.split("_");
            Long adId = Long.parseLong(parts[1]);
            //int currentIndex = Integer.parseInt(parts[2]);
            Ad ad = adService.findById(adId);
            if(ad != null){
                List<String> imageUrls = ad.getImageUrl();
                int nextIndex = (messageId + 1) % imageUrls.size(); // Geht zum nächsten Bild, kehrt zum ersten zurück, wenn am Ende
                String nextImageUrl = imageUrls.get(nextIndex);
                // Erstellen Sie neue Inline-Buttons für das nächste Bild
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, nextIndex);
                botCallback.editImageMessage(chatId, messageId, nextImageUrl,newKeyboard);

            }
        }
    }



    private void getAllPhotos(Long chatId, String text) {
        log.info("You are in getAllPhotos");
        // Den AdID aus dem callbackData extrahieren
        String[] dataParts = text.split("_");
        if (!dataParts[0].equals("nextPhoto")) {
            // Ungültige Daten empfangen
            log.info("You are in dataParts.length != 2");
            return;
        }
        Long adId = Long.parseLong(dataParts[1]);

        // Die Anzeige basierend auf AdID holen
        Ad ad = adService.findById(adId);
        if (ad == null) {
            // Keine Anzeige gefunden
            log.info("You are in ad");

            return;
        }
        // Alle Fotos der Anzeige an den Benutzer senden
        for (String photoUrl : ad.getImageUrl()) {
            log.info("ImgaeSize: " + ad.getImageUrl().size() + " " + ad.getId());

            botCallback.sendPhotoWithInlKeyboard(chatId, "Hamma rasmlar", photoUrl, null);
        }

        // Optional: Inline-Tasten erneut senden, um dem Benutzer zu erlauben, durch mehr Fotos zu navigieren oder sie zu favorisieren
        //botCallback.sendMessageWithInlKeyboard(chatId, "Weitere Aktionen?", button.inlKeyboardForAd());
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
            botCallback.sendMessageWithInlKeyboard(chatId, "E'lon tasdiqlab bo'lindi, " +
                    "Mening e'lonlarim bo'limidan o'chirishingiz mumkin", null);
        }

    }

    private void savePhotoUrl(String text, String photoUrl, Ad currentAd) {
        List<String> photoUrls = currentAd.getImageUrl();
        if (photoUrls == null) {
            photoUrls = new ArrayList<>();
            currentAd.setImageUrl(photoUrls);
            currentAd.setDescription(text);

        }
        photoUrls.add(photoUrl);
        log.info("PhotoUrl: " + photoUrl);
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
                } else {
                    botCallback.sendMessageWithInlKeyboard(chatId, "Birorta ham e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
                }
            } else {
                List<Ad> byStadt = adService.findByStadt(text);
                if (byStadt != null && !byStadt.isEmpty()) {
                    getAd(chatId, byStadt);
                } else {
                    botCallback.sendMessageWithInlKeyboard(chatId, text + "da e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
                }
            }
            userStateManager.setUserState(chatId, GETTING_ADS);

        }
    }

    private void getAd(Long chatId, List<Ad> byStadt) {
        for (Ad ad : byStadt) {
            List<String> photoUrls = ad.getImageUrl();
            if (photoUrls != null && !photoUrls.isEmpty()) {
                botCallback.sendPhotoWithInlKeyboard(chatId, ad.getDescription(), photoUrls.get(0), button.inlKeyboardForAd(ad.getId(),null));
            }
        }
    }

    private void autoSearch(Long chatId) {
        botCallback.sendInKeyboardForSearch(chatId);
        userStateManager.setUserState(chatId, SEARCH_AD_AUTO);
    }

    private void auto(Long chatId) {
        botCallback.sendAutoMenu(chatId);
        userStateManager.setUserState(chatId, AUTO);
    }

    private void back(Long chatId) {
        botCallback.menu(chatId);
        userStateManager.setUserState(chatId, START);
    }

}
