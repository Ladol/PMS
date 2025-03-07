package pt.ulisboa.tecnico.rnl.dei.dms.person.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

@Repository
@Transactional
public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByType(Person.PersonType type);
}
