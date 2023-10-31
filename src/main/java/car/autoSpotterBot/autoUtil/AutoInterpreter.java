package car.autoSpotterBot.autoUtil;

import car.autoSpotterBot.MyBot;
import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.TransButtonConstant;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.model.transport.*;
import car.autoSpotterBot.service.BotUserService;
import car.autoSpotterBot.service.StandortService;
import car.autoSpotterBot.service.transport.AutomobileService;
import car.autoSpotterBot.state.UserStateManager;
import car.autoSpotterBot.state.UserStateTrans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static car.autoSpotterBot.state.UserStateInAuto.*;

@Component
public class AutoInterpreter {
    private static final Logger log = LoggerFactory.getLogger(MyBot.class);
    private static final int PAGE_SIZE = 10;
    private final BotUserService userService;
    private final Button button;
    private final StandortService standortService;
    private final AutomobileService automobileService;
    private final BotCallback botCallback;
    private final Map<Long, Automobile> currentAdAuto = new ConcurrentHashMap<>();
    private final Map<Long, Truck> currentAdTruck = new ConcurrentHashMap<>();
    private final Map<Long, OtherTransport> currentAdOtherTrans = new ConcurrentHashMap<>();
    private final Map<Long, AgroTechnology> currentAdAgroTech = new ConcurrentHashMap<>();
    private final Map<Long, SpareParts> currentAdSpareParts = new ConcurrentHashMap<>();

    private final UserStateManager userState;
    private final UserStateTrans userStateTrans;
    private final Map<Long, Integer> photoIndexMap = new HashMap<>();
    Map<Long, Integer> userPageState = new HashMap<>();

    public AutoInterpreter(BotCallback botCallback, BotUserService userService, Button buttonService, StandortService standortService, AutomobileService automobileService, UserStateManager userStateManager, UserStateTrans userStateTrans) {
        this.userService = userService;
        this.button = buttonService;
        this.standortService = standortService;
        this.automobileService = automobileService;
        this.botCallback = botCallback;
        this.userState = userStateManager;
        this.userStateTrans = userStateTrans;
    }

    public void autoInterpret(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Integer messageId = update.getMessage().getMessageId();

            if (text != null) {
                switch (text) {

                    case TransButtonConstant.transport -> auto(chatId);
                    case TransButtonConstant.automobile -> {
                        botCallback.sendMessageWithInlKeyboard(chatId, "Bo'limni tanlang \uD83D\uDC47", button.inlKeyboardSection());
                        userStateTrans.setUserState(chatId, AUTO_SECTION);
                    }
                    case TransButtonConstant.agroTech -> {
                        botCallback.sendMessageWithInlKeyboard(chatId, "Bo'limni tanlang \uD83D\uDC47", button.inlKeyboardSection());
                        userStateTrans.setUserState(chatId, AGRO_TECH_SECTION);
                    }
                    case TransButtonConstant.truck -> {
                        botCallback.sendMessageWithInlKeyboard(chatId, "Bo'limni tanlang \uD83D\uDC47", button.inlKeyboardSection());
                        userStateTrans.setUserState(chatId, TRUCK_SECTION);
                    }
                    case TransButtonConstant.otherTrans -> {
                        botCallback.sendMessageWithInlKeyboard(chatId, "Bo'limni tanlang \uD83D\uDC47", button.inlKeyboardSection());
                        userStateTrans.setUserState(chatId, OTHER_TRANS_SECTION);
                    }
                    case TransButtonConstant.spareParts -> {
                        botCallback.sendMessageWithInlKeyboard(chatId, "Bo'limni tanlang \uD83D\uDC47", button.inlKeyboardSection());
                        userStateTrans.setUserState(chatId, SPARE_PARTS_SECTION);
                    }

                    case TransButtonConstant.mainMenu -> botCallback.menu(chatId);
                    case TransButtonConstant.deleteAd -> deleteMyAutoAd(chatId, null);
                    case TransButtonConstant.autoFavorite -> getMyFavorite(chatId);
                    case TransButtonConstant.nextPage -> nextPage(chatId);
                    case TransButtonConstant.previousPage -> previousPage(chatId);
                    case TransButtonConstant.placeAutoAd -> initializeNewAd(chatId);
                    case TransButtonConstant.autoSearch -> autoSearch(chatId);
                    case TransButtonConstant.mayAutoAds -> getMyAds(chatId);
                    case TransButtonConstant.backInAutoAd -> back(chatId);
                }
            }
            userState.setUserState(chatId, AUTO);

        } else if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getFrom().getId();
            String callBackText = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            Automobile currentAutomobile = currentAdAuto.getOrDefault(chatId, new Automobile());
            Truck currentTruck = currentAdTruck.getOrDefault(chatId, new Truck());
            AgroTechnology currentAgroTech = currentAdAgroTech.getOrDefault(chatId, new AgroTechnology());
            OtherTransport currentOtherTrans = currentAdOtherTrans.getOrDefault(chatId, new OtherTransport());
            SpareParts currentSpareParts = currentAdSpareParts.getOrDefault(chatId, new SpareParts());
            validateUserState(chatId, callBackText, currentAutomobile, currentAgroTech, currentTruck, currentOtherTrans, currentSpareParts);

