package car.autoSpotterBot.state.transState;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserStateTrans {
    private static final UserStateTrans instance = new UserStateTrans();
    private final Map<Long, UserStateInAuto> userStateMap = new HashMap<>();

    private UserStateTrans() {
    }

    public static UserStateTrans getInstance() {
        return instance;
    }

    public UserStateInAuto getUserState(long userId) {
        return userStateMap.get(userId);
    }

    public void setUserState(long userId, UserStateInAuto state) {
        userStateMap.put(userId, state);
    }

}
