package car.autoSpotterBot.autoUtil;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Service
@Controller
public class Button {

    public InlineKeyboardMarkup buildInlKeyboardForCities(long userId, List<String> buttons1, List<String> buttons2,
                                                          List<String> buttons3, List<String> buttons4, List<String> buttons5) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        inlineButton(buttons1, inlineButtons);
        inlineButton(buttons2, inlineButtons);
        inlineButton(buttons3, inlineButtons);
        inlineButton(buttons4, inlineButtons);
        inlineButton(buttons5, inlineButtons);
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    private void inlineButton(List<String> buttons1, List<List<InlineKeyboardButton>> inlineButtons) {
        List<InlineKeyboardButton> inlineKeyboardButtonsRow1 = new ArrayList<>();
        for (String button : buttons1) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(button);
            inlineKeyboardButton.setCallbackData(button);
            inlineKeyboardButtonsRow1.add(inlineKeyboardButton);
        }
        inlineButtons.add(inlineKeyboardButtonsRow1);
    }

    public ReplyKeyboardMarkup mainMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        replyKeyboardMarkup.setResizeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        row1.add(new KeyboardButton("Avtotransport"));
        row1.add(new KeyboardButton("Uy-joy"));

        row2.add(new KeyboardButton("Foods"));
        row2.add(new KeyboardButton("Maishiy xizmat"));

        keyboard.add(row1);
        keyboard.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
    public InlineKeyboardMarkup inlKeyboardConfirmation(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton morePhotosBtn = new InlineKeyboardButton();
        InlineKeyboardButton favoriteBtn = new InlineKeyboardButton();
        morePhotosBtn.setText("Tasdiqlash");
        morePhotosBtn.setCallbackData("Tasdiqlash");
        favoriteBtn.setText("Bekor qilish");
        favoriteBtn.setCallbackData("Bekor qilish");

        row.add(morePhotosBtn);
        row.add(favoriteBtn);
        rows.add(row);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
    public InlineKeyboardMarkup inlKeyboardForAd(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton morePhotosBtn = new InlineKeyboardButton();
        InlineKeyboardButton favoriteBtn = new InlineKeyboardButton();
        morePhotosBtn.setText("Hamma rasmlar");
        morePhotosBtn.setCallbackData("morePhotos");
        favoriteBtn.setText("Favoritga qo'shish");
        favoriteBtn.setCallbackData("favorite");

        row.add(morePhotosBtn);
        row.add(favoriteBtn);
        rows.add(row);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup autoMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        replyKeyboardMarkup.setResizeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        row1.add(new KeyboardButton("Avto-E'lon joylash \uD83D\uDE99"));
        row1.add(new KeyboardButton("Mening Avto e'lonlarim \uD83D\uDCB5\uD83D\uDE98"));

        row2.add(new KeyboardButton("Qidirish \uD83D\uDD0E"));
        row2.add(new KeyboardButton("Favorit ❤\uFE0F"));
        row3.add(new KeyboardButton("Ortga ⬅\uFE0F"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }


    public ReplyKeyboardMarkup bundeslanderMenu(List<String> bundeslander) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String bundesland : bundeslander) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(bundesland));
            keyboard.add(row);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup buildInlKeyboardForSearch(Long chatId, List<String> citiesRow1, List<String> citiesRow2, List<String> citiesRow3,
                                                          List<String> citiesRow4, List<String> citiesRow5, List<String> allAutoAd) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

        inlineButton(citiesRow1, inlineButtons);
        inlineButton(citiesRow2, inlineButtons);
        inlineButton(citiesRow3, inlineButtons);
        inlineButton(citiesRow4, inlineButtons);
        inlineButton(citiesRow5, inlineButtons);
        inlineButton(allAutoAd, inlineButtons);

        inlineKeyboardMarkup.setKeyboard(inlineButtons);

        return inlineKeyboardMarkup;
    }
    public List<List<String>> getCities() {
        List<String> bundeslanderRow1 = Arrays.asList("Toshkent", "Andijon", "Buxoro");
        List<String> bundeslanderRow2 = Arrays.asList("Farg'ona", "Jizzax", "Sirdaryo");
        List<String> bundeslanderRow3 = Arrays.asList("Namangan", "Samarqand", "Xorazm");
        List<String> bundeslanderRow4 = Arrays.asList("Qashqadaryo", "Surxandaryo");
        List<String> bundeslanderRow5 = Arrays.asList("Qoraqalpog'iston", "Navoi");

        return Arrays.asList(bundeslanderRow1, bundeslanderRow2, bundeslanderRow3, bundeslanderRow4, bundeslanderRow5);
    }

}