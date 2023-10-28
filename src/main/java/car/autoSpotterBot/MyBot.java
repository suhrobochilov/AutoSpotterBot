package car.autoSpotterBot;

import car.autoSpotterBot.autoUtil.AutoInterpreter;
import car.autoSpotterBot.autoUtil.BotCallback;
import car.autoSpotterBot.autoUtil.UserStateManager;
import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstants;
import car.autoSpotterBot.configuration.BotConfig;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.service.BotUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static car.autoSpotterBot.autoUtil.UserStateInAuto.*;

@Component
public class MyBot extends TelegramLongPollingBot implements BotCallback {
    private static final Logger log = LoggerFactory.getLogger(MyBot.class);
    private final Button button;
    private final BotUserService userService;
    private final UserStateManager userStateManager;
    private final BotConfig botConfig;
    private final Map<Long, Integer> messageIdMap = new ConcurrentHashMap<>();
    private AutoInterpreter autoInterpreter;

    public MyBot(BotUserService userService, Button buttonService, UserStateManager userStateManager, BotConfig botConfig) {
        this.userService = userService;
        this.button = buttonService;
        this.userStateManager = userStateManager;
        this.botConfig = botConfig;
    }

    @Autowired
    @Lazy
    public void setAutoInterpreter(AutoInterpreter autoInterpreter) {
        this.autoInterpreter = autoInterpreter;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Integer messageId = update.getMessage().getMessageId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String userName = update.getMessage().getFrom().getUserName();
            BotUser existingUser = userService.findByTelegramId(chatId);
            if (existingUser == null) {
                BotUser newUser = new BotUser();
                newUser.setTelegramId(chatId);
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setUsername(userName);
                userService.save(newUser);
            }
            switch (text) {

                case "/start" -> {
                    mainMenu(chatId);
                    userStateManager.setUserState(chatId, START);
                }
                case ButtonConstants.auto, ButtonConstants.placeAutoAd, ButtonConstants.autoSearch, ButtonConstants.backInAutoAd, ButtonConstants.mayAutoAds,
                        ButtonConstants.nextPage, ButtonConstants.previousPage, ButtonConstants.autoFavorite ->
                        autoInterpreter.autoInterpret(chatId, text, null, null, messageId);
                case ButtonConstants.immobile, ButtonConstants.foods, ButtonConstants.service ->
                        sendMessageWithInlKeyboard(chatId, "Bu funksiya hali tayyor emas", null);
            }

        } else if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getFrom().getId();
            String callBackQuery = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            autoInterpreter.autoInterpret(chatId, callBackQuery, null, null, messageId);

        } else if (update.getMessage().hasPhoto()) {
            List<PhotoSize> photoSizes = update.getMessage().getPhoto();
            Long chatId = update.getMessage().getFrom().getId();
            PhotoSize largestPhoto = photoSizes.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
            String photoUrl = largestPhoto.getFileId();
            String caption = update.getMessage().getCaption();
            Integer messageId = update.getMessage().getMessageId();
            if (userStateManager.getUserState(chatId).equals(SENDING_PHOTO_FOR_AUTO) || userStateManager.getUserState(chatId).equals(WAITING_FOR_CONFIRMATION)) {
                autoInterpreter.autoInterpret(chatId, caption, photoUrl, null, messageId);
            }
        } else if (update.getMessage().hasVideo()) {
            Long chatId = update.getMessage().getFrom().getId();
            Video video = update.getMessage().getVideo();
            String videoUrl = video.getFileId();
            Integer messageId = update.getMessage().getMessageId();
            if (userStateManager.getUserState(chatId).equals(SENDING_PHOTO_FOR_AUTO) || userStateManager.getUserState(chatId).equals(WAITING_FOR_CONFIRMATION)) {
                autoInterpreter.autoInterpret(chatId, null, null, videoUrl, messageId);
            }
        }

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
    public void mainMenu(long chatId) {
        sendMessageWithReplyKeyboard(chatId, "Assalomu Alaykum Botga xush kelibsiz \n" +
                "Quyidagilardan birini tanlang!", button.mainMenu());
    }

    @Override
    public void menu(long chatId) {
        sendMessageWithReplyKeyboard(chatId, "Bo'limni tanlang!", button.mainMenu());
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
    public void sendMessageWithReplyKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to user: {}", chatId, e);
        }
    }

    @Override
    public void sendAutoMenu(Long chatId) {
        sendMessageWithReplyKeyboard(chatId, "Quyidagilardan birini tanlang!", button.autoMenu());
    }

    @Override
    public void sendInlineKeyboardCites(Long chatId) {
        Button button = new Button();
        List<List<String>> cities = button.getCities();

        InlineKeyboardMarkup inlineKeyboard = this.button.buildInlKeyboardForCities(chatId, cities.get(0), cities.get(1),
                cities.get(2), cities.get(3), cities.get(4));

        sendMessageWithInlKeyboard(chatId, "Hududni tanlang", inlineKeyboard);
    }

    @Override
    public void sendPhotoWithInlKeyboard(Long chatId, String text, String photoUrl, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        InputFile inputFile = new InputFile(photoUrl);
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setCaption(text);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendInKeyboardForSearch(Long chatId) {
        Button button = new Button();
        List<List<String>> cities = button.getCities();
        List<String> allAutoAd = List.of("Hammasini ko'rsatish");
        InlineKeyboardMarkup inlineKeyboard = this.button.buildInlKeyboardForSearch(chatId, cities.get(0), cities.get(1),
                cities.get(2), cities.get(3), cities.get(4), allAutoAd);
        sendMessageWithInlKeyboard(chatId, "Viloyatni tanlang", inlineKeyboard);

    }

    public void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);

        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to delete message with ID: {} in chat: {}", messageId, chatId, e);
        }
    }

    private void storeSentMessageId(Long chatId, Integer messageId) {
        messageIdMap.put(chatId, messageId);
    }

    private Integer retrieveStoredMessageId(Long chatId) {
        return messageIdMap.getOrDefault(chatId, null);
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
