package car.autoSpotterBot;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.configuration.BotConfig;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.service.AdService;
import car.autoSpotterBot.service.BotUserService;
import car.autoSpotterBot.service.transport.TransportService;
import car.autoSpotterBot.state.UserStateConstants;
import car.autoSpotterBot.state.UserStateManager;
import car.autoSpotterBot.state.UserStateRealEstate;
import car.autoSpotterBot.state.UserStateTransport;
import car.autoSpotterBot.util.MessageId;
import car.autoSpotterBot.util.realEstateUtils.RealEstateInterpreter;
import car.autoSpotterBot.util.transportUtils.BotCallback;
import car.autoSpotterBot.util.transportUtils.MessageText;
import car.autoSpotterBot.util.transportUtils.TransportInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static car.autoSpotterBot.state.UserStateConstants.*;

@Component
public class MyBot extends TelegramLongPollingBot implements BotCallback {
    private static final Logger log = LoggerFactory.getLogger(MyBot.class);
    private final Button button;
    private final BotUserService userService;
    private final UserStateManager userStateManager;
    private final UserStateTransport userStateTransport;
    private final UserStateRealEstate userStateRealEstate;
    private final BotConfig botConfig;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final MessageId allMessageIds;
    private AdService adService;
    private TransportInterpreter transportInterpreter;
    private RealEstateInterpreter realEstateInterpreter;
    private TransportService transportService;


    public MyBot(BotUserService userService, Button buttonService, UserStateManager userStateManager, UserStateTransport userStateTransport, UserStateRealEstate userStateRealEstate, BotConfig botConfig, MessageId allMessageIds) {
        this.userService = userService;
        this.button = buttonService;
        this.userStateManager = userStateManager;
        this.userStateTransport = userStateTransport;
        this.userStateRealEstate = userStateRealEstate;
        this.botConfig = botConfig;
        this.allMessageIds = allMessageIds;
    }

    @Autowired
    @Lazy
    public void setAdService(AdService adService) {
        this.adService = adService;
    }

    @Autowired
    @Lazy
    public void setTransportInterpreter(TransportInterpreter transportInterpreter) {
        this.transportInterpreter = transportInterpreter;
    }

    @Autowired
    @Lazy
    public void setRealEstateInterpreter(RealEstateInterpreter realEstateInterpreter) {
        this.realEstateInterpreter = realEstateInterpreter;
    }

