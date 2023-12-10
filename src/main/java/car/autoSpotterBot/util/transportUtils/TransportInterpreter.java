package car.autoSpotterBot.util.transportUtils;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.state.UserStateManager;
import car.autoSpotterBot.state.UserStateTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Comparator;
import java.util.List;

import static car.autoSpotterBot.state.UserStateConstants.*;

@Component
public class TransportInterpreter {
    private static final Logger log = LoggerFactory.getLogger(TransportInterpreter.class);
    private final Button button;
    private final BotCallback botCallback;
    private final UserStateTransport userStateTransport;
    private final UserStateManager userStateManager;
    private final AutoInterpreter autoInterpreter;
    private final TruckInterpreter truckInterpreter;
    private final AgroTechInterpreter agroTechInterpreter;
    private final OtherTransInterpreter otherTrans;
    private final SparePartsInterpreter sparePartsInterpreter;

    public TransportInterpreter(BotCallback botCallback, Button buttonService, UserStateTransport userStateTransport, UserStateManager userStateManager, AutoInterpreter autoInterpreter, TruckInterpreter truckInterpreter, AgroTechInterpreter agroTechInterpreter, OtherTransInterpreter otherTechInterpreter, SparePartsInterpreter sparePartsInterpreter) {
        this.button = buttonService;
        this.botCallback = botCallback;
        this.userStateTransport = userStateTransport;
        this.userStateManager = userStateManager;
        this.autoInterpreter = autoInterpreter;
        this.truckInterpreter = truckInterpreter;
        this.agroTechInterpreter = agroTechInterpreter;
        this.otherTrans = otherTechInterpreter;
        this.sparePartsInterpreter = sparePartsInterpreter;
    }

    public void transportInterpreter(Update update) {
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            int messageId = update.getMessage().getMessageId();
             if (update.hasMessage() && update.getMessage().hasPhoto()) {
                List<PhotoSize> photoSizes = update.getMessage().getPhoto();
                PhotoSize largestPhoto = photoSizes.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
                String photoUrl = largestPhoto.getFileId();
                String caption = update.getMessage().getCaption();
                if (userStateTransport.getUserStatusTransport(chatId).equals(AUTOMOBIL)) {
                    autoInterpreter.autointerpreter(chatId, messageId, caption, photoUrl, null);
                } else if (userStateTransport.getUserStatusTransport(chatId).equals(TRUCK)) {
                    truckInterpreter.interpreter(chatId,messageId,caption,photoUrl,null);
                } else if (userStateTransport.getUserStatusTransport(chatId).equals(AGRO_TECH)) {
                    agroTechInterpreter.interpreter(chatId,messageId,caption,photoUrl,null);
                }else if (userStateTransport.getUserStatusTransport(chatId).equals(SPARE_PARTS)) {
                    sparePartsInterpreter.interpreter(chatId,messageId,caption,photoUrl,null);
                }else if (userStateTransport.getUserStatusTransport(chatId).equals(OTHER_TRANS)) {
                    otherTrans.interpreter(chatId,messageId,caption,photoUrl,null);
                }

            } else if (update.hasMessage() && update.getMessage().hasVideo()) {
                Video video = update.getMessage().getVideo();
                String videoUrl = video.getFileId();
                String caption = update.getMessage().getCaption();
                if (userStateTransport.getUserStatusTransport(chatId).equals(AUTOMOBIL)) {
                    autoInterpreter.autointerpreter(chatId, messageId, caption, null, videoUrl);
                }else if (userStateTransport.getUserStatusTransport(chatId).equals(TRUCK)) {
                    truckInterpreter.interpreter(chatId,messageId,caption,null,videoUrl);
                } else if (userStateTransport.getUserStatusTransport(chatId).equals(AGRO_TECH)) {
                    agroTechInterpreter.interpreter(chatId,messageId,caption,null,videoUrl);
                }else if (userStateTransport.getUserStatusTransport(chatId).equals(SPARE_PARTS)) {
                    sparePartsInterpreter.interpreter(chatId,messageId,caption,null,videoUrl);
                }else if (userStateTransport.getUserStatusTransport(chatId).equals(OTHER_TRANS)) {
                    otherTrans.interpreter(chatId,messageId,caption,null,videoUrl);
                }

            }

        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getFrom().getId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            String text = update.getCallbackQuery().getData();
            if (userStateTransport.getUserStatusTransport(chatId) != null &&
                    userStateTransport.getUserStatusTransport(chatId).equals(AUTOMOBIL)) {
                autoInterpreter.autointerpreter(chatId, messageId, text, null, null);
            } else if (userStateTransport.getUserStatusTransport(chatId) != null &&
                    userStateTransport.getUserStatusTransport(chatId).equals(TRUCK)) {
                truckInterpreter.interpreter(chatId,messageId,text,null,null);
            }else if (userStateTransport.getUserStatusTransport(chatId) != null &&
                    userStateTransport.getUserStatusTransport(chatId).equals(AGRO_TECH)) {
                agroTechInterpreter.interpreter(chatId,messageId,text,null,null);
            }else if (userStateTransport.getUserStatusTransport(chatId) != null &&
                    userStateTransport.getUserStatusTransport(chatId).equals(SPARE_PARTS)) {
                sparePartsInterpreter.interpreter(chatId,messageId,text,null,null);
            }else if (userStateTransport.getUserStatusTransport(chatId) != null &&
                    userStateTransport.getUserStatusTransport(chatId).equals(OTHER_TRANS)) {
                otherTrans.interpreter(chatId,messageId,text,null,null);
            }

            switch (text) {
                case ButtonConstant.automobile -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateTransport.setUserStatusTransport(chatId, AUTOMOBIL);
                }
                case ButtonConstant.truck -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateTransport.setUserStatusTransport(chatId, TRUCK);
                }
                case ButtonConstant.agroTech -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateTransport.setUserStatusTransport(chatId, AGRO_TECH);
                }
                case ButtonConstant.spareParts -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateTransport.setUserStatusTransport(chatId, SPARE_PARTS);
                }
                case ButtonConstant.otherTrans -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateTransport.setUserStatusTransport(chatId, OTHER_TRANS);
                }
            }

        }
    }
    private void checkUserSubStatus(long chatId, int messageId, InlineKeyboardMarkup newKeyboard) {
        if (userStateManager.getUserSubStatus(chatId).equals(PLACE_AD)) {
            botCallback.editMessage(chatId, messageId, "Qaysi viloyatda e'lon bermoqchisiz?", newKeyboard);
        } else {
            botCallback.editMessage(chatId, messageId, "Qaysi viloyatda qidirmoqchisiz?", newKeyboard);
        }
    }
}
