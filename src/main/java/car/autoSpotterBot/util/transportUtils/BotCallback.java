package car.autoSpotterBot.util.transportUtils;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface BotCallback {

    void editImageMessage(Long chatId, Integer messageId, String captionText, String imageUrl, String videoUrl, InlineKeyboardMarkup newKeyboard);

    void editKeyboard(long chatId, int messageId, String text, InlineKeyboardMarkup inlineKeyboardMarkup);

    void editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup newKeyboard);

    Message sendMessageWithInlKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard);

    Message sendMessageWithReplyKeyboard(Long chatId, String text, ReplyKeyboardMarkup keyboard);

    void sendPhotoWithInlKeyboard(Long chatid, String text, String photoUrl, InlineKeyboardMarkup inlineKeyboardMarkup);

    void sendVideoWithInlKeyboard(Long chatId, String text, String videoUrl, InlineKeyboardMarkup inlineKeyboardMarkup);

    void deleteMessageLater(Long chatId, Integer messageId, long delayInSeconds);

    void deleteMessage(long chatId, int messageId);

}