    @Autowired
    @Lazy
    public void setTransportService(TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            int messageId = update.getMessage().getMessageId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String userName = update.getMessage().getFrom().getUserName();
            BotUser existingUser = userService.findByTelegramId(chatId);
            if (existingUser == null) {
                saveNewUser(chatId, firstName, lastName, userName);
            }
            switch (text) {
                case "/start" -> start(chatId,messageId);
                case ButtonConstant.myAds -> myAds(chatId, messageId);
                case ButtonConstant.myFavorite -> myFavorite(chatId, messageId);
                case ButtonConstant.back -> back(chatId);
                case ButtonConstant.placeAd -> placeAd(chatId, messageId);
                case ButtonConstant.searchAd -> searchAd(chatId, messageId);
                default -> {
                    Message message = sendMessageWithInlKeyboard(chatId, "Noto'g'ri xabar yubordingiz!", null);
                    deleteMessageLater(chatId, message.getMessageId(), 7);
                }
            }
            if (userStateManager.getUserMainStatus(chatId) != null &&
                    userStateManager.getUserMainStatus(chatId).equals(TRANSPORT) && !text.equals(ButtonConstant.searchAd) && !text.equals(ButtonConstant.placeAd)) {
                transportInterpreter.transportInterpreter(update);
            } else if (userStateManager.getUserMainStatus(chatId) != null &&
                    userStateManager.getUserMainStatus(chatId).equals(REAL_ESTATE) && !text.equals(ButtonConstant.searchAd) && !text.equals(ButtonConstant.placeAd)) {
                realEstateInterpreter.interpreter(update);
            }

        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            Long chatId = update.getMessage().getChatId();
            if (userStateManager.getUserMainStatus(chatId).equals(TRANSPORT)) {
                transportInterpreter.transportInterpreter(update);
            } else if (userStateManager.getUserMainStatus(chatId).equals(REAL_ESTATE)) {
                realEstateInterpreter.interpreter(update);
            }
        } else if (update.hasMessage() && update.getMessage().hasVideo()) {
            Long chatId = update.getMessage().getChatId();
            if (userStateManager.getUserMainStatus(chatId).equals(TRANSPORT)) {
                transportInterpreter.transportInterpreter(update);
            } else if (userStateManager.getUserMainStatus(chatId).equals(REAL_ESTATE)) {
                realEstateInterpreter.interpreter(update);
            }
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getFrom().getId();
            String callbackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            if (callbackData.equals(ButtonConstant.mainMenu)) {
                mainMenu(chatId, messageId);
            }
            if (userStateManager.getUserMainStatus(chatId) != null && userStateManager.getUserMainStatus(chatId).equals(TRANSPORT)) {
                transportInterpreter.transportInterpreter(update);
            }
            if (userStateManager.getUserMainStatus(chatId) != null && userStateManager.getUserMainStatus(chatId).equals(REAL_ESTATE)) {
                realEstateInterpreter.interpreter(update);
            }
            if (callbackData.equals(ButtonConstant.transport)) {
                InlineKeyboardMarkup newKeyboard = button.transMenu();
                if (userStateManager.getUserSubStatus(chatId).equals(PLACE_AD)) {
                    editMessage(chatId, messageId, "Qaysi transport uchun e'lon bermoqchisiz?", newKeyboard);
                } else {
                    editMessage(chatId, messageId, "Qanaqa transport vositasini qidiryapsiz?", newKeyboard);
                }
                userStateManager.setUserMainStatus(chatId, TRANSPORT);
            } else if (callbackData.equals(ButtonConstant.realEstate)) {
                InlineKeyboardMarkup newKeyboard = button.realEstateMenu();
                if (userStateManager.getUserSubStatus(chatId).equals(PLACE_AD)) {
                    editMessage(chatId, messageId, "Qanday ko'chmas mulk uchun e'lon bermoqchisiz?", newKeyboard);
                } else {
                    editMessage(chatId, messageId, "Qanday ko'chmas mulk qidiryapsiz?", newKeyboard);
                }
                userStateManager.setUserMainStatus(chatId, REAL_ESTATE);
            }
            if (callbackData.startsWith(ButtonConstant.deleteAd)) {
                transportService.deleteAd(chatId, callbackData, messageId);
            }
            if (callbackData.startsWith(ButtonConstant.deleteAdFromFavorite)) {
                transportService.deleteAdFromFavorite(chatId, callbackData, messageId);
            }
        }

    }

    private void placeAd(long chatId, int messageId) {
        deleteMessageLater(chatId, messageId, 1);
        clearOldMessages(chatId, allMessageIds.getIdOfAdsShown(), allMessageIds.getIdOfMyAds(), allMessageIds.getIdFavoriteAds(),
                allMessageIds.getIdOfButton(), allMessageIds.getIdSearchButton(), allMessageIds.getIdPlaceAdButton(), PLACE_AD);
        Message message = sendMessageWithInlKeyboard(chatId, MessageText.placeAdMessage, button.mainMenu());
        allMessageIds.setIdPlaceAdButton(message.getMessageId());
    }

    private void searchAd(long chatId, int messageId) {
        deleteMessageLater(chatId, messageId, 1);
        clearOldMessages(chatId, allMessageIds.getIdOfAdsShown(), allMessageIds.getIdOfMyAds(), allMessageIds.getIdFavoriteAds(),
                allMessageIds.getIdOfButton(), allMessageIds.getIdSearchButton(), allMessageIds.getIdPlaceAdButton(), SEARCH_AD);
        Message message = sendMessageWithInlKeyboard(chatId, MessageText.searchAdMessage, button.mainMenu());
        allMessageIds.setIdSearchButton(message.getMessageId());
    }

    private void myAds(long chatId, int messageId) {
        deleteMessageLater(chatId, messageId, 3);
        userStateManager.setUserSubStatus(chatId, null);
        userStateManager.setUserMainStatus(chatId, null);
        clearOldMessages(chatId, allMessageIds.getIdOfAdsShown(), allMessageIds.getIdOfMyAds(), allMessageIds.getIdFavoriteAds(),
                allMessageIds.getIdOfButton(), allMessageIds.getIdSearchButton(), allMessageIds.getIdPlaceAdButton(), null);
        adService.getMyAds(chatId);
    }

    private void myFavorite(long chatId, int messageId) {
        deleteMessageLater(chatId, messageId, 3);
        clearOldMessages(chatId, allMessageIds.getIdOfAdsShown(), allMessageIds.getIdOfMyAds(), allMessageIds.getIdFavoriteAds(),
                allMessageIds.getIdOfButton(), allMessageIds.getIdSearchButton(), allMessageIds.getIdPlaceAdButton(), null);
        adService.getMyFavorite(chatId);
    }

    private void start(long chatId, int messageId) {
        deleteMessageLater(chatId, messageId, 2);
        sendMessageWithReplyKeyboard(chatId, MessageText.startMessage, button.startMenu());
        setUserState(chatId, START, null, null, null);
    }

    private void back(long chatId) {
        sendMessageWithReplyKeyboard(chatId, "Asosiy menu", button.startMenu());
        setUserState(chatId, START, null, null, null);
    }
    private void mainMenu(long chatId, int messageId){
        InlineKeyboardMarkup newButton = button.mainMenu();
        editMessage(chatId, messageId, MessageText.placeAdMessage, newButton);
        userStateManager.setUserMainStatus(chatId, START);
        userStateTransport.setUserStatusTransport(chatId, null);
        setUserState(chatId,START,this.userStateManager.getUserSubStatus(chatId),null,null);
    }

    private void setUserState(long chatId, UserStateConstants userMainState, UserStateConstants userSubState,
                              UserStateConstants userStateTransport, UserStateConstants userStateRealEstate) {
        this.userStateManager.setUserMainStatus(chatId, userMainState);
        this.userStateTransport.setUserStatusTransport(chatId, userStateTransport);
        this.userStateRealEstate.setUserStatusRealEstate(chatId, userStateRealEstate);
        this.userStateManager.setUserSubStatus(chatId, userSubState);
    }

    private void clearOldMessages(long chatId, List<Integer> idOfAdsShown, List<Integer> idOfMyAds, List<Integer> idOfFavorite,
                                  int idOfButton, int idSearchButton, int idPlaceAdButton, UserStateConstants subStatus) {
        userStateManager.setUserSubStatus(chatId, subStatus);
        userStateTransport.setUserStatusTransport(chatId, null);
        userStateManager.setUserMainStatus(chatId, null);

        if (idOfAdsShown != null && !idOfAdsShown.isEmpty()) {
            for (Integer integer : idOfAdsShown) {
                deleteMessageLater(chatId, integer, 1);
            }
            idOfAdsShown.clear();
        }
        if (idOfMyAds != null && !idOfMyAds.isEmpty()) {
            for (Integer integer : idOfMyAds) {
                deleteMessageLater(chatId, integer, 1);
            }
            idOfMyAds.clear();
        }
        if (idOfFavorite != null && !idOfFavorite.isEmpty()) {
            for (Integer integer : idOfFavorite) {
                deleteMessageLater(chatId, integer, 1);
            }
            idOfFavorite.clear();
        }
        if (idOfButton != 0) {
            deleteMessageLater(chatId, idOfButton, 1);
            allMessageIds.setIdOfButton(0);
        }
        if (idSearchButton != 0) {
            deleteMessageLater(chatId, idSearchButton, 1);
            allMessageIds.setIdSearchButton(0);
        }
        if (idPlaceAdButton != 0) {
            deleteMessageLater(chatId, idPlaceAdButton, 1);
            allMessageIds.setIdPlaceAdButton(0);
        }
    }


    private void saveNewUser(Long chatId, String firstName, String lastName, String userName) {
        BotUser newUser = new BotUser();
        newUser.setTelegramId(chatId);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setUsername(userName);
        userService.save(newUser);
    }

    @Override
    public void editImageMessage(Long chatId, Integer messageId, String captionText, String imageUrl, String videoUrl, InlineKeyboardMarkup newKeyboard) {
        if (imageUrl != null || videoUrl != null) {
            EditMessageMedia editMessageMedia = new EditMessageMedia();

            if (imageUrl != null) {
                InputMediaPhoto photoMedia = new InputMediaPhoto(imageUrl);
                photoMedia.setCaption(captionText);
                editMessageMedia.setMedia(photoMedia);
            } else {
                InputMediaVideo videoMedia = new InputMediaVideo(videoUrl);
                videoMedia.setCaption(captionText);
                editMessageMedia.setMedia(videoMedia);
            }

            editMessageMedia.setChatId(chatId);
            editMessageMedia.setMessageId(messageId);
            editMessageMedia.setReplyMarkup(newKeyboard);

            try {
                execute(editMessageMedia);
            } catch (TelegramApiException e) {
                if (e.getMessage().contains("message is not modified")) {
                    throw new RuntimeException("Versuch, eine Nachricht ohne tatsächliche Änderungen zu aktualisieren.");
                } else {
                    e.printStackTrace();
                }
            }

        } else {
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setReplyMarkup(newKeyboard);
            editMessageReplyMarkup.setChatId(chatId);
            editMessageReplyMarkup.setMessageId(messageId);

            try {
                execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                if (e.getMessage().contains("message is not modified")) {
                    throw new RuntimeException("Versuch, eine Nachricht ohne tatsächliche Änderungen zu aktualisieren.");
                } else {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void editKeyboard(long chatId, int messageId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(messageId);

        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("message is not modified")) {
                throw new RuntimeException("Versuch, eine Nachricht ohne tatsächliche Änderungen zu aktualisieren.");
            } else {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup newKeyboard) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setMessageId(messageId);
        newMessage.setChatId(chatId);
        newMessage.setText(text);
        if (newKeyboard != null) {
            newMessage.setReplyMarkup(newKeyboard);
        }

        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("message is not modified")) {
                throw new RuntimeException("Versuch, eine Nachricht ohne tatsächliche Änderungen zu aktualisieren.");
            } else {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Message sendMessageWithInlKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(keyboard);
        try {
            return execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to user: {}", chatId, e);
            return null;
        }
    }

    @Override
    public Message sendMessageWithReplyKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(keyboard);
        try {
            return execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to user: {}", chatId, e);
            return null;
        }
    }

    @Override
    public Integer sendPhotoWithInlKeyboard(Long chatId, String text, String photoUrl, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        InputFile inputFile = new InputFile(photoUrl);
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setCaption(text);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        try {
            Message response = execute(sendPhoto);
            return response.getMessageId();
        } catch (TelegramApiException e) {
            log.error("Fehler beim Senden des Fotos: ", e);
            return null;
        }

    }

    @Override
    public Integer sendVideoWithInlKeyboard(Long chatId, String text, String videoUrl, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendVideo sendVideo = new SendVideo();
        InputFile inputFile = new InputFile(videoUrl);
        sendVideo.setChatId(chatId);
        sendVideo.setCaption(text);
        sendVideo.setVideo(inputFile);
        sendVideo.setReplyMarkup(inlineKeyboardMarkup);
        try {
            Message response = execute(sendVideo);
            return response.getMessageId();
        } catch (TelegramApiException e) {
            log.error("Fehler beim Senden des Videos: ", e);
            return null;
        }
    }

    @Override
    public void deleteMessageLater(Long chatId, Integer messageId, long delayInSeconds) {
        if (messageId != null) {
            Runnable task = () -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(String.valueOf(chatId));
                deleteMessage.setMessageId(messageId);
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    log.error("Error by delete Message: " + e);
                }
            };
            scheduler.schedule(task, delayInSeconds, TimeUnit.SECONDS);
        } else {
            log.error("Id of Message is null: " + null);
        }
    }

    @Override
    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);

        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "@AutoSpotterBot";
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

}
