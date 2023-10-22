package car.autoSpotterBot.autoUtil;

import org.springframework.stereotype.Service;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
@Service
public class UserStateManager {
    private static final UserStateManager instance = new UserStateManager();
    private final Map<Long, UserStateInAuto> userStateMap = new HashMap<>();

    private UserStateManager() {}

    public static UserStateManager getInstance() {
        return instance;
    }

    public UserStateInAuto getUserState(long userId) {
        return userStateMap.get(userId);
    }

    public void setUserState(long userId, UserStateInAuto state) {
        userStateMap.put(userId, state);
    }

    /**
     * Setzt den Zustand des Benutzers mit der gegebenen chatId zurück.
     *
     * @param chatId Die ID des Chats/Benutzers, dessen Zustand zurückgesetzt werden soll.
     */
    public void resetUserState(Long chatId) {
        if(userStateMap.containsKey(chatId)) {
            userStateMap.remove(chatId);
        }
        // Optional: Eine Standardbenutzerzustand hinzufügen oder andere Aktionen ausführen, wenn der Zustand zurückgesetzt wird
        // userStates.put(chatId, new UserState());
    }
}

