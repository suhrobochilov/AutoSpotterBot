package car.autoSpotterBot.state;

import org.springframework.stereotype.Service;

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
}

