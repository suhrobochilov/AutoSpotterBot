package car.autoSpotterBot.util.realEstateUtils;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.model.realeState.BuildingLot;
import car.autoSpotterBot.service.realEstate.RealEstateService;
import car.autoSpotterBot.state.UserStateManager;
import car.autoSpotterBot.state.UserStateRealEstate;
import car.autoSpotterBot.util.transportUtils.AutoInterpreter;
import car.autoSpotterBot.util.transportUtils.BotCallback;
import car.autoSpotterBot.util.transportUtils.MessageText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static car.autoSpotterBot.state.UserStateConstants.PLACE_AD;
import static car.autoSpotterBot.state.UserStateConstants.SEARCH_AD;

@Component
public class BuildingLotInterpreter {
    private static final Logger log = LoggerFactory.getLogger(AutoInterpreter.class);
    private final Button button;
    private final BotCallback botCallback;
    private final RealEstateService realEstateService;
    private final UserStateManager userStateManager;
    private final Map<Long, BuildingLot> currentAd = new ConcurrentHashMap<>();
    private final UserStateRealEstate userStateRealEstate;

    public BuildingLotInterpreter(Button button, BotCallback botCallback, RealEstateService realEstateService, UserStateManager userStateManager, UserStateRealEstate userStateRealEstate) {
        this.button = button;
        this.botCallback = botCallback;
        this.realEstateService = realEstateService;
        this.userStateManager = userStateManager;
        this.userStateRealEstate = userStateRealEstate;
    }

    public void interpreter(long chatId, int messageId, String text, String photoUrl, String videoUrl) {

        BuildingLot currentBuildingLot = currentAd.getOrDefault(chatId, new BuildingLot());
        if (text != null) {
            if (text.equals(ButtonConstant.backInAutoAd)) {
                log.info("State: " + userStateManager.getUserSubStatus(chatId));
                InlineKeyboardMarkup newButton = button.transMenu();
                checkUserSubStatus(chatId,messageId,newButton);
            }
            if (text.equals(ButtonConstant.nextPage)) {
                botCallback.deleteMessage(chatId, messageId);
                realEstateService.displayNextPage(chatId, BuildingLot.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.equals(ButtonConstant.previousPage)) {
                realEstateService.displayPreviousPage(chatId, BuildingLot.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.startsWith(ButtonConstant.nextPhoto) || text.startsWith(ButtonConstant.previousPhoto) ||
                    text.startsWith(ButtonConstant.video) || text.startsWith(ButtonConstant.favorite)) {
                realEstateService.getNextPhoto(chatId, text, messageId, BuildingLot.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (text.startsWith(ButtonConstant.favorite)) {
                realEstateService.addToFavorite(chatId, text, BuildingLot.class);
                userStateManager.setUserSubStatus(chatId, PLACE_AD);
            }
            if (userStateManager.getUserSubStatus(chatId).equals(SEARCH_AD) && !text.equals(ButtonConstant.backInAutoAd)) {
                realEstateService.searchAd(chatId, text, BuildingLot.class);
                botCallback.sendMessageWithReplyKeyboard(chatId, "Keyingi e'lonlarni ko'rish uchun \uD83D\uDC47", button.nextPage());
            }

            if (text.equals(ButtonConstant.confirm)) {
                realEstateService.finalizeAndSaveAd(chatId, currentBuildingLot, botCallback);
                botCallback.deleteMessageLater(chatId, messageId, 10);
                currentAd.clear();
            }
            if (text.equals(ButtonConstant.cancel)) {
                cancelAutoAd(chatId);
                botCallback.deleteMessageLater(chatId, messageId, 10);
            }
            if (!userStateManager.getUserSubStatus(chatId).equals(SEARCH_AD)) {
                switch (text) {
                    case "Toshkent", "Andijon", "Buxoro", "Farg'ona", "Jizzax", "Sirdaryo", "Namangan", "Samarqand",
                            "Xorazm", "Surxandaryo", "Qashqadaryo", "Qoraqalpog'iston", "Navoi" -> {
                        realEstateService.setStandort(text, currentBuildingLot);
                        currentAd.put(chatId, currentBuildingLot);
                        botCallback.editMessage(chatId, messageId, MessageText.autoAdExample, null);
                    }
                }
            }
        }

        if (photoUrl != null) {
            realEstateService.saveUrl(text, photoUrl, null, currentBuildingLot);
            botCallback.deleteMessage(chatId, messageId);

            if (text != null) {
                botCallback.sendPhotoWithInlKeyboard(chatId, currentBuildingLot.getDescription(), photoUrl, button.inlKeyboardConfirmation());
            }
        }

        if (videoUrl != null) {
            realEstateService.saveUrl(text, null, videoUrl, currentBuildingLot);
            if (text != null) {
                botCallback.sendVideoWithInlKeyboard(chatId, currentBuildingLot.getDescription(), videoUrl, button.inlKeyboardConfirmation());
            }
        }
    }

    private void cancelAutoAd(Long chatId) {
        currentAd.remove(chatId);
        botCallback.sendMessageWithInlKeyboard(chatId, "E'lon bekor qilindi", null);
        currentAd.clear();
    }

    private void checkUserSubStatus(long chatId, int messageId, InlineKeyboardMarkup newKeyboard) {
        if (userStateManager.getUserSubStatus(chatId).equals(PLACE_AD)) {
            botCallback.editMessage(chatId, messageId, "Qaysi transport uchun e'lon bermoqchisiz?", newKeyboard);
        } else {
            botCallback.editMessage(chatId, messageId, "Qanday transport vositasini qidiryapsiz?", newKeyboard);
        }
        userStateRealEstate.setUserStatusRealEstate(chatId,null);
    }
}
