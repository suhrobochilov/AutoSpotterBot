package car.autoSpotterBot.model;


import jakarta.persistence.*;

import java.util.List;

@Entity
public class Stadt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "standort")
    private List<Ad> ads;

    @ManyToMany(mappedBy = "notificationCities")
    private List<BotUser> usersWithNotifications;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<Ad> getAds() {
        return ads;
    }

    public void setAds(List<Ad> ads) {
        this.ads = ads;
    }

    public List<BotUser> getUsersWithNotifications() {
        return usersWithNotifications;
    }

    public void setUsersWithNotifications(List<BotUser> usersWithNotifications) {
        this.usersWithNotifications = usersWithNotifications;
    }
}
