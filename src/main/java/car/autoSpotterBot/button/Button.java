package car.autoSpotterBot.button;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Service
public class Button {
    public InlineKeyboardMarkup mainMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton transport = new InlineKeyboardButton();
        InlineKeyboardButton realEstate = new InlineKeyboardButton();
        InlineKeyboardButton electronics = new InlineKeyboardButton();
        InlineKeyboardButton services = new InlineKeyboardButton();
        transport.setText(ButtonConstant.transport);
        transport.setCallbackData(ButtonConstant.transport);
        realEstate.setText(ButtonConstant.realEstate);
        realEstate.setCallbackData(ButtonConstant.realEstate);
        electronics.setText(ButtonConstant.electronics);
        electronics.setCallbackData(ButtonConstant.electronics);
        services.setText(ButtonConstant.service);
        services.setCallbackData(ButtonConstant.service);
        row1.add(transport);
        row1.add(realEstate);
        row2.add(electronics);
        row2.add(services);
        rows.add(row1);
        rows.add(row2);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlKeyboardConfirmation() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton morePhotosBtn = new InlineKeyboardButton();
        InlineKeyboardButton favoriteBtn = new InlineKeyboardButton();
        morePhotosBtn.setText("Tasdiqlash");
        morePhotosBtn.setCallbackData(ButtonConstant.confirm);
        favoriteBtn.setText("Bekor qilish");
        favoriteBtn.setCallbackData(ButtonConstant.cancel);

        row.add(morePhotosBtn);
        row.add(favoriteBtn);
        rows.add(row);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlKeyboardForMyAds(Long adId, Integer nextIndex) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton deleteAd = new InlineKeyboardButton();
        InlineKeyboardButton next = new InlineKeyboardButton();
        InlineKeyboardButton previous = new InlineKeyboardButton();
        InlineKeyboardButton video = new InlineKeyboardButton();
        deleteAd.setText("E'lonni o'chirish \uD83D\uDD34");
        deleteAd.setCallbackData(ButtonConstant.deleteAd + "_" + adId);
        next.setText("Keyingi rasm ▶\uFE0F");
        next.setCallbackData(ButtonConstant.nextPhoto + adId + "_" + nextIndex);
        previous.setText("◀\uFE0F Oldingi rasm");
        previous.setCallbackData(ButtonConstant.previousPhoto + adId + "_" + nextIndex);
        video.setText("Video \uD83D\uDCF9");
        video.setCallbackData(ButtonConstant.video + adId);
        row1.add(previous);
        row1.add(next);
        row2.add(video);
        row2.add(deleteAd);
        rows.add(row1);
        rows.add(row2);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlKeyboardForAd(Long adID, Integer nextIndex) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton next = new InlineKeyboardButton();
        InlineKeyboardButton previous = new InlineKeyboardButton();
        InlineKeyboardButton favorite = new InlineKeyboardButton();
        InlineKeyboardButton video = new InlineKeyboardButton();
        next.setText("Keyingi rasm ▶\uFE0F");
        next.setCallbackData(ButtonConstant.nextPhoto + adID + "_" + nextIndex);
        previous.setText("◀\uFE0F Oldingi rasm");
        previous.setCallbackData(ButtonConstant.previousPhoto + adID + "_" + nextIndex);
        video.setText("Video \uD83D\uDCF9");
        video.setCallbackData(ButtonConstant.video + adID);
        favorite.setText("Favoritga qo'shish");
        favorite.setCallbackData(ButtonConstant.favorite + adID);

        row1.add(previous);
        row1.add(next);
        row2.add(video);
        row2.add(favorite);
        rows.add(row1);
        rows.add(row2);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlKeyboardAddFav(Long adID, Integer nextIndex) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton next = new InlineKeyboardButton();
        InlineKeyboardButton previous = new InlineKeyboardButton();
        InlineKeyboardButton favorite = new InlineKeyboardButton();
        InlineKeyboardButton video = new InlineKeyboardButton();
        next.setText("Keyingi rasm ▶\uFE0F");
        next.setCallbackData(ButtonConstant.nextPhoto + adID + "_" + nextIndex);
        previous.setText("◀\uFE0F Oldingi rasm");
        previous.setCallbackData(ButtonConstant.previousPhoto + adID + "_" + nextIndex);
        video.setText("Video \uD83D\uDCF9");
        video.setCallbackData(ButtonConstant.video + adID);
        favorite.setText("Favoritga qo'shildi ☑\uFE0F");
        favorite.setCallbackData(ButtonConstant.favorite + adID);

