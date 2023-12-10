package car.autoSpotterBot.state;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
public class UserStateManager {
    private final Map<Long, UserStateConstants> userMainStatus = new HashMap<>();
    private final Map<Long, UserStateConstants> userSubStatus = new HashMap<>();

    private UserStateManager() {}
    public UserStateConstants getUserMainStatus(long userId) {
        return userMainStatus.get(userId);
    }
    public UserStateConstants getUserSubStatus(long userId) {
        return userSubStatus.get(userId);
    }


    public void setUserMainStatus(long userId, UserStateConstants state) {
        userMainStatus.put(userId, state);
    }
    public void setUserSubStatus(long userId, UserStateConstants state) {
        userSubStatus.put(userId, state);
    }

}

