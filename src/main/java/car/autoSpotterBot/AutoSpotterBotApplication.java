package car.autoSpotterBot;

import car.autoSpotterBot.autoUtil.UserStateManager;
import car.autoSpotterBot.configuration.BotConfig;
import car.autoSpotterBot.service.AdService;
import car.autoSpotterBot.service.BotUserService;
import car.autoSpotterBot.service.StadtService;
import car.autoSpotterBot.button.Button;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class AutoSpotterBotApplication {
	private BotUserService userService;
	private Button buttonService;
	private  AdService adService;
	private StadtService stadtService;
	private UserStateManager userStateManager;
	private BotConfig botConfig;

	public static void main(String[] args) throws TelegramApiException{
		AutoSpotterBotApplication autoSpotterBotApplication = new AutoSpotterBotApplication();
		autoSpotterBotApplication.run(args);
	}
	public void run(String[] args) throws TelegramApiException {
		ApplicationContext context = SpringApplication.run(AutoSpotterBotApplication.class, args);

		BotUserService userService = context.getBean(BotUserService.class);
		Button buttonService = context.getBean(Button.class);
		AdService adService = context.getBean(AdService.class);
		StadtService stadtService = context.getBean(StadtService.class);
		UserStateManager userStateManager = context.getBean(UserStateManager.class);
		BotConfig botConfig = context.getBean(BotConfig.class);
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		telegramBotsApi.registerBot( new MyBot(userService,buttonService, stadtService, adService, userStateManager,botConfig));
	}

}
