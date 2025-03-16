package pt.ulisboa.tecnico.rnl.dei.dms.person.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;

@Data
@Entity
@Table(name = "people")
public class Person {

	public enum PersonType {
		COORDINATOR, STAFF, STUDENT, TEACHER, SC
	}

	@Id
	@GeneratedValue
	private Long id;

	@NotBlank(message = "Name cannot be empty")
	@Column(name = "name", nullable = false)
	private String name;

	@NotBlank(message = "IST ID cannot be empty")
	@Column(name = "ist_id", nullable = false, unique = true)
	private String istId;

	@NotBlank(message = "Email cannot be empty")
	@Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email must be valid")
	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
    private PersonType type;

	protected Person() {
	}

	public Person(String name, String istId, String email, PersonType type) {
		this.name = name;
		this.istId = istId;
		this.email = email;
		this.type = type;
	}

	public Person(PersonDto personDto) {
		this(personDto.name(), personDto.istId(), personDto.email(),
				PersonType.valueOf(personDto.type().toUpperCase()));
		System.out.println("PersonDto: " + personDto);
		System.out.println("PersonType: " + personDto.type());
	}
}
