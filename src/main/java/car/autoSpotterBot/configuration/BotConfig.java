package car.autoSpotterBot.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class BotConfig {
    @Value("${bot.token}")
    private String botToken;

    public String getBotToken() {
        return botToken;
    }
}

