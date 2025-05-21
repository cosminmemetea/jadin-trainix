package trainix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trainix.model.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
}