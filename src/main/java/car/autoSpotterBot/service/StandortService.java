package car.autoSpotterBot.service;

import car.autoSpotterBot.exception.StadtNotFoundException;
import car.autoSpotterBot.model.Standort;
import car.autoSpotterBot.repository.StandortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StandortService {

    private final StandortRepository standortRepository;

    @Autowired
    public StandortService(StandortRepository standortRepository) {
        this.standortRepository = standortRepository;
    }

    // Find all Städte
    public List<Standort> findAll() {
        return standortRepository.findAll();
    }

    // Find Stadt by ID
    public Standort findById(Long id) {
        return standortRepository.findById(id)
                .orElseThrow(() -> new StadtNotFoundException(id));
    }

    // Save a new Stadt
    public Standort save(Standort standort) {
        return standortRepository.save(standort);
    }

    // Update an existing Stadt
    public Standort updateStadt(Standort standort) {
        if (standortRepository.existsById(standort.getId())) {
            return standortRepository.save(standort);
        }
        throw new StadtNotFoundException(standort.getId());
    }

    // Delete a Stadt by ID
    public void deleteById(Long id) {
        if (standortRepository.existsById(id)) {
            standortRepository.deleteById(id);
        } else {
            throw new StadtNotFoundException(id);
        }
    }
    @Transactional
    public Standort saveCity(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Der Name der Stadt darf nicht leer oder null sein.");
        }

        Standort standort = standortRepository.findByName(name);
        if (standort == null) {
            standort = new Standort();
            standort.setName(name);
            standortRepository.save(standort);
        }
        return standort;
    }
}