        row1.add(previous);
        row1.add(next);
        row2.add(video);
        row2.add(favorite);
        rows.add(row1);
        rows.add(row2);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlKeyboardMyFavorite(Long adId, Integer nextIndex) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton deleteAd = new InlineKeyboardButton();
        InlineKeyboardButton next = new InlineKeyboardButton();
        InlineKeyboardButton previous = new InlineKeyboardButton();
        InlineKeyboardButton video = new InlineKeyboardButton();
        deleteAd.setText("Favoritdan o'chirish \uD83D\uDD34");
        deleteAd.setCallbackData(ButtonConstant.deleteAdFromFavorite + "_" + adId);
        next.setText("Keyingi rasm ▶\uFE0F");
        next.setCallbackData(ButtonConstant.nextPhoto + adId + "_" + nextIndex);
        previous.setText("◀\uFE0F Oldingi rasm");
        previous.setCallbackData(ButtonConstant.previousPhoto + adId + "_" + nextIndex);
        video.setText("Video \uD83D\uDCF9");
        video.setCallbackData(ButtonConstant.video + adId);
        row1.add(previous);
        row1.add(next);
        row2.add(video);
        row2.add(deleteAd);
        rows.add(row1);
        rows.add(row2);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup transMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();

        InlineKeyboardButton automobile = new InlineKeyboardButton();
        InlineKeyboardButton truck = new InlineKeyboardButton();
        InlineKeyboardButton agroTech = new InlineKeyboardButton();
        InlineKeyboardButton otherTrans = new InlineKeyboardButton();
        InlineKeyboardButton spareParts = new InlineKeyboardButton();
        InlineKeyboardButton mainMenu = new InlineKeyboardButton();

        automobile.setText(ButtonConstant.automobile);
        automobile.setCallbackData(ButtonConstant.automobile);
        truck.setText(ButtonConstant.truck);
        truck.setCallbackData(ButtonConstant.truck);
        agroTech.setText(ButtonConstant.agroTech);
        agroTech.setCallbackData(ButtonConstant.agroTech);
        otherTrans.setText(ButtonConstant.otherTrans);
        otherTrans.setCallbackData(ButtonConstant.otherTrans);
        spareParts.setText(ButtonConstant.spareParts);
        spareParts.setCallbackData(ButtonConstant.spareParts);
        mainMenu.setText(ButtonConstant.mainMenu);
        mainMenu.setCallbackData(ButtonConstant.mainMenu);
        row1.add(automobile);
        row1.add(truck);
        row2.add(otherTrans);
        row2.add(spareParts);
        row3.add(mainMenu);
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
    public InlineKeyboardMarkup realEstateMenu() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();

        InlineKeyboardButton apartment = new InlineKeyboardButton();
        InlineKeyboardButton buildingLot = new InlineKeyboardButton();
        InlineKeyboardButton businessPremise = new InlineKeyboardButton();
        InlineKeyboardButton house = new InlineKeyboardButton();
        InlineKeyboardButton parkingSpaces = new InlineKeyboardButton();
        InlineKeyboardButton rentalHome = new InlineKeyboardButton();
        InlineKeyboardButton mainMenu = new InlineKeyboardButton();

