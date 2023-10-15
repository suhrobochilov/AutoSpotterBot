package car.autoSpotterBot.util;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Component
@Service
@Controller
public class Button {

    public InlineKeyboardMarkup buildInlineKeyboard(long userId, String text, List<String> buttons1, List<String> buttons2,
                                                    List<String> buttons3,List<String> buttons4,List<String> buttons5) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setText(text);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

        // Add first row of buttons
        inlineButton(buttons1, inlineButtons);

        // Add second row of buttons
        inlineButton(buttons2, inlineButtons);
        inlineButton(buttons3, inlineButtons);
        inlineButton(buttons4, inlineButtons);
        inlineButton(buttons5, inlineButtons);

        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

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

        row1.add(new KeyboardButton("E'lon joylash"));
        row1.add(new KeyboardButton("Qidiruv"));

        row2.add(new KeyboardButton("Mening E'lonlarim"));
        row2.add(new KeyboardButton("Favorit"));

        keyboard.add(row1);
        keyboard.add(row2);

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


}