package car.autoSpotterBot.model.transport;

import car.autoSpotterBot.model.BotUser;
import jakarta.persistence.*;

@Entity
public class Favorit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private BotUser user;

    @ManyToOne
    @JoinColumn(name = "transport_id")
    private Ad transport;
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

    public Ad getTransport() {
        return transport;
    }

    public void setTransport(Ad anzeige) {
        this.transport = anzeige;
    }
}

