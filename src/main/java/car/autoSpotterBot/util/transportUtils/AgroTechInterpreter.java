package car.autoSpotterBot.util.transportUtils;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.model.transport.AgroTechnology;
import car.autoSpotterBot.service.GeneralService;
import car.autoSpotterBot.state.UserStateManager;
import car.autoSpotterBot.state.UserStateTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static car.autoSpotterBot.state.UserStateConstants.*;

@Component
public class AgroTechInterpreter {
    private static final Logger log = LoggerFactory.getLogger(AgroTechInterpreter.class);
    private final Button button;
    private final BotCallback botCallback;
    private final GeneralService generalService;
    private final UserStateManager userStateManager;
    private final Map<Long, AgroTechnology> currentAd = new ConcurrentHashMap<>();
    private final UserStateTransport userStateTransport;

    public AgroTechInterpreter(Button button, BotCallback botCallback, GeneralService generalService, UserStateManager userStateManager, UserStateTransport userStateTransport) {
        this.button = button;
        this.botCallback = botCallback;
        this.generalService = generalService;
        this.userStateManager = userStateManager;
        this.userStateTransport = userStateTransport;
    }

    public void interpreter(long chatId, int messageId, String text, String photoUrl, String videoUrl) {

        AgroTechnology currentAd = this.currentAd.getOrDefault(chatId, new AgroTechnology());
        if (text != null) {
            if (text.equals(ButtonConstant.backInAutoAd)) {
                InlineKeyboardMarkup newButton = button.transMenu();
                checkUserSubStatus(chatId, messageId, newButton);
            }
            if (text.startsWith("page")) {
                int id = splitText(text);
                generalService.displayNextPage(chatId, AgroTechnology.class, id);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.startsWith(ButtonConstant.nextPhoto) || text.startsWith(ButtonConstant.previousPhoto) ||
                    text.startsWith(ButtonConstant.video) || text.startsWith(ButtonConstant.favorite)) {
                generalService.getNextPhoto(chatId, text, messageId, AgroTechnology.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.startsWith(ButtonConstant.favorite)) {
                generalService.addToFavorite(chatId, text, AgroTechnology.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (userStateManager.getUserSubStatus(chatId) != null && userStateManager.getUserSubStatus(chatId).equals(SEARCH_AD) && !text.equals(ButtonConstant.backInAutoAd)) {
                generalService.searchAd(chatId, text, AgroTechnology.class);
            }
            if (userStateManager.getUserSubStatus(chatId) != null && !userStateManager.getUserSubStatus(chatId).equals(SEARCH_AD)) {
                saveLocation(chatId, messageId, text, currentAd);
            } else if (userStateManager.getUserSubStatus(chatId) == null) {
                botCallback.sendMessageWithInlKeyboard(chatId, "E'lon berish uchun boshidan boshlang", null);
            }

            if (text.equals(ButtonConstant.confirm)) {
                confirmAd(chatId, messageId, currentAd);
            }
            if (text.equals(ButtonConstant.cancel)) {
                cancelAutoAd(chatId);
            }
        }

        if (photoUrl != null && userStateManager.getUserSubStatus(chatId) != null) {
            botCallback.deleteMessageLater(chatId,messageId, 10);
            savePhotoUrl(chatId, text, photoUrl, currentAd);
        }
        if (videoUrl != null && userStateManager.getUserSubStatus(chatId) != null) {
            botCallback.deleteMessageLater(chatId,messageId, 10);
            saveVideoUrl(chatId, text, videoUrl, currentAd);
        }
    }

    private void checkUserSubStatus(long chatId, int messageId, InlineKeyboardMarkup newKeyboard) {
        if (userStateManager.getUserSubStatus(chatId).equals(PLACE_AD)) {
            botCallback.editMessage(chatId, messageId, "Qaysi transport uchun e'lon bermoqchisiz?", newKeyboard);
        } else {
            botCallback.editMessage(chatId, messageId, "Qanday transport vositasini qidiryapsiz?", newKeyboard);
        }
        userStateTransport.setUserStatusTransport(chatId, null);
    }

    private Integer splitText(String text) {
        String[] parts = text.split("_");
        return Integer.parseInt(parts[1]);
    }

    private void savePhotoUrl(long chatId, String text, String photoUrl, AgroTechnology currentAd) {
        if (text != null) {
            botCallback.sendPhotoWithInlKeyboard(chatId, currentAd.getDescription(), photoUrl, button.inlKeyboardConfirmation());
            userStateTransport.setUserStateAuto(chatId, PHOTO);
            generalService.saveUrl(text, photoUrl, null, currentAd);
        }
        if (userStateTransport.getUserStateAuto(chatId) == null || !userStateTransport.getUserStateAuto(chatId).equals(EMPTY) &&
                !userStateTransport.getUserStateAuto(chatId).equals(PHOTO) && !userStateTransport.getUserStateAuto(chatId).equals(VIDEO)) {
            botCallback.sendMessageWithInlKeyboard(chatId, " Foto E'lon matnini rasm va videolar bilan birgalikda yuboring", null);
            userStateTransport.setUserStateAuto(chatId, EMPTY);
        }
        if (userStateTransport.getUserStateAuto(chatId) != null && userStateTransport.getUserStateAuto(chatId).equals(PHOTO) ||
                userStateTransport.getUserStateAuto(chatId).equals(VIDEO)) {
            generalService.saveUrl(text, photoUrl, null, currentAd);
        }
    }

    private void saveVideoUrl(long chatId, String text, String videoUrl, AgroTechnology currentAd) {
        if (text != null) {
            botCallback.sendVideoWithInlKeyboard(chatId, currentAd.getDescription(), videoUrl, button.inlKeyboardConfirmation());
            userStateTransport.setUserStateAuto(chatId, VIDEO);
            generalService.saveUrl(text, null, videoUrl, currentAd);
        }
        if (userStateTransport.getUserStateAuto(chatId) == null || !userStateTransport.getUserStateAuto(chatId).equals(EMPTY) &&
                !userStateTransport.getUserStateAuto(chatId).equals(PHOTO) && !userStateTransport.getUserStateAuto(chatId).equals(VIDEO)) {
            botCallback.sendMessageWithInlKeyboard(chatId, "Video E'lon matnini rasm va videolar bilan birgalikda yuboring", null);
            userStateTransport.setUserStateAuto(chatId, EMPTY);
        }
        if (userStateTransport.getUserStateAuto(chatId) != null && userStateTransport.getUserStateAuto(chatId).equals(PHOTO) ||
                userStateTransport.getUserStateAuto(chatId).equals(VIDEO)) {
            generalService.saveUrl(text, null, videoUrl, currentAd);
        }
    }

    private void confirmAd(long chatId, int messageId, AgroTechnology currentAd) {
        generalService.finalizeAndSaveAd(chatId, currentAd);
        this.currentAd.remove(chatId);
        this.currentAd.clear();
        userStateManager.setUserSubStatus(chatId, null);
        userStateTransport.setUserStateAuto(chatId, null);
        botCallback.deleteMessageLater(chatId,messageId,10);
    }

    private void cancelAutoAd(Long chatId) {
        currentAd.remove(chatId);
        Message message = botCallback.sendMessageWithInlKeyboard(chatId, "E'lon bekor qilindi", null);
        botCallback.deleteMessageLater(chatId,message.getMessageId(),10);
        currentAd.clear();
        userStateManager.setUserSubStatus(chatId, null);
        userStateTransport.setUserStateAuto(chatId, null);
    }

    private void saveLocation(long chatId, int messageId, String text, AgroTechnology currentAd) {
        switch (text) {
            case "Toshkent", "Andijon", "Buxoro", "Farg'ona", "Jizzax", "Sirdaryo", "Namangan", "Samarqand",
                    "Xorazm", "Surxandaryo", "Qashqadaryo", "Qoraqalpog'iston", "Navoi" -> {
                generalService.setStandort(text, currentAd);
                this.currentAd.put(chatId, currentAd);
                botCallback.editMessage(chatId, messageId, MessageText.autoAdExample, null);
            }
        }
    }
}
