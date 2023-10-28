package car.autoSpotterBot.model;


import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
public class Ad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id",nullable = true)
    private BotUser user;

    @ManyToMany(cascade = CascadeType.REMOVE)
    private List<BotUser> favoritedByUsers;

    @ManyToOne
    @JoinColumn(name = "stadt_id")
    private Stadt standort;
    @Column(length = 5000)
    private List<String> imageUrl;
    @Column(length = 1000)
    private String description;
    @Column(length = 5000)
    private String videoUrl;

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

    public List<String> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(List<String> imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}

