package car.autoSpotterBot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageId {
    private static final Logger log = LoggerFactory.getLogger(MessageId.class);
    private final Map<Long, Map<MessageType, List<Integer>>> messageIds = new HashMap<>();
    private final List<MessageType> keyWordForMessageIds = new ArrayList<>();

    public void setMessageId(Long chatId, MessageType type, Integer messageId) {
        Map<MessageType, List<Integer>> messageTypeMap = messageIds.computeIfAbsent(chatId, k -> new HashMap<>());
        List<Integer> messageIdList = messageTypeMap.computeIfAbsent(type, k -> new ArrayList<>());
        messageIdList.add(messageId);
        log.info("Added messageId {} for chatId {} and MessageType {}", messageId, chatId, type);
    }

    public List<Integer> getMessageIds(Long chatId, MessageType type) {
        Map<MessageType, List<Integer>> chatMessageIds = messageIds.get(chatId);
        if (chatMessageIds != null) {
            return chatMessageIds.getOrDefault(type, new ArrayList<>());
        }
        return new ArrayList<>();
    }

    public void clearMessageIds(Long chatId, MessageType type) {
        Map<MessageType, List<Integer>> chatMessageTypes = messageIds.get(chatId);
        if (chatMessageTypes != null) {
            chatMessageTypes.remove(type);
            log.info("Cleared messageId for chatId {} and MessageType {}", chatId, type);
        }
    }

    public List<MessageType> getKeyWordForMessageIds() {
        if (keyWordForMessageIds.isEmpty()) {
            keyWordForMessageIds.add(MessageType.ID_OF_MY_ADS);
            keyWordForMessageIds.add(MessageType.ID_OF_SEARCHED_ADS);
            keyWordForMessageIds.add(MessageType.ID_OF_SEARCH_BUTTON);
            keyWordForMessageIds.add(MessageType.ID_OF_FAVORITED_ADS);
            keyWordForMessageIds.add(MessageType.ID_OF_PLACE_AD_BUTTON);
        }
        return keyWordForMessageIds;
    }
}
