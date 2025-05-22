package trainix.repository;

import trainix.model.HabitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository
        extends JpaRepository<HabitEntity, Long> {
}
