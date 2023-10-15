
package car.autoSpotterBot;

import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Stadt;
import car.autoSpotterBot.service.AdService;
import car.autoSpotterBot.service.BotUserService;
import car.autoSpotterBot.service.StadtService;
import car.autoSpotterBot.util.Button;
import car.autoSpotterBot.util.ButtonConstants;
import car.autoSpotterBot.util.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import static car.autoSpotterBot.util.State.*;

@Component
public class MyBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(MyBot.class);
    private final Button buttonService;
    private final StadtService stadtService;

    private final BotUserService userService;
    private final Map<Long, Ad> currentAdsMap = new HashMap<>();
    private final Map<Long, State> userStateMap = new HashMap<>();
    private final AdService adService;
    Ad ad = new Ad();


    public MyBot(BotUserService userService, Button buttonService, StadtService stadtService, AdService adService) {
        this.userService = userService;
        this.buttonService = buttonService;
        this.stadtService = stadtService;
        this.adService = adService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String userName = update.getMessage().getFrom().getUserName();

            Ad currentAd = currentAdsMap.get(chatId);

            if ("/start".equals(text)) {
                BotUser existingUser = userService.findByTelegramId(chatId);

                if (existingUser == null) {
                    BotUser newUser = new BotUser();
                    newUser.setTelegramId(chatId);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setUsername(userName);
                    userService.save(newUser);
                    sendMessageToUser("Assalomu Alaykum " + firstName + " " + lastName + " Botga Xush kelibsiz", chatId);
                }
                userStateMap.put(chatId, State.START);
                sendMainMenu(chatId);
            } else if (text.equals(ButtonConstants.placeAd)) {
                sendInlineKeyboardCites(chatId);
                userStateMap.put(chatId, State.PLACEaD);
                BotUser user = userService.findByTelegramId(chatId);
                ad.setUser(user);
                currentAdsMap.put(chatId, ad);

            } else if (text.equals(ButtonConstants.search)) {
                sendMessageToUser("Funksiya hali tayyor emas", chatId);
            } else if (text.equals(ButtonConstants.favorite)) {
                sendMessageToUser("Funksiya hali toyyor emas", chatId);
            }
            State currentState = userStateMap.get(chatId);
            switch (currentState) {
                case ASKING_FOR_MODEL -> {
                    currentAd.setModel(text);
                    sendMessageToUser("Ishlab chiqarilgan yilini kiriting", chatId);
                    userStateMap.put(chatId, ASKING_FOR_BUILDYEAR);
                }
                case ASKING_FOR_BUILDYEAR -> {
                    try {
                        int buildYear = Integer.parseInt(text);
                        currentAd.setBuildYear(buildYear);
                        sendMessageToUser("Avtomobil rangini kiriting", chatId);
                        userStateMap.put(chatId, ASKING_FOR_COLOR);
                    } catch (NumberFormatException e) {
                        sendMessageToUser("Ishlab chiqarilgan yili to'g'ri kiriting", chatId);
                    }
                }
                case ASKING_FOR_COLOR -> {
                    currentAd.setColor(text);
                    sendMessageToUser("Yurgan masofasini kiriting", chatId);
                    userStateMap.put(chatId, ASKING_FOR_MILEAGE);
                }

                case ASKING_FOR_MILEAGE -> {
                    try {
                        int mileage = Integer.parseInt(text);
                        currentAd.setMileage(mileage);
                        sendMessageToUser("Narxi qancha", chatId);
                        userStateMap.put(chatId, ASKING_FOR_PRICE);
                    } catch (NumberFormatException e) {
                        sendMessageToUser("Kilometrni to'g'ri kiriting", chatId);
                    }
                }
                case ASKING_FOR_PRICE -> {
                    currentAd.setPrice(text);
                    sendMessageToUser("Qo'shimcha ma'lumot qoldiring", chatId);
                    userStateMap.put(chatId, State.ASKING_FOR_DESCRIPTION);
                }
                case ASKING_FOR_DESCRIPTION -> {
                    currentAd.setDescription(text);
                    sendMessageToUser("Avtomobil rasmini yuklang", chatId);
                    userStateMap.put(chatId, ASKING_FOR_IMAGE);

                }

                // ... Ihre anderen Fälle ...
            }


        } else if (update.hasCallbackQuery()) {
            String callBackQuery = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getFrom().getId();
            State currentState = userStateMap.get(chatId);
            Ad currentAd = currentAdsMap.get(chatId);
            if (currentState == State.PLACEaD) {
                Stadt stadt = stadtService.saveCity(callBackQuery);
                currentAd.setStandort(stadt);
                sendInlineKeyboardBrend(chatId);
                userStateMap.put(chatId, State.ASKING_FOR_BRAND);
            }
            if (currentState == State.ASKING_FOR_BRAND) {
                currentAd.setManufacturer(callBackQuery);
                sendMessageToUser("Avtomobil modelini kiriting (Nexia, Spark, Damas", chatId);
                userStateMap.put(chatId, State.ASKING_FOR_MODEL);
            }

        } else if (update.getMessage().hasPhoto()) {

            Long chatId = update.getMessage().getChatId();
            List<PhotoSize> photos = update.getMessage().getPhoto();
            PhotoSize photo = photos.get(photos.size() - 1); // Das größte Foto
            String fileId = photo.getFileId();
            Ad currentAd = currentAdsMap.get(chatId);
            State currentState = userStateMap.get(chatId);
            switch (currentState) {
                case ASKING_FOR_IMAGE -> {
                    currentAd.setImageUrl(fileId);
                    adService.save(ad);
                    sendMessageToUser("E'lon joylashtirildi", chatId);
                    userStateMap.remove(chatId);
                    currentAdsMap.remove(chatId);
                }

            }
        }

    }

    private void sendPhoto(Long chatId, String photoUrl, String caption) {

        SendPhoto sendPhoto = new SendPhoto();
        InputFile inputFile = new InputFile(photoUrl);

        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setCaption(caption);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessageToUser(String text, Long userId) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(String.valueOf(userId));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to user: {}", userId, e);
        }
    }

    private void sendInlineKeyboardCites(long chatId) {
        List<String> bundeslanderRow1 = Arrays.asList("Toshkent", "Andijon", "Buxoro");
        List<String> bundeslanderRow2 = Arrays.asList("Farg'ona", "Jizzax", "Sirdaryo");
        List<String> bundeslanderRow3 = Arrays.asList("Namangan", "Samarqand", "Xorazm");
        List<String> bundeslanderRow4 = Arrays.asList("Qashqadaryo", "Surxandaryo");
        List<String> bundeslanderRow5 = Arrays.asList("Qoraqalpoqiston", "Navoi");

        InlineKeyboardMarkup inlineKeyboard = buttonService.buildInlineKeyboard(chatId, "Viloyatni tanlang:", bundeslanderRow1, bundeslanderRow2,
                bundeslanderRow3, bundeslanderRow4, bundeslanderRow5);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Viloyatni tanlang");
        message.setReplyMarkup(inlineKeyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send main menu to user: {}", chatId, e);
        }
    }

    private void sendInlineKeyboardBrend(long chatId) {
        List<String> bundeslanderRow1 = Arrays.asList("Chevrolet", "Kia", "Hyundai");
        List<String> bundeslanderRow2 = Arrays.asList("Ford", "BYD", "CHangan");
        List<String> bundeslanderRow3 = Arrays.asList("Tesla", "Honda", "BMW");
        List<String> bundeslanderRow4 = Arrays.asList("Mercedes-Benz", "Lada");
        List<String> bundeslanderRow5 = Arrays.asList("VAZ", "Land Rover");

        InlineKeyboardMarkup inlineKeyboard = buttonService.buildInlineKeyboard(chatId, "Avtomobil markasini tanlang", bundeslanderRow1, bundeslanderRow2,
                bundeslanderRow3, bundeslanderRow4, bundeslanderRow5);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Avtomobil markasini tanlang");
        message.setReplyMarkup(inlineKeyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send main menu to user: {}", chatId, e);
        }
    }

    private void sendMainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Quyidagilardan birini tanlang!");
        message.setReplyMarkup(buttonService.mainMenu());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send main menu to user: {}", chatId, e);
        }
    }

    private void askForBundesland(long chatId, List<String> bundeslander) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Bitte wählen Sie ein Bundesland:");
        message.setReplyMarkup(buttonService.bundeslanderMenu(bundeslander));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to ask for Bundesland: {}", chatId, e);
        }
    }

    @Override
    public String getBotUsername() {
        return "@AutoSpotterBot";
    }

    @Override
    public String getBotToken() {
        // Sie sollten diesen Token niemals veröffentlichen oder teilen. Es ist nur zu Demonstrationszwecken hier.
        return "6693677530:AAG79gw4s2MbKPYkOvYXmFETAncBEkX_l7I";
    }
}
