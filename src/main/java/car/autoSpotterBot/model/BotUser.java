package car.autoSpotterBot.model;

import car.autoSpotterBot.model.transport.Favorit;
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
    private List<Ad> automobiles;

    @OneToMany(mappedBy = "user")
    private List<Favorit> favoriten;

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
        return automobiles;
    }

    public void setAds(List<Ad> automobiles) {
        this.automobiles = automobiles;
    }

    public List<Ad> getAutomobiles() {
        return automobiles;
    }

    public void setAutomobiles(List<Ad> automobiles) {
        this.automobiles = automobiles;
    }

    public List<Favorit> getFavoriten() {
        return favoriten;
    }

    public void setFavoriten(List<Favorit> favoriten) {
        this.favoriten = favoriten;
    }
}

