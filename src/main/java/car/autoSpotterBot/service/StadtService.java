package car.autoSpotterBot.service;

import car.autoSpotterBot.exception.StadtNotFoundException;
import car.autoSpotterBot.model.Stadt;
import car.autoSpotterBot.repository.StadtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StadtService {

    private final StadtRepository stadtRepository;

    @Autowired
    public StadtService(StadtRepository stadtRepository) {
        this.stadtRepository = stadtRepository;
    }

    // Find all Städte
    public List<Stadt> findAll() {
        return stadtRepository.findAll();
    }

    // Find Stadt by ID
    public Stadt findById(Long id) {
        return stadtRepository.findById(id)
                .orElseThrow(() -> new StadtNotFoundException(id));
    }

    // Save a new Stadt
    public Stadt save(Stadt stadt) {
        return stadtRepository.save(stadt);
    }

    // Update an existing Stadt
    public Stadt updateStadt(Stadt stadt) {
        if (stadtRepository.existsById(stadt.getId())) {
            return stadtRepository.save(stadt);
        }
        throw new StadtNotFoundException(stadt.getId());
    }

    // Delete a Stadt by ID
    public void deleteById(Long id) {
        if (stadtRepository.existsById(id)) {
            stadtRepository.deleteById(id);
        } else {
            throw new StadtNotFoundException(id);
        }
    }
    @Transactional
    public Stadt saveCity(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Der Name der Stadt darf nicht leer oder null sein.");
        }

        Stadt stadt = stadtRepository.findByName(name);
        if (stadt == null) {
            stadt = new Stadt();
            stadt.setName(name);
            stadtRepository.save(stadt);
        }
        return stadt;
    }
}
