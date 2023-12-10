package car.autoSpotterBot.state;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserStateTransport {
    public UserStateTransport() {
    }

    private final Map<Long, UserStateConstants> userStatusTransport = new HashMap<>();
    public UserStateConstants getUserStatusTransport(long userId) {
        return userStatusTransport.get(userId);
    }
    public void setUserStatusTransport(long userId, UserStateConstants state) {
        userStatusTransport.put(userId, state);
    }

}
