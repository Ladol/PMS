package pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Enumerated(EnumType.STRING)
    private ThesisState thesisState = ThesisState.PROPOSTA_JURI_SUBMETIDA;
    @Enumerated(EnumType.STRING)
    private DefenseState defenseState = null;
    
    @ManyToOne
    @JoinColumn(name = "sc_approver_id")
    private Person scApprover;
    
    private LocalDateTime scApprovalDate;

    @ManyToOne
    @JoinColumn(name = "jury_president_id")
    private Person juryPresident;
    
    @ManyToOne
    @JoinColumn(name = "coordinator_id")
    private Person coordinatorAssigner;
    
    private LocalDateTime presidentAssignmentDate;

    @ManyToOne
    @JoinColumn(name = "document_signer_id")
    private Person documentSigner;
    
    private LocalDateTime documentSignDate;
    
    private String signedDocumentPath;

    @ManyToOne
    @JoinColumn(name = "fenix_submitter_id")
    private Person fenixSubmitter;
    
    private LocalDateTime fenixSubmissionDate;

    @ManyToOne
    @JoinColumn(name = "defense_scheduler_id")
    private Person defenseScheduler;
    
    private LocalDateTime defenseScheduleDate;
    
    private LocalDateTime plannedDefenseDate;

    @ManyToOne
    @JoinColumn(name = "grader_id")
    private Person grader;
    
    private LocalDateTime gradingDate;
    
    private Integer grade;
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