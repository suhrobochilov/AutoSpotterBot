package car.autoSpotterBot.state.transState;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class UserStateAuto {
    private static final UserStateAuto instance = new UserStateAuto();
    private final Map<Long, UserStateInAuto> userStateMap = new HashMap<>();

    private UserStateAuto() {
    }

    public static UserStateAuto getInstance() {
        return instance;
    }

    public UserStateInAuto getUserState(long userId) {
        return userStateMap.get(userId);
    }

    public void setUserState(long userId, UserStateInAuto state) {
        userStateMap.put(userId, state);
    }
}