        apartment.setText(ButtonConstant.apartment);
        apartment.setCallbackData(ButtonConstant.apartment);
        buildingLot.setText(ButtonConstant.buildingLot);
        buildingLot.setCallbackData(ButtonConstant.buildingLot);
        businessPremise.setText(ButtonConstant.businessPremise);
        businessPremise.setCallbackData(ButtonConstant.businessPremise);
        house.setText(ButtonConstant.house);
        house.setCallbackData(ButtonConstant.house);
        parkingSpaces.setText(ButtonConstant.parkingSpace);
        parkingSpaces.setCallbackData(ButtonConstant.parkingSpace);
        rentalHome.setText(ButtonConstant.rentalHome);
        rentalHome.setCallbackData(ButtonConstant.rentalHome);
        mainMenu.setText(ButtonConstant.mainMenu);
        mainMenu.setCallbackData(ButtonConstant.mainMenu);
        row1.add(apartment);
        row1.add(house);
        row2.add(businessPremise);
        row2.add(buildingLot);
        row3.add(rentalHome);
        row3.add(parkingSpaces);
        row4.add(mainMenu);
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup nextPage() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        replyKeyboardMarkup.setResizeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add(new KeyboardButton(ButtonConstant.previousPage));
        row1.add(new KeyboardButton(ButtonConstant.nextPage));
        row2.add(new KeyboardButton(ButtonConstant.back));

        keyboard.add(row1);
        keyboard.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
    public InlineKeyboardMarkup createInlineKeyboardForPages(int currentPage, int totalPages) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Erstellen Sie die Zahlenreihe für die Seiten
        List<InlineKeyboardButton> pageButtons = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            InlineKeyboardButton pageButton = new InlineKeyboardButton();
            pageButton.setText(Integer.toString(i));
            pageButton.setCallbackData("page_" + i); // Der Callback sollte die Seitennummer enthalten
            pageButtons.add(pageButton);

            // Fügen Sie nach jeder Reihe von 5 Buttons eine neue Reihe hinzu
            if (i % 5 == 0 || i == totalPages) {
                rows.add(pageButtons);
                pageButtons = new ArrayList<>();
            }
        }

        // Füge Navigationsbuttons hinzu, wenn notwendig
        if (totalPages > 1) {
            List<InlineKeyboardButton> navigationButtons = new ArrayList<>();

            InlineKeyboardButton previousButton = new InlineKeyboardButton();
            previousButton.setText("⬅️");
            // Fügen Sie Logik hinzu, um den Callback für den vorherigen Button zu bestimmen
            previousButton.setCallbackData("page_" + Math.max(1, currentPage - 1));
            navigationButtons.add(previousButton);

            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("➡️");
            // Fügen Sie Logik hinzu, um den Callback für den nächsten Button zu bestimmen
            nextButton.setCallbackData("page_" + Math.min(totalPages, currentPage + 1));
            navigationButtons.add(nextButton);

            rows.add(navigationButtons);
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }



    public ReplyKeyboardMarkup startMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        replyKeyboardMarkup.setResizeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add(new KeyboardButton(ButtonConstant.placeAd));
        row1.add(new KeyboardButton(ButtonConstant.searchAd));
        row2.add(new KeyboardButton(ButtonConstant.myAds));
        row2.add(new KeyboardButton(ButtonConstant.myFavorite));

        keyboard.add(row1);
        keyboard.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup cities() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (List<String> cityRow : getCities()) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            for (String city : cityRow) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(city);
                button.setCallbackData(city);
                rowInline.add(button);
            }
            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public List<List<String>> getCities() {
        List<String> bundeslanderRow1 = Arrays.asList("Toshkent", "Andijon", "Buxoro");
        List<String> bundeslanderRow2 = Arrays.asList("Farg'ona", "Jizzax", "Sirdaryo");
        List<String> bundeslanderRow3 = Arrays.asList("Namangan", "Samarqand", "Xorazm");
        List<String> bundeslanderRow4 = Arrays.asList("Qashqadaryo", "Surxandaryo");
        List<String> bundeslanderRow5 = Arrays.asList("Qoraqalpog'iston", "Navoi");
        List<String> bundeslanderRow6 = List.of(ButtonConstant.backInAutoAd);

        return Arrays.asList(bundeslanderRow1, bundeslanderRow2, bundeslanderRow3, bundeslanderRow4, bundeslanderRow5, bundeslanderRow6);
    }


}