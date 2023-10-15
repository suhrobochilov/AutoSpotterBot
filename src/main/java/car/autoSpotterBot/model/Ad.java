package car.autoSpotterBot.model;


import jakarta.persistence.*;

import java.util.List;

@Entity
public class Ad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private BotUser user; // Der Benutzer, der die Anzeige erstellt hat

    @ManyToMany(mappedBy = "favoriteAds")
    private List<BotUser> favoritedByUsers; // Benutzer, die diese Anzeige favorisiert haben

    @ManyToOne
    @JoinColumn(name = "stadt_id")
    private Stadt standort;

    // Weitere Attribute der Anzeige
    private String imageUrl;
    private String manufacturer;
    private String model;
    private int buildYear;
    private String color;
    private int mileage;
    private String price;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BotUser getUser() {
        return user;
    }

    public void setUser(BotUser user) {
        this.user = user;
    }

    public List<BotUser> getFavoritedByUsers() {
        return favoritedByUsers;
    }

    public void setFavoritedByUsers(List<BotUser> favoritedByUsers) {
        this.favoritedByUsers = favoritedByUsers;
    }

    public Stadt getStandort() {
        return standort;
    }

    public void setStandort(Stadt standort) {
        this.standort = standort;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getBuildYear() {
        return buildYear;
    }

    public void setBuildYear(int buildYear) {
        this.buildYear = buildYear;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

