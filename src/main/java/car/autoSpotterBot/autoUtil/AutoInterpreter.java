package car.autoSpotterBot.autoUtil;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.TransButtonConstant;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.Automobile;
import car.autoSpotterBot.service.BotUserService;
import car.autoSpotterBot.service.StandortService;
import car.autoSpotterBot.service.transport.AutomobileService;
import car.autoSpotterBot.state.UserStateManager;
import car.autoSpotterBot.state.transState.UserStateAuto;
import car.autoSpotterBot.state.transState.UserStateTrans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static car.autoSpotterBot.state.transState.UserStateInAuto.*;

@Component
public class AutoInterpreter {
    private static final Logger log = LoggerFactory.getLogger(AutoInterpreter.class);
    private static final int PAGE_SIZE = 10;
    private final BotUserService userService;
    private final Button button;
    private final StandortService standortService;
    private final AutomobileService automobileService;
    private final BotCallback botCallback;
    private final Map<Long, Automobile> currentAdAuto = new ConcurrentHashMap<>();

    private final UserStateManager userState;
    private final UserStateTrans userStateTrans;
    private final UserStateAuto userStateAuto;
    private Map<Long, Integer> photoIndexMap = new HashMap<>();
    private Map<Long, Integer> userPageState = new HashMap<>();

    public AutoInterpreter(BotUserService userService, Button button, StandortService standortService, AutomobileService automobileService, BotCallback botCallback, UserStateManager userState, UserStateTrans userStateTrans, UserStateAuto userStateAuto) {
        this.userService = userService;
        this.button = button;
        this.standortService = standortService;
        this.automobileService = automobileService;
        this.botCallback = botCallback;
        this.userState = userState;
        this.userStateTrans = userStateTrans;
        this.userStateAuto = userStateAuto;
    }

