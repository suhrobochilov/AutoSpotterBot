package car.autoSpotterBot.service;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.exception.AdNotFoundException;
import car.autoSpotterBot.model.Ad;
import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.realeState.Apartment;
import car.autoSpotterBot.model.realeState.House;
import car.autoSpotterBot.model.transport.*;
import car.autoSpotterBot.repository.BotUserRepository;
import car.autoSpotterBot.repository.transport.FavoritRepository;
import car.autoSpotterBot.repository.transport.TransportRepository;
import car.autoSpotterBot.service.realEstate.ApartmentService;
import car.autoSpotterBot.service.realEstate.HouseService;
import car.autoSpotterBot.service.transport.*;
import car.autoSpotterBot.util.MessageId;
import car.autoSpotterBot.util.MessageType;
import car.autoSpotterBot.util.transportUtils.BotCallback;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdService {
    private static final Logger log = LoggerFactory.getLogger(AdService.class);
    private final BotCallback botCallback;
    private final Button button;
    private final AutomobileService automobileService;
    private final TruckService truckService;
    private final AgroTechnologyService agroTechService;
    private final SparePartsService sparePartsService;
    private final OtherTransportService otherTransportService;
    private final ApartmentService apartmentService;
    private final HouseService houseService;
    private final MessageId messageId;
    private final GeneralService generalService;
    private final Map<Long, Integer> photoIndexMap = new HashMap<>();


    public AdService(BotCallback botCallback, Button button, AutomobileService automobileService, TruckService truckService, AgroTechnologyService agroTechService, SparePartsService sparePartsService, OtherTransportService otherTransportService, ApartmentService apartmentService, HouseService houseService, MessageId messageId, GeneralService generalService) {

        this.botCallback = botCallback;
        this.button = button;
        this.automobileService = automobileService;
        this.truckService = truckService;
        this.agroTechService = agroTechService;
        this.sparePartsService = sparePartsService;
        this.otherTransportService = otherTransportService;
        this.apartmentService = apartmentService;
        this.houseService = houseService;
        this.messageId = messageId;
        this.generalService = generalService;
    }



}
