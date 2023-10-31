package car.autoSpotterBot.model.transport;


import car.autoSpotterBot.model.BotUser;
import car.autoSpotterBot.model.Standort;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS) // TPC Strategy
public abstract class Transport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private BotUser user;

    @ManyToOne
    @JoinColumn(name = "stadt_id")
    private Standort standort;

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

    public Standort getStandort() {
        return standort;
    }

    public void setStandort(Standort standort) {
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