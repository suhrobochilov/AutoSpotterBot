package car.autoSpotterBot.util.realEstateUtils;

import car.autoSpotterBot.button.Button;
import car.autoSpotterBot.button.ButtonConstant;
import car.autoSpotterBot.state.UserStateManager;
import car.autoSpotterBot.state.UserStateRealEstate;
import car.autoSpotterBot.util.transportUtils.BotCallback;
import car.autoSpotterBot.util.transportUtils.TransportInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Comparator;
import java.util.List;

import static car.autoSpotterBot.state.UserStateConstants.*;

@Service
public class RealEstateInterpreter {
    private static final Logger log = LoggerFactory.getLogger(TransportInterpreter.class);
    private final Button button;
    private final BotCallback botCallback;
    private final UserStateRealEstate userStateRealEstate;
    private final UserStateManager userStateManager;
    private final ApartmentInterpreter apartmentInterpreter;
    private final ParkingSpaceInterpreter parkingSpaceInterpreter;
    private final HouseInterpreter houseInterpreter;
    private final BuildingLotInterpreter buildingLotInterpreter;
    private final BusinessPremiseInterpreter businessPremiseInterpreter;
    private final RentalHomeInterpreter rentalHomeInterpreter;

    public RealEstateInterpreter(Button button, BotCallback botCallback, UserStateRealEstate userStateRealEstate, UserStateManager userStateManager, ApartmentInterpreter apartmentInterpreter, ParkingSpaceInterpreter parkingSpaceInterpreter, HouseInterpreter houseInterpreter, BuildingLotInterpreter buildingLotInterpreter, BusinessPremiseInterpreter businessPremiseInterpreter, RentalHomeInterpreter rentalHomeInterpreter) {
        this.button = button;
        this.botCallback = botCallback;
        this.userStateRealEstate = userStateRealEstate;
        this.userStateManager = userStateManager;
        this.apartmentInterpreter = apartmentInterpreter;
        this.parkingSpaceInterpreter = parkingSpaceInterpreter;
        this.houseInterpreter = houseInterpreter;
        this.buildingLotInterpreter = buildingLotInterpreter;
        this.businessPremiseInterpreter = businessPremiseInterpreter;
        this.rentalHomeInterpreter = rentalHomeInterpreter;
    }