            /*switch (callBackText) {
                case "Tasdiqlash" -> finalizeAndSaveAd(chatId, currentAutomobile);
                case "Bekor qilish" -> cancelAutoAd(chatId);
            }
            if (callBackText.startsWith(TransButtonConstant.nextPhoto) || callBackText.startsWith(TransButtonConstant.previousPhoto) ||
                    callBackText.startsWith(TransButtonConstant.video) || callBackText.startsWith(TransButtonConstant.favorite)) {
                getNextPhoto(chatId, callBackText, messageId);
            }
            if (callBackText.startsWith("favorite")) {
                addToFavorite(chatId, callBackText);
            }*/
            userState.setUserState(chatId, AUTO);

        } else if (update.getMessage().hasPhoto()) {
            List<PhotoSize> photoSizes = update.getMessage().getPhoto();
            Long chatId = update.getMessage().getFrom().getId();
            PhotoSize largestPhoto = photoSizes.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
            String photoUrl = largestPhoto.getFileId();
            String caption = update.getMessage().getCaption();
            Integer messageId = update.getMessage().getMessageId();
            Automobile currentAutomobile = currentAdAuto.getOrDefault(chatId, new Automobile());
            if (photoUrl != null) {
                saveUrl(caption, photoUrl, null, currentAutomobile);
                userStateTrans.setUserState(chatId, WAITING_FOR_CONFIRMATION);
                if (caption != null) {
                    botCallback.sendPhotoWithInlKeyboard(chatId, currentAutomobile.getDescription(), photoUrl, button.inlKeyboardConfirmation());
                }
            }
            userState.setUserState(chatId, AUTO);

        } else if (update.getMessage().hasVideo()) {
            Long chatId = update.getMessage().getFrom().getId();
            Video video = update.getMessage().getVideo();
            String text = update.getMessage().getCaption();
            String videoUrl = video.getFileId();
            Integer messageId = update.getMessage().getMessageId();
            Automobile currentAutomobile = currentAdAuto.getOrDefault(chatId, new Automobile());
            if (videoUrl != null) {
                log.info("Videourl for Ad: " + videoUrl);
                saveUrl(text, null, videoUrl, currentAutomobile);
                userStateTrans.setUserState(chatId, WAITING_FOR_CONFIRMATION);
            }
            userState.setUserState(chatId, AUTO);
        }
    }

    private void getInlButton(Long chatId) {

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
    }


    private void deleteMyAutoAd(Long chatId, Long adId) {
        boolean deleted = automobileService.deleteAdByUserIdAndAdId(chatId, adId);
        if (deleted) {
            botCallback.sendMessageWithInlKeyboard(chatId, "Ihre Anzeigen wurden gelöscht.", null);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.", null);
        }
    }

    private void editMyAd(Long chatId) {
        List<Automobile> userAutomobiles = automobileService.findByUserId(chatId);
        if (userAutomobiles.isEmpty()) {
            botCallback.sendMessageWithInlKeyboard(chatId, "Sie haben keine Anzeigen zum Bearbeiten.", null);
            return;
        }
        for (Automobile automobile : userAutomobiles) {
            List<String> imageUrl = automobile.getImageUrl();
            botCallback.sendPhotoWithInlKeyboard(chatId, automobile.getDescription(), imageUrl.get(0), button.inlKeyboardForMyAds());
        }
    }

    private void addToFavorite(Long chatId, String text) {
        BotUser user = userService.findByTelegramId(chatId);
        String[] parts = text.split("_");
        Long adId = Long.valueOf(parts[1]);
        automobileService.addFavorite(user.getId(), adId);
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
            log.info("text: " + text + "  " + TransButtonConstant.favorite);
            InlineKeyboardMarkup newKeyboard = button.inlKeyboardAddFav(adId, nextIndex);
            botCallback.editImageMessage(chatId, messageId, captionText, null, null, newKeyboard);

        }
    }

    private void getMyAds(Long chatId) {
        List<Automobile> myAutomobiles = automobileService.findByUserId(chatId);
        if (!myAutomobiles.isEmpty()) {
            for (Automobile automobile : myAutomobiles) {
                List<String> photoUrls = automobile.getImageUrl();
                botCallback.sendPhotoWithInlKeyboard(chatId, automobile.getDescription(), photoUrls.get(0), button.inlKeyboardForMyAds());
            }
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Sizda e'lon-pelon jo'qku \uD83D\uDE04", null);
        }

    }

    private void cancelAutoAd(Long chatId) {
        if (userStateTrans.getUserState(chatId).equals(WAITING_FOR_CONFIRMATION)) {
            currentAdAuto.remove(chatId);
            userStateTrans.setUserState(chatId, AUTO_AD_CANCELLED);
            botCallback.sendMessageWithInlKeyboard(chatId, "E'lon bekor qilindi", null);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "E'lon tasdiqlab bo'lindi, " + "Mening e'lonlarim bo'limidan o'chirishingiz mumkin", null);
        }

    }

    private void saveUrl(String text, String photoUrl, String videoUrl, Automobile currentAutomobile) {
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

    }

    private void finalizeAndSaveAd(Long chatId, Automobile currentAutomobile) {
        if (userStateTrans.getUserState(chatId).equals(WAITING_FOR_CONFIRMATION)) {
            BotUser user = userService.findByTelegramId(chatId);
            currentAutomobile.setUser(user);
            currentAdAuto.put(chatId, currentAutomobile);
            automobileService.saveAutomobile(currentAutomobile);
            currentAdAuto.remove(chatId);
            userStateTrans.setUserState(chatId, AUTO_AD_PLACED);
            botCallback.sendMessageWithInlKeyboard(chatId, "Avto E'loningiz joylandi", null);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Siz e'lonni bekor qilgansiz, qaytadan yuklang", null);
        }

    }

    private void initializeNewAd(Long chatId) {
        BotUser user = userService.findByTelegramId(chatId);
        Automobile newAutomobile = new Automobile();
        newAutomobile.setUser(user);
        currentAdAuto.put(chatId, newAutomobile);
        botCallback.sendInlineKeyboardCites(chatId);
        userStateTrans.setUserState(chatId, PLACE_AUTOMOBILE_AD);
    }

    private void validateUserState(Long chatId, String text, Automobile currentAutomobile, AgroTechnology currentAgroTech, Truck currentTruck,
                                   OtherTransport currentOtherTech, SpareParts currentSpareParts) {
        log.info("Text: " + text + "User State: " + userStateTrans.getUserState(chatId));
        switch (userStateTrans.getUserState(chatId)) {
            case PLACE_AUTOMOBILE_AD -> {
                botCallback.sendMessageWithInlKeyboard(chatId, MessageText.autoAdExample, null);
                Standort standort = standortService.saveCity(text);
                currentAutomobile.setStandort(standort);
                userStateTrans.setUserState(chatId, SEND_PHOTO_AUTO);
            }
            case PLACE_AGRO_TECH_AD -> {
                botCallback.sendMessageWithInlKeyboard(chatId, MessageText.autoAdExample, null);
                Standort standort = standortService.saveCity(text);
                currentAgroTech.setStandort(standort);
                userStateTrans.setUserState(chatId, SEND_PHOTO_AUTO);
            }
            case PLACE_TRUCK_AD -> {
                botCallback.sendMessageWithInlKeyboard(chatId, MessageText.autoAdExample, null);
                Standort standort = standortService.saveCity(text);
                currentTruck.setStandort(standort);
                userStateTrans.setUserState(chatId, SEND_PHOTO_AUTO);
            }
            case PLACE_OTHER_TECH_AD -> {
                botCallback.sendMessageWithInlKeyboard(chatId, MessageText.autoAdExample, null);
                Standort standort = standortService.saveCity(text);
                currentOtherTech.setStandort(standort);
                userStateTrans.setUserState(chatId, SEND_PHOTO_AUTO);
            }
            case PLACE_SPARE_PARTS_AD -> {
                botCallback.sendMessageWithInlKeyboard(chatId, MessageText.autoAdExample, null);
                Standort standort = standortService.saveCity(text);
                currentSpareParts.setStandort(standort);
                userStateTrans.setUserState(chatId, SEND_PHOTO_AUTO);
            }
            case AUTO_SECTION -> handleAutoSection(chatId, text);
            case TRUCK_SECTION -> handleTruckSection(chatId,text);
            case AGRO_TECH_SECTION -> handleAgroTechSection(chatId,text);
            case OTHER_TRANS_SECTION -> handleOtherTransSection(chatId,text);
            case SPARE_PARTS_SECTION -> handleSparePartsSection(chatId,text);

        }


        if (userStateTrans.getUserState(chatId).equals(SEARCH_AD_AUTO)) {

            if (text.equals("Hammasini ko'rsatish")) {

                List<Automobile> allAutomobiles = automobileService.findAll();
                log.info("All ads: " + allAutomobiles.size());
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
            userStateTrans.setUserState(chatId, SEARCH_AD_AUTO);

        }
        photoIndexMap.clear();

    }

    private void handleSparePartsSection(Long chatId, String text) {
    }

    private void handleOtherTransSection(Long chatId, String text) {
    }

    private void handleAgroTechSection(Long chatId, String text) {
    }

    private void handleTruckSection(Long chatId, String text) {
        if (text.equals(TransButtonConstant.placeAutoAd))
    }

    private void handleAutoSection(Long chatId, String text) {
        if (text.equals(TransButtonConstant.placeAutoAd)){
            botCallback.sendMessageWithInlKeyboard(chatId, MessageText.autoAdExample,null);
            userStateTrans.setUserState(chatId,PLACE_AUTOMOBILE_AD);

        }else {
            
        }
    }

    private void getAd(Long chatId, List<Automobile> automobiles) {

        int currentIndex = userPageState.getOrDefault(chatId, automobiles.size());  // Starte mit der letzten Anzeige
        int startIndex = Math.max(0, currentIndex - PAGE_SIZE);
        log.info("currentIndex: " + currentIndex + " startIndex: " + startIndex);
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

    public void nextPage(Long chatId) {
        List<Automobile> allAutomobiles = automobileService.findAll();
        int currentIndex = userPageState.getOrDefault(chatId, allAutomobiles.size());
        if (currentIndex > 0) {
            getAd(chatId, allAutomobiles);
        } else {
            botCallback.sendMessageWithInlKeyboard(chatId, "Jo'q boshqa e'lon-pelon! \uD83D\uDE04", null);
        }
    }

    public void previousPage(Long chatId) {
        int currentIndex = userPageState.getOrDefault(chatId, 0) + PAGE_SIZE;
        userPageState.put(chatId, Math.min(automobileService.findAll().size(), currentIndex));
        List<Automobile> allAutomobiles = automobileService.findAll();
        getAd(chatId, allAutomobiles);
    }


    private void autoSearch(Long chatId) {
        botCallback.sendInKeyboardForSearch(chatId);
        userStateTrans.setUserState(chatId, SEARCH_AD_AUTO);
        photoIndexMap.clear();

    }

    private void auto(Long chatId) {
        botCallback.sendMessageWithReplyKeyboard(chatId, "Quyidagilardan birini tanlang!", button.autoMenu());
        userState.setUserState(chatId, AUTO);
        photoIndexMap.clear();
    }

    private void back(Long chatId) {
        botCallback.menu(chatId);
        userState.setUserState(chatId, START);
        photoIndexMap.clear();

    }

}
