
package car.autoSpotterBot;

import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.service.AdService;
import car.autoSpotterBot.service.BotUserService;
import car.autoSpotterBot.service.StadtService;
import car.autoSpotterBot.autoUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static car.autoSpotterBot.autoUtil.UserStateInAuto.*;

@Component
public class MyBot extends TelegramLongPollingBot implements BotCallback {
    private static final Logger log = LoggerFactory.getLogger(MyBot.class);
    private final Button button;
    private final StadtService stadtService;

    private final BotUserService userService;
    private final Map<Long, Ad> currentAdsMap = new HashMap<>();
    private final Map<Long, UserStateInAuto> userStateMap = new HashMap<>();
    private final AdService adService;
    private final AutoInterpreter autoInterpreter;
    private final UserStateManager userStateManager;
    private Map<Long, Integer> messageIdMap = new ConcurrentHashMap<>();
    @Value("${bot.token}")
    private String botToken;

    public MyBot(BotUserService userService, Button buttonService, StadtService stadtService, AdService adService, UserStateManager userStateManager) {
        this.userService = userService;
        this.button = buttonService;
        this.stadtService = stadtService;
        this.adService = adService;
        this.autoInterpreter = new AutoInterpreter(this, userService, buttonService, stadtService, adService, userStateManager);
        this.userStateManager = userStateManager;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
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
            if (text.equals("/start")) {

                mainMenu(chatId);
                userStateManager.setUserState(chatId, START);
            } else if (text.equals(ButtonConstants.auto) || text.equals(ButtonConstants.placeAutoAd) || text.equals(ButtonConstants.autoSearch)
                    || text.equals(ButtonConstants.backInAutoAd)) {
                autoInterpreter.autoInterpret(chatId, text, null);
            }else if (text.equals(ButtonConstants.immobile) || text.equals(ButtonConstants.foods) || text.equals(ButtonConstants.service)){
                sendMessageWithInlKeyboard(chatId,"Bu funksiya hali tayyor emas",null);
            }

        } else if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getFrom().getId();
            String callBackQuery = update.getCallbackQuery().getData();
            Integer messageId = retrieveStoredMessageId(chatId); // Holen Sie sich die gespeicherte messageId

            autoInterpreter.autoInterpret(chatId, callBackQuery, null);
            if (userStateManager.getUserState(chatId).equals(PLACE_AD_FOR_AUTO)) {
                autoInterpreter.autoInterpret(chatId, callBackQuery, null);
            } else if (userStateManager.getUserState(chatId).equals(SEARCH_AD_FOR_AUTO)) {
                autoInterpreter.autoInterpret(chatId, callBackQuery, null);
            }
        } else if (update.getMessage().hasPhoto()) {
            List<PhotoSize> photoSizes = update.getMessage().getPhoto();
            Long chatId = update.getMessage().getFrom().getId();
            PhotoSize largestPhoto = photoSizes.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
            String photoUrl = largestPhoto.getFileId();
            String caption = update.getMessage().getCaption();
            if (userStateManager.getUserState(chatId).equals(SENDING_PHOTO_FOR_AUTO)) {
                autoInterpreter.autoInterpret(chatId, caption, photoUrl);
            }else {
                sendMessageWithInlKeyboard(chatId,"Oldin kerakli bo'limni tanlab keyin rasm yuboring ",null);
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
    public void sendMessageWithInlKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.getMessageThreadId();
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
    public void deleteMessage ( Long chatId, Integer messageId){
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
        return botToken;
    }

}