    public void interpreter(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            int messageId = update.getMessage().getMessageId();
            if (update.hasMessage() && update.getMessage().hasPhoto()) {
                List<PhotoSize> photoSizes = update.getMessage().getPhoto();
                PhotoSize largestPhoto = photoSizes.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
                String photoUrl = largestPhoto.getFileId();
                String caption = update.getMessage().getCaption();
                if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(APARTMENT)) {
                    apartmentInterpreter.interpreter(chatId, messageId, caption, photoUrl, null);
                } else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(BUILDING_LOT)) {
                    buildingLotInterpreter.interpreter(chatId, messageId, caption, photoUrl, null);
                } else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(BUSINESS_PREMISE)) {
                    businessPremiseInterpreter.interpreter(chatId, messageId, caption, photoUrl, null);
                } else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(HOUSE)) {
                    houseInterpreter.interpreter(chatId, messageId, caption, photoUrl, null);
                } else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(PARKING_SPACE)) {
                    parkingSpaceInterpreter.interpreter(chatId, messageId, caption, photoUrl, null);
                } else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(RENTAL_HOME)) {
                    rentalHomeInterpreter.interpreter(chatId, messageId, caption, photoUrl, null);
                }

            } else if (update.hasMessage() && update.getMessage().hasVideo()) {
                Video video = update.getMessage().getVideo();
                String videoUrl = video.getFileId();
                String caption = update.getMessage().getCaption();
                if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(APARTMENT)) {
                    apartmentInterpreter.interpreter(chatId, messageId, caption, null, videoUrl);
                } else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(BUILDING_LOT)) {
                    buildingLotInterpreter.interpreter(chatId, messageId, caption, null, videoUrl);
                } else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(BUSINESS_PREMISE)) {
                    businessPremiseInterpreter.interpreter(chatId, messageId, caption, null, videoUrl);
                } else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(HOUSE)) {
                    houseInterpreter.interpreter(chatId, messageId, caption, null, videoUrl);
                } else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(PARKING_SPACE)) {
                    parkingSpaceInterpreter.interpreter(chatId, messageId, caption, null, videoUrl);
                }else if (userStateRealEstate.getUserStatusRealEstate(chatId).equals(RENTAL_HOME)) {
                    rentalHomeInterpreter.interpreter(chatId, messageId, caption, null, videoUrl);
                }

            }

        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getFrom().getId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            String text = update.getCallbackQuery().getData();
            log.info("State: " + userStateRealEstate.getUserStatusRealEstate(chatId) + " " + "text: " + text);
            if (userStateRealEstate.getUserStatusRealEstate(chatId) != null &&
                    userStateRealEstate.getUserStatusRealEstate(chatId).equals(APARTMENT)) {
                apartmentInterpreter.interpreter(chatId, messageId, text, null, null);
            } else if (userStateRealEstate.getUserStatusRealEstate(chatId) != null &&
                    userStateRealEstate.getUserStatusRealEstate(chatId).equals(BUILDING_LOT)) {
                buildingLotInterpreter.interpreter(chatId, messageId, text, null, null);
            } else if (userStateRealEstate.getUserStatusRealEstate(chatId) != null &&
                    userStateRealEstate.getUserStatusRealEstate(chatId).equals(BUSINESS_PREMISE)) {
                businessPremiseInterpreter.interpreter(chatId, messageId, text, null, null);
            } else if (userStateRealEstate.getUserStatusRealEstate(chatId) != null &&
                    userStateRealEstate.getUserStatusRealEstate(chatId).equals(HOUSE)) {
                houseInterpreter.interpreter(chatId, messageId, text, null, null);
            } else if (userStateRealEstate.getUserStatusRealEstate(chatId) != null &&
                    userStateRealEstate.getUserStatusRealEstate(chatId).equals(PARKING_SPACE)) {
                parkingSpaceInterpreter.interpreter(chatId, messageId, text, null, null);
            }else if (userStateRealEstate.getUserStatusRealEstate(chatId) != null &&
                    userStateRealEstate.getUserStatusRealEstate(chatId).equals(RENTAL_HOME)) {
                rentalHomeInterpreter.interpreter(chatId, messageId, text, null, null);
            }

            switch (text) {
                case ButtonConstant.apartment -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateRealEstate.setUserStatusRealEstate(chatId, APARTMENT);
                }
                case ButtonConstant.buildingLot -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateRealEstate.setUserStatusRealEstate(chatId, BUILDING_LOT);
                }
                case ButtonConstant.businessPremise -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateRealEstate.setUserStatusRealEstate(chatId, BUSINESS_PREMISE);
                }
                case ButtonConstant.house -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateRealEstate.setUserStatusRealEstate(chatId, HOUSE);
                }
                case ButtonConstant.parkingSpace -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateRealEstate.setUserStatusRealEstate(chatId, PARKING_SPACE);
                }
                case ButtonConstant.rentalHome -> {
                    InlineKeyboardMarkup newKeyboard = button.cities();
                    checkUserSubStatus(chatId, messageId, newKeyboard);
                    userStateRealEstate.setUserStatusRealEstate(chatId, RENTAL_HOME);
                }
            }

        }
    }

    private void checkUserSubStatus(long chatId, int messageId, InlineKeyboardMarkup newKeyboard) {
        if (userStateManager.getUserSubStatus(chatId).equals(PLACE_AD)) {
            botCallback.editMessage(chatId, messageId, "Qaysi viloyatda e'lon bermoqchisiz?", newKeyboard);
        } else {
            botCallback.editMessage(chatId, messageId, "Qaysi viloyatda qidirmoqchisiz?", newKeyboard);
        }
        userStateRealEstate.setUserStatusRealEstate(chatId,null);
    }
}
