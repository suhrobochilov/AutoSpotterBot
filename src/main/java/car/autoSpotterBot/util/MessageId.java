package car.autoSpotterBot.util;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageId {
    private final List<Integer> idFavoriteAds = new ArrayList<>();
    private final List<Integer> idOfAdsShown = new ArrayList<>();
    private final List<Integer> idOfMyAds = new ArrayList<>();
    private int idSearchButton;
    private int idPlaceAdButton;
    private int idOfButton;

    public MessageId() {
    }

    public int getIdOfButton() {
        return idOfButton;
    }

    public void setIdOfButton(int idOfButton) {
        this.idOfButton = idOfButton;
    }

    public List<Integer> getIdOfAdsShown() {
        return idOfAdsShown;
    }

    public void setIdOfAdsShown(Integer idOfAdsShown) {
        this.idOfAdsShown.add(idOfAdsShown);
    }

    public List<Integer> getIdFavoriteAds() {
        return idFavoriteAds;
    }

    public void setIdFavoriteAds(int idFavoriteAds) {
        this.idFavoriteAds.add(idFavoriteAds);
    }

    public List<Integer> getIdOfMyAds() {
        return idOfMyAds;
    }

    public void setIdOfMyAds(Integer idOfMyAds) {
        this.idOfMyAds.add(idOfMyAds);
    }

    public Integer getIdSearchButton() {
        return idSearchButton;
    }

    public void setIdSearchButton(int idSearchButton) {
        this.idSearchButton = idSearchButton;
    }

    public void setIdSearchButton(Integer idSearchButton) {
        this.idSearchButton = idSearchButton;
    }

    public Integer getIdPlaceAdButton() {
        return idPlaceAdButton;
    }

    public void setIdPlaceAdButton(int idPlaceAdButton) {
        this.idPlaceAdButton = idPlaceAdButton;
    }

    public void setIdPlaceAdButton(Integer idPlaceAdButton) {
        this.idPlaceAdButton = idPlaceAdButton;
    }
}
