package car.autoSpotterBot.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "user_table")
public class BotUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "telegram_id")
    private Long telegramId;
    private String username;
    private String firstName;
    private String lastName;
    private boolean isAdmin;

    @OneToMany(mappedBy = "user")
    private List<Ad> ads;

    @ManyToMany
    @JoinTable(
            name = "user_favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "ad_id")
    )
    private List<Ad> favoriteAds;

    @ManyToMany
    @JoinTable(
            name = "user_notifications",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "stadt_id")
    )
    private List<Stadt> notificationCities;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public List<Ad> getAds() {
        return ads;
    }

    public void setAds(List<Ad> ads) {
        this.ads = ads;
    }

    public List<Ad> getFavoriteAds() {
        return favoriteAds;
    }

    public void setFavoriteAds(List<Ad> favoriteAds) {
        this.favoriteAds = favoriteAds;
    }

    public List<Stadt> getNotificationCities() {
        return notificationCities;
    }

    public void setNotificationCities(List<Stadt> notificationCities) {
        this.notificationCities = notificationCities;
    }
}

