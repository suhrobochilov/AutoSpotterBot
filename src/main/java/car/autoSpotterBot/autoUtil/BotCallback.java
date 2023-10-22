package car.autoSpotterBot.autoUtil;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface BotCallback {

    void mainMenu(long chatId);

    void menu(long chatId);

    void sendMessageWithInlKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard);

    void sendMessageWithReplyKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard);

    void sendAutoMenu(Long chatId);

    void sendInlineKeyboardCites(Long chatId);

    void sendPhotoWithInlKeyboard(Long chatid, String text, String photoUrl, InlineKeyboardMarkup inlineKeyboardMarkup);

    void sendInKeyboardForSearch(Long chatId);
}
