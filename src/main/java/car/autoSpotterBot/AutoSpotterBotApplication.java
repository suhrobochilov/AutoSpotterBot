package car.autoSpotterBot;

import car.autoSpotterBot.autoUtil.UserStateManager;
import car.autoSpotterBot.configuration.BotConfig;
import car.autoSpotterBot.service.AdService;
import car.autoSpotterBot.service.BotUserService;
import car.autoSpotterBot.service.StadtService;
import car.autoSpotterBot.button.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class AutoSpotterBotApplication implements CommandLineRunner {

	@Autowired
	private MyBot myBot;

	public static void main(String[] args) {
		SpringApplication.run(AutoSpotterBotApplication.class, args);
	}

	@Override
	public void run(String... args) throws TelegramApiException {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		telegramBotsApi.registerBot(myBot);
	}
}
