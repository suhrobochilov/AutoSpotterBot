package car.autoSpotterBot.util.transportUtils;


import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.model.transport.Truck;
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
public class TruckInterpreter {
    private static final Logger log = LoggerFactory.getLogger(AutoInterpreter.class);
    private final Button button;
    private final BotCallback botCallback;
    private final TransportService transportService;
    private final UserStateManager userStateManager;
    private final Map<Long, Truck> currentAdAuto = new ConcurrentHashMap<>();
    private final UserStateTransport userStateTransport;

    public TruckInterpreter(Button button, BotCallback botCallback, TransportService transportService, UserStateManager userStateManager, UserStateTransport userStateTransport) {
        this.button = button;
        this.botCallback = botCallback;
        this.transportService = transportService;
        this.userStateManager = userStateManager;
        this.userStateTransport = userStateTransport;
    }
    public void interpreter( long chatId, int messageId, String text, String photoUrl, String videoUrl) {

        Truck currentTruck = currentAdAuto.getOrDefault(chatId, new Truck());
        if (text != null) {
            if (text.equals(ButtonConstant.backInAutoAd)) {
                InlineKeyboardMarkup newButton = button.transMenu();
                checkUserSubStatus(chatId,messageId,newButton);
            }
            if (text.equals(ButtonConstant.nextPage)) {
                botCallback.deleteMessage(chatId, messageId);
                transportService.displayNextPage(chatId, Truck.class,2);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.equals(ButtonConstant.previousPage)) {
                transportService.displayPreviousPage(chatId, Truck.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.startsWith(ButtonConstant.nextPhoto) || text.startsWith(ButtonConstant.previousPhoto) ||
                    text.startsWith(ButtonConstant.video) || text.startsWith(ButtonConstant.favorite)) {
                transportService.getNextPhoto(chatId, text, messageId, Truck.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.startsWith(ButtonConstant.favorite)) {
                transportService.addToFavorite(chatId, text, Truck.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (userStateManager.getUserSubStatus(chatId).equals(SEARCH_AD) && !text.equals(ButtonConstant.backInAutoAd)) {
                transportService.searchAd(chatId, text, Truck.class);
                botCallback.sendMessageWithReplyKeyboard(chatId, "Keyingi e'lonlarni ko'rish uchun \uD83D\uDC47", button.nextPage());
            }

            if (text.equals(ButtonConstant.cancel)) {
                cancelAutoAd(chatId);
                botCallback.deleteMessageLater(chatId, messageId, 10);
            }
            if (!userStateManager.getUserSubStatus(chatId).equals(SEARCH_AD)) {
                switch (text) {
                    case "Toshkent", "Andijon", "Buxoro", "Farg'ona", "Jizzax", "Sirdaryo", "Namangan", "Samarqand",
                            "Xorazm", "Surxandaryo", "Qashqadaryo", "Qoraqalpog'iston", "Navoi" -> {
                        transportService.setStandort(text, currentTruck);
                        currentAdAuto.put(chatId, currentTruck);
                        botCallback.editMessage(chatId, messageId, MessageText.autoAdExample, null);
                    }
                }
            }
            if (text.equals(ButtonConstant.confirm)) {
                transportService.finalizeAndSaveAd(chatId, currentTruck);
                botCallback.deleteMessageLater(chatId, messageId, 10);
                currentAdAuto.clear();
                userStateManager.setUserSubStatus(chatId, null);
            }
        }

        if (photoUrl != null && userStateManager.getUserSubStatus(chatId) != null) {
            transportService.saveUrl(text, photoUrl, null, currentTruck);
            botCallback.deleteMessage(chatId, messageId);

            if (text != null) {
                botCallback.sendPhotoWithInlKeyboard(chatId, currentTruck.getDescription(), photoUrl, button.inlKeyboardConfirmation());
            }
        }

        if (videoUrl != null && userStateManager.getUserSubStatus(chatId) != null) {
            transportService.saveUrl(text, null, videoUrl, currentTruck);
            if (text != null) {
                botCallback.sendVideoWithInlKeyboard(chatId, currentTruck.getDescription(), videoUrl, button.inlKeyboardConfirmation());
            }
        }
    }

    private void cancelAutoAd(Long chatId) {
        currentAdAuto.remove(chatId);
        botCallback.sendMessageWithInlKeyboard(chatId, "E'lon bekor qilindi", null);
        currentAdAuto.clear();
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
