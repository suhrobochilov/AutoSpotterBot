package car.autoSpotterBot.util.transportUtils;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.model.transport.SpareParts;
import car.autoSpotterBot.service.transport.TransportService;
import car.autoSpotterBot.state.UserStateManager;
import car.autoSpotterBot.state.UserStateTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static car.autoSpotterBot.state.UserStateConstants.PLACE_AD;
import static car.autoSpotterBot.state.UserStateConstants.SEARCH_AD;

@Component
public class SparePartsInterpreter {
    private static final Logger log = LoggerFactory.getLogger(AutoInterpreter.class);
    private final Button button;
    private final BotCallback botCallback;
    private final TransportService transportService;
    private final UserStateManager userStateManager;
    private final UserStateTransport userStateTransport;
    private final Map<Long, SpareParts> currentAdMap = new ConcurrentHashMap<>();

    public SparePartsInterpreter(Button button, BotCallback botCallback, TransportService transportService, UserStateManager userStateManager, UserStateTransport userStateTransport) {
        this.button = button;
        this.botCallback = botCallback;
        this.transportService = transportService;
        this.userStateManager = userStateManager;
        this.userStateTransport = userStateTransport;
    }

    public void interpreter( long chatId, int messageId, String text, String photoUrl, String videoUrl) {

        SpareParts currentAd = currentAdMap.getOrDefault(chatId, new SpareParts());
        if (text != null) {
            if (text.equals(ButtonConstant.backInAutoAd)) {
                InlineKeyboardMarkup newButton = button.transMenu();
                checkUserSubStatus(chatId,messageId,newButton);
            }
            if (text.equals(ButtonConstant.nextPage)) {
                botCallback.deleteMessage(chatId, messageId);
                transportService.displayNextPage(chatId, SpareParts.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.equals(ButtonConstant.previousPage)) {
                transportService.displayPreviousPage(chatId, SpareParts.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.startsWith(ButtonConstant.nextPhoto) || text.startsWith(ButtonConstant.previousPhoto) ||
                    text.startsWith(ButtonConstant.video) || text.startsWith(ButtonConstant.favorite)) {
                transportService.getNextPhoto(chatId, text, messageId, SpareParts.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.startsWith(ButtonConstant.favorite)) {
                transportService.addToFavorite(chatId, text, SpareParts.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (userStateManager.getUserSubStatus(chatId).equals(SEARCH_AD) && !text.equals(ButtonConstant.backInAutoAd)) {
                transportService.searchAd(chatId, text, SpareParts.class);
                botCallback.sendMessageWithReplyKeyboard(chatId, "Keyingi e'lonlarni ko'rish uchun \uD83D\uDC47", button.nextPage());
            }

            if (text.equals(ButtonConstant.confirm)) {
                transportService.finalizeAndSaveAd(chatId, currentAd, botCallback);
                botCallback.deleteMessageLater(chatId, messageId, 10);
                currentAdMap.clear();
            }
            if (text.equals(ButtonConstant.cancel)) {
                cancelAutoAd(chatId);
                botCallback.deleteMessageLater(chatId, messageId, 10);
            }
            if (!userStateManager.getUserSubStatus(chatId).equals(SEARCH_AD)) {
                switch (text) {
                    case "Toshkent", "Andijon", "Buxoro", "Farg'ona", "Jizzax", "Sirdaryo", "Namangan", "Samarqand",
                            "Xorazm", "Surxandaryo", "Qashqadaryo", "Qoraqalpog'iston", "Navoi" -> {
                        transportService.setStandort(text, currentAd);
                        currentAdMap.put(chatId, currentAd);
                        botCallback.editMessage(chatId, messageId, MessageText.autoAdExample, null);
                    }
                }
            }
        }

        if (photoUrl != null) {
            transportService.saveUrl(text, photoUrl, null, currentAd);
            botCallback.deleteMessage(chatId, messageId);

            if (text != null) {
                botCallback.sendPhotoWithInlKeyboard(chatId, currentAd.getDescription(), photoUrl, button.inlKeyboardConfirmation());
            }
        }

        if (videoUrl != null) {
            transportService.saveUrl(text, null, videoUrl, currentAd);
            if (text != null) {
                botCallback.sendVideoWithInlKeyboard(chatId, currentAd.getDescription(), videoUrl, button.inlKeyboardConfirmation());
            }
        }
    }

    private void cancelAutoAd(Long chatId) {
        currentAdMap.remove(chatId);
        botCallback.sendMessageWithInlKeyboard(chatId, "E'lon bekor qilindi", null);
        currentAdMap.clear();
    }

    private void checkUserSubStatus(long chatId, int messageId, InlineKeyboardMarkup newKeyboard) {
        if (userStateManager.getUserSubStatus(chatId).equals(PLACE_AD)) {
            botCallback.editMessage(chatId, messageId, "Qaysi transport uchun e'lon bermoqchisiz?", newKeyboard);
        } else {
            botCallback.editMessage(chatId, messageId, "Qanday transport vositasini qidiryapsiz?", newKeyboard);
        }
        userStateTransport.setUserStatusTransport(chatId,null);
    }
}
