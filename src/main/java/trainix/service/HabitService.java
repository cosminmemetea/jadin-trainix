// src/main/java/trainix/service/HabitService.java
package trainix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainix.model.HabitEntity;
import trainix.repository.HabitRepository;

import java.util.List;
import java.util.Optional;

@Service
public class HabitService {

    private final HabitRepository repo;

    public HabitService(HabitRepository repo) {
        this.repo = repo;
    }

    /**
     * Fetch all habits.
     */
    public List<HabitEntity> findAll() {
        return repo.findAll();
    }

    /**
     * Create or update a habit.
     */
    public HabitEntity save(HabitEntity habit) {
        return repo.save(habit);
    }

    /**
     * Mark the habit done today (increments timesCompleted).
     */
    @Transactional
    public void markDone(Long id) {
        Optional<HabitEntity> opt = repo.findById(id);
        opt.ifPresent(h -> {
            h.markDone();
            repo.save(h);
        });
    }

    /**
     * (Optional) fetch one by id.
     */
    public Optional<HabitEntity> findById(Long id) {
        return repo.findById(id);
    }
}
