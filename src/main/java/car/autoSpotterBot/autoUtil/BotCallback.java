package car.autoSpotterBot.autoUtil;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface BotCallback {

    void editImageMessage(Long chatId, Integer messageId, String captionText, String imageUrl, String videoUrl, InlineKeyboardMarkup newKeyboard);

    Message sendMessageWithInlKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard);

    void sendMessageWithReplyKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard);

    void sendInlineKeyboardCites(Long chatId);

    void sendPhotoWithInlKeyboard(Long chatid, String text, String photoUrl, InlineKeyboardMarkup inlineKeyboardMarkup);

    void sendInKeyboardForSearch(Long chatId);

    void deleteMessageLater(Long chatId, Integer messageId, long delayInSeconds);
}
