package trainix.service;

import org.springframework.stereotype.Service;
import trainix.model.HabitEntity;
import trainix.repository.HabitRepository;

import java.util.List;

@Service
public class HabitService {
    private final HabitRepository repo;

    public HabitService(HabitRepository repo) {
        this.repo = repo;
    }

    public List<HabitEntity> findAll() {
        return repo.findAll();
    }

    public HabitEntity save(HabitEntity h) {
        return repo.save(h);
    }

    public void markDone(Long id) {
        repo.findById(id).ifPresent(h -> {
            h.markDone();
            repo.save(h);
        });
    }
}
