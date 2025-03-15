package pt.ulisboa.tecnico.rnl.dei.dms.person.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
@Service
@Transactional
public class PersonService {

	@Autowired
	private PersonRepository personRepository;

	private Person fetchPersonOrThrow(long id) {
		return personRepository.findById(id)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, Long.toString(id)));
	}

    @Transactional
    public List<PersonDto> getPeopleByType(String type) {
        return personRepository.findByType(Person.PersonType.valueOf(type.toUpperCase()))
            .stream()
            .map(PersonDto::new)
            .toList();
    }

	@Transactional
	public List<PersonDto> getPeople() {
		return personRepository.findAll().stream()
				.map(PersonDto::new)
				.toList();
	}

	@Transactional
	public PersonDto createPerson(PersonDto personDto) {
		validatePersonDto(personDto);
		return savePerson(null, personDto);
	}

	@Transactional
	public PersonDto getPerson(long id) {
		return new PersonDto(fetchPersonOrThrow(id));
	}

	@Transactional
	public PersonDto updatePerson(long id, PersonDto personDto) {
		fetchPersonOrThrow(id); // ensure exists
		validatePersonDto(personDto);
		return savePerson(id, personDto);
	}
	
	private PersonDto savePerson(Long id, PersonDto personDto) {
		Person person = new Person(personDto);
		person.setId(id); // null for create, existing id for update
		return new PersonDto(personRepository.save(person));
	}
	
	private void validatePersonDto(PersonDto personDto) {
		if (personDto.name() == null || personDto.name().trim().isEmpty()) {
			throw new DEIException(ErrorMessage.INVALID_PERSON_DATA, "Name cannot be empty");
		}
		
		if (personDto.istId() == null || personDto.istId().trim().isEmpty()) {
			throw new DEIException(ErrorMessage.INVALID_PERSON_DATA, "IST ID cannot be empty");
		}
		
		if (personDto.email() == null || !personDto.email().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
			throw new DEIException(ErrorMessage.INVALID_PERSON_DATA, "Invalid email format");
		}
		
		try {
			Person.PersonType.valueOf(personDto.type().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new DEIException(ErrorMessage.INVALID_PERSON_DATA, "Invalid person type: " + personDto.type());
		}
	}

	@Transactional
	public void deletePerson(long id) {
		fetchPersonOrThrow(id); // ensure exists

		personRepository.deleteById(id);
	}
}
