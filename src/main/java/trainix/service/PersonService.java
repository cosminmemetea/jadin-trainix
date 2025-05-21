package trainix.service;

import org.springframework.stereotype.Service;
import trainix.model.Person;
import trainix.repository.PersonRepository;

import java.util.List;

@Service
public class PersonService {
    private final PersonRepository repo;

    public PersonService(PersonRepository repo) {
        this.repo = repo;
    }

    public List<Person> findAll() {
        return repo.findAll();
    }

    public Person save(Person p) {
        return repo.save(p);
    }
}