    public void autointerpreter(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Integer messageId = update.getMessage().getMessageId();

            switch (text) {
                case TransButtonConstant.placeAutoAd -> {
                    botCallback.sendInlineKeyboardCites(chatId);
                    userStateAuto.setUserState(chatId, PLACE_AUTOMOBILE_AD);
                }
                case TransButtonConstant.autoSearch -> {
                    botCallback.sendInKeyboardForSearch(chatId);
                    userStateAuto.setUserState(chatId, SEARCH_AD_AUTO);
                }
                case TransButtonConstant.mayAutoAds -> getMyAds(chatId);
                case TransButtonConstant.autoFavorite -> getMyFavorite(chatId);
                case TransButtonConstant.backInAutoAd -> back(chatId);
                case TransButtonConstant.nextPage -> nextPage(chatId);
                case TransButtonConstant.previousPage -> previousPage(chatId);
            }
            if (userStateAuto.getUserState(chatId).equals(EDIT_MY_AD_TEXT)) {
                editMyAdText(chatId, text);
            }

            userStateTrans.setUserState(chatId, AUTO);
            userState.setUserState(chatId, AUTO);

        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {

            List<PhotoSize> photoSizes = update.getMessage().getPhoto();
            Long chatId = update.getMessage().getFrom().getId();
            PhotoSize largestPhoto = photoSizes.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
            String photoUrl = largestPhoto.getFileId();
            String caption = update.getMessage().getCaption();
            Integer messageId = update.getMessage().getMessageId();
            Automobile currentAutomobile = currentAdAuto.getOrDefault(chatId, new Automobile());
            if (photoUrl != null) {
                log.info("Caption: " + caption + " UserAutoState: " + userStateAuto.getUserState(chatId));
                saveUrl(chatId, caption, photoUrl, null, currentAutomobile);
                if (caption != null) {
                    botCallback.sendPhotoWithInlKeyboard(chatId, currentAutomobile.getDescription(), photoUrl, button.inlKeyboardConfirmation());
                }
            }
            userStateTrans.setUserState(chatId, AUTO);

            userState.setUserState(chatId, AUTO);

        } else if (update.hasMessage() && update.getMessage().hasVideo()) {

            Long chatId = update.getMessage().getFrom().getId();
            Video video = update.getMessage().getVideo();
            String text = update.getMessage().getCaption();
            String videoUrl = video.getFileId();
            Integer messageId = update.getMessage().getMessageId();
            Automobile currentAutomobile = currentAdAuto.getOrDefault(chatId, new Automobile());
            if (videoUrl != null) {
                log.info("VideoUrl for Ad: " + videoUrl);
                saveUrl(chatId, text, null, videoUrl, currentAutomobile);
            }
            userStateTrans.setUserState(chatId, AUTO);
            userState.setUserState(chatId, AUTO);

        } else if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getFrom().getId();
            String callBackText = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            Automobile currentAutomobile = currentAdAuto.getOrDefault(chatId, new Automobile());

            validateUserState(chatId, callBackText, messageId, currentAutomobile);
            userStateTrans.setUserState(chatId, AUTO);
            userState.setUserState(chatId, AUTO);
        }

    }

    private void validateUserState(Long chatId, String text, Integer messageId, Automobile currentAutomobile) {
        if (text.startsWith(TransButtonConstant.nextPhoto) || text.startsWith(TransButtonConstant.previousPhoto) ||
                text.startsWith(TransButtonConstant.video) || text.startsWith(TransButtonConstant.favorite)) {
            getNextPhoto(chatId, text, messageId);
        }
        if (text.startsWith(TransButtonConstant.favorite)) {
            addToFavorite(chatId, text);
        }
        if (text.startsWith(TransButtonConstant.deleteAd)) {
            deleteMyAutoAd(chatId, text);
        }
        if (text.startsWith(TransButtonConstant.editText)) {
            sendMessageForEditAd(chatId);
        }
        if (userStateAuto.getUserState(chatId).equals(SEARCH_AD_AUTO)) {
            autoSearch(chatId, text);
        }
        if (!userStateAuto.getUserState(chatId).equals(SEARCH_AD_AUTO)) {
            switch (text) {
                case "Toshkent", "Andijon", "Buxoro", "Farg'ona", "Jizzax", "Sirdaryo", "Namangan", "Samarqand",
                        "Xorazm", "Surxandaryo", "Qashqadaryo", "Qoraqalpog'iston", "Navoi" -> {
                    setStandort(chatId, messageId,text, currentAutomobile);
                    botCallback.deleteMessageLater(chatId,messageId,10);
                }
                case TransButtonConstant.confirm -> {
                    finalizeAndSaveAd(chatId, currentAutomobile);
                    botCallback.deleteMessageLater(chatId,messageId,10);
                }
                case TransButtonConstant.cancel -> {
                    cancelAutoAd(chatId);
                    botCallback.deleteMessageLater(chatId,messageId,10);
                }

            }
        }

    }

    private void autoSearch(Long chatId, String text) {
        if (text.equals("Hammasini ko'rsatish")) {

            List<Automobile> allAutomobiles = automobileService.findAll();
            if (allAutomobiles != null && !allAutomobiles.isEmpty()) {
                getAd(chatId, allAutomobiles);
                botCallback.sendMessageWithReplyKeyboard(chatId, "Navbatdagi e'lonlar", button.nextPage());

            } else {
                botCallback.sendMessageWithInlKeyboard(chatId, "Birorta ham e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
            }
        } else {
            List<Automobile> byStadt = automobileService.findByStandort(text);
            if (byStadt != null && !byStadt.isEmpty()) {
                getAd(chatId, byStadt);
                botCallback.sendMessageWithReplyKeyboard(chatId, "Navbatdagi e'lonlar", button.nextPage());

            } else {
                botCallback.sendMessageWithInlKeyboard(chatId, text + "da e'lon topilmadi \uD83D\uDE45\u200D♂\uFE0F", null);
            }
        }
        userStateAuto.setUserState(chatId, SEARCH_AD_AUTO);
    }

    private void setStandort(Long chatId, Integer messageId, String text, Automobile currentAutomobile) {
        botCallback.sendMessageWithInlKeyboard(chatId, MessageText.autoAdExample, null);
        Standort standort = standortService.saveCity(text);
        currentAutomobile.setStandort(standort);
        currentAdAuto.put(chatId, currentAutomobile);
        userStateAuto.setUserState(chatId, PLACE_AUTOMOBILE_AD);
    }

    private void saveUrl(Long chatId, String text, String photoUrl, String videoUrl, Automobile currentAutomobile) {
        log.info("Text in SaveUrl: " + text);
        List<String> photoUrls = currentAutomobile.getImageUrl();
        if (photoUrl != null) {
            if (photoUrls == null) {
                photoUrls = new ArrayList<>();
                currentAutomobile.setImageUrl(photoUrls);
                currentAutomobile.setDescription(text);
            }
            photoUrls.add(photoUrl);
        } else {
            currentAutomobile.setVideoUrl(videoUrl);
            log.info("VideoUrl in saveUrl " + videoUrl);
        }
        currentAdAuto.put(chatId, currentAutomobile);
        userStateAuto.setUserState(chatId, PLACE_AUTOMOBILE_AD);
    }

    private void finalizeAndSaveAd(Long chatId, Automobile currentAutomobile) {

        BotUser user = userService.findByTelegramId(chatId);
        currentAutomobile.setUser(user);
        currentAdAuto.put(chatId, currentAutomobile);
        automobileService.saveAutomobile(currentAutomobile);
        currentAdAuto.remove(chatId);
        userStateTrans.setUserState(chatId, PLACE_AUTOMOBILE_AD);
        botCallback.sendMessageWithInlKeyboard(chatId, "Avto E'loningiz joylandi", null);

    }

    private void getAd(Long chatId, List<Automobile> automobiles) {

        int currentIndex = userPageState.getOrDefault(chatId, automobiles.size());  // Starte mit der letzten Anzeige
        int startIndex = Math.max(0, currentIndex - PAGE_SIZE);
        for (int i = startIndex; i < currentIndex; i++) {
            Automobile automobile = automobiles.get(i);
            List<String> photoUrls = automobile.getImageUrl();
            if (photoUrls != null && !photoUrls.isEmpty()) {
                botCallback.sendPhotoWithInlKeyboard(chatId, "E'lon nomeri: " + automobile.getId() + "\n " +
                        automobile.getDescription(), photoUrls.get(0), button.inlKeyboardForAd(automobile.getId(), null));
            }
        }
        userPageState.put(chatId, startIndex);
    }

    private void deleteMyAutoAd(Long chatId, String text) {
        String[] parts = text.split("_");
        log.info("Text AdId: " + text);
        Long adId = Long.parseLong(parts[1]);
        Automobile automobile = automobileService.findById(adId);
        boolean deleted = automobileService.deleteById(automobile.getId());
        if (deleted) {
            botCallback.sendMessageWithInlKeyboard(chatId, "Ihre Anzeigen wurden gelöscht.", null);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.", null);
        }
    }

    private void editMyAdText(Long chatId, String text) {
        String[] parts = text.split("_");
        log.info("Text AdId: " + text);
        Long adId = Long.parseLong(parts[1]);
        List<Automobile> userAutomobiles = automobileService.findByUserId(chatId);
        if (userAutomobiles.isEmpty()) {
            botCallback.sendMessageWithInlKeyboard(chatId, "Sizda e'lon jo'q", null);
            return;
        }
        for (Automobile automobile : userAutomobiles) {
            String description = automobile.getDescription();
            Automobile automobileId = automobileService.findById(adId);
            botCallback.sendMessageWithInlKeyboard(chatId, "E'lon matnini qaytadan yuklang \uD83D\uDC47\uD83D\uDC47\uD83D\uDC47", null);
            botCallback.sendMessageWithInlKeyboard(chatId, description, null);
        }
    }

    private void sendMessageForEditAd(Long chatId) {
        List<Automobile> userAutomobiles = automobileService.findByUserId(chatId);
        if (userAutomobiles.isEmpty()) {
            botCallback.sendMessageWithInlKeyboard(chatId, "Sizda e'lon jo'q", null);
            return;
        }
        for (Automobile automobile : userAutomobiles) {
            String description = automobile.getDescription();
            botCallback.sendMessageWithInlKeyboard(chatId, "E'lon matnini qaytadan yuklang \uD83D\uDC47\uD83D\uDC47\uD83D\uDC47", null);
            botCallback.sendMessageWithInlKeyboard(chatId, description, null);
        }
        userStateAuto.setUserState(chatId, EDIT_MY_AD_TEXT);
    }

    private void addToFavorite(Long chatId, String text) {
        BotUser user = userService.findByTelegramId(chatId);
        String[] parts = text.split("_");
        Long adId = Long.valueOf(parts[1]);
        automobileService.addFavorite(user.getTelegramId(), adId);
        userService.save(user);
    }

    private void getNextPhoto(Long chatId, String text, Integer messageId) {
        String[] parts = text.split("_");
        Long adId = Long.parseLong(parts[1]);
        Automobile automobile = automobileService.findById(adId);
        String captionText = automobile.getDescription();
        List<String> imageUrls = automobile.getImageUrl();
        String videoUrl = automobile.getVideoUrl();
        int currentIndex = photoIndexMap.getOrDefault(chatId, 0);
        int nextIndex = (currentIndex + 1) % imageUrls.size();
        int previousIndex = (currentIndex - 1) % imageUrls.size();
        if (text.startsWith(TransButtonConstant.nextPhoto)) {
            if (imageUrls.size() >= 2) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, nextIndex);
                InlineKeyboardMarkup newKeyboard1 = button.inlKeyboardForMyAds(adId, nextIndex);
                botCallback.editImageMessage(chatId, messageId, captionText, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(TransButtonConstant.previousPhoto)) {
            if (currentIndex != 0) {
                String nextImageUrl = imageUrls.get(nextIndex);
                InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, previousIndex);
                botCallback.editImageMessage(chatId, messageId, captionText, nextImageUrl, null, newKeyboard);
                photoIndexMap.put(chatId, nextIndex);
            }
        } else if (text.startsWith(TransButtonConstant.video)) {
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardForAd(adId, nextIndex);
            botCallback.editImageMessage(chatId, messageId, captionText, null, videoUrl, newKeyboard);
            photoIndexMap.put(chatId, nextIndex);
        } else if (text.startsWith(TransButtonConstant.favorite)) {
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardAddFav(adId, nextIndex);
            botCallback.editImageMessage(chatId, messageId, captionText, null, null, newKeyboard);
        }
        userStateAuto.setUserState(chatId, PLACE_AUTOMOBILE_AD);

    }

    private void getMyAds(Long chatId) {
        List<Automobile> myAutomobiles = automobileService.findByUserId(chatId);
        if (!myAutomobiles.isEmpty()) {
            for (Automobile automobile : myAutomobiles) {
                List<String> photoUrls = automobile.getImageUrl();

                botCallback.sendPhotoWithInlKeyboard(chatId, automobile.getDescription(), photoUrls.get(0), button.inlKeyboardForMyAds(automobile.getId(),null));
            }
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Sizda e'lon-pelon jo'qku \uD83D\uDE04", null);
        }
        userStateAuto.setUserState(chatId, PLACE_AUTOMOBILE_AD);

    }

    private void cancelAutoAd(Long chatId) {
        currentAdAuto.remove(chatId);
        botCallback.sendMessageWithInlKeyboard(chatId, "E'lon bekor qilindi", null);
        //botCallback.sendMessageWithInlKeyboard(chatId, "E'lon tasdiqlab bo'lindi, " + " Mening e'lonlarim bo'limidan o'chirishingiz mumkin", null);
    }

    private void back(Long chatId) {
        botCallback.sendMessageWithReplyKeyboard(chatId, "Bo'limni tanlang", button.transportMenu());
        userState.setUserState(chatId, AUTO);
        photoIndexMap.clear();

    }

    private void initializeNewAd(Long chatId) {
        BotUser user = userService.findByTelegramId(chatId);
        Automobile newAutomobile = new Automobile();
        newAutomobile.setUser(user);
        currentAdAuto.put(chatId, newAutomobile);
        botCallback.sendInlineKeyboardCites(chatId);
        userStateAuto.setUserState(chatId, PLACE_AUTOMOBILE_AD);
    }

    public void nextPage(Long chatId) {
        List<Automobile> allAutomobiles = automobileService.findAll();
        int currentIndex = userPageState.getOrDefault(chatId, allAutomobiles.size());
        if (currentIndex > 0) {
            getAd(chatId, allAutomobiles);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Jo'q boshqa e'lon-pelon! \uD83D\uDE04", null);
        }
        userStateAuto.setUserState(chatId, PLACE_AUTOMOBILE_AD);

    }

    public void previousPage(Long chatId) {
        int currentIndex = userPageState.getOrDefault(chatId, 0) + PAGE_SIZE;
        userPageState.put(chatId, Math.min(automobileService.findAll().size(), currentIndex));
        List<Automobile> allAutomobiles = automobileService.findAll();
        getAd(chatId, allAutomobiles);
        userStateAuto.setUserState(chatId, PLACE_AUTOMOBILE_AD);

    }

    private void getMyFavorite(Long chatId) {
        List<Automobile> favoriteAutomobiles = automobileService.getFavoritesByUserId(chatId);
        if (favoriteAutomobiles.isEmpty()) {
            botCallback.sendMessageWithInlKeyboard(chatId, "Sie haben keine favorisierten Anzeigen", null);
            return;
        }
        for (Automobile automobile : favoriteAutomobiles) {
            List<String> imageUrl = automobile.getImageUrl();
            botCallback.sendPhotoWithInlKeyboard(chatId, "E'lon nomeri: " + automobile.getId() + "\n " +
                    automobile.getDescription(), imageUrl.get(0), null);
        }
        userStateAuto.setUserState(chatId, PLACE_AUTOMOBILE_AD);

    }

}
