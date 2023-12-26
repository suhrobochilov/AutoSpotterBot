package car.autoSpotterBot.state;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
public class UserStateRealEstate {
    public UserStateRealEstate() {
    }
    private final Map<Long, UserStateConstants> userStatusRealEstate = new HashMap<>();
    private final Map<Long, UserStateConstants> userStateAuto = new HashMap<>();
    public UserStateConstants getUserStatusRealEstate(long userId) {
        return userStatusRealEstate.get(userId);
    }
    public void setUserStatusRealEstate(long userId, UserStateConstants state) {
        userStatusRealEstate.put(userId, state);
    }
    public UserStateConstants getUserStateAuto(long userId) {
        return userStateAuto.get(userId);
    }
    public void setUserStateAuto(long userId, UserStateConstants state) {
        userStateAuto.put(userId, state);
    }
}
