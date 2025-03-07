package pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

@Data
@Entity
@Table(name = "thesis_proposals")
public class ThesisProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Person student;

    @ManyToMany
    @JoinTable(
        name = "proposal_jury",
        joinColumns = @JoinColumn(name = "proposal_id"),
        inverseJoinColumns = @JoinColumn(name = "professor_id")
    )
    private Set<Person> juryMembers = new HashSet<>();

    private LocalDateTime submissionDate;
    
    // Default constructor
    protected ThesisProposal() {
    }
    
    // Constructor with parameters
    public ThesisProposal(Person student, Set<Person> juryMembers) {
        this.student = student;
        this.juryMembers = juryMembers;
        this.submissionDate = LocalDateTime.now();
    }
}