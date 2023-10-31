package car.autoSpotterBot.model;


import car.autoSpotterBot.model.transport.Automobile;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Standort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "standort")
    private List<Automobile> automobiles;

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


    public List<Automobile> getAds() {
        return automobiles;
    }

    public void setAds(List<Automobile> automobiles) {
        this.automobiles = automobiles;
    }
}