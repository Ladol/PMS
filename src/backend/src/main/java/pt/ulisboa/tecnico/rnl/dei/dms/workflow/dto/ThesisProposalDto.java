package pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.DefenseState;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisProposal;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisState;

public record ThesisProposalDto(
    Long id,
    PersonDto student,
    Set<PersonDto> juryMembers,
    LocalDateTime submissionDate,
    ThesisState thesisState,
    DefenseState defenseState,
    PersonDto scApprover,
    LocalDateTime scApprovalDate,
    PersonDto juryPresident,
    PersonDto coordinatorAssigner,
    LocalDateTime presidentAssignmentDate,
    PersonDto documentSigner,
    LocalDateTime documentSignDate,
    String signedDocumentPath,
    PersonDto fenixSubmitter,
    LocalDateTime fenixSubmissionDate,
    PersonDto defenseScheduler,
    LocalDateTime defenseScheduleDate,
    LocalDateTime plannedDefenseDate,
    PersonDto grader,
    LocalDateTime gradingDate,
    Integer grade
) {
    public ThesisProposalDto(ThesisProposal proposal) {
        this(
            proposal.getId(),
            proposal.getStudent() != null ? new PersonDto(proposal.getStudent()) : null,
            proposal.getJuryMembers() != null ? 
                proposal.getJuryMembers().stream()
                    .map(PersonDto::new)
                    .collect(Collectors.toSet()) : 
                null,
            proposal.getSubmissionDate(),
            proposal.getThesisState(),
            proposal.getDefenseState(),
            proposal.getScApprover() != null ? new PersonDto(proposal.getScApprover()) : null,
            proposal.getScApprovalDate(),
            proposal.getJuryPresident() != null ? new PersonDto(proposal.getJuryPresident()) : null,
            proposal.getCoordinatorAssigner() != null ? new PersonDto(proposal.getCoordinatorAssigner()) : null,
            proposal.getPresidentAssignmentDate(),
            proposal.getDocumentSigner() != null ? new PersonDto(proposal.getDocumentSigner()) : null,
            proposal.getDocumentSignDate(),
            proposal.getSignedDocumentPath(),
            proposal.getFenixSubmitter() != null ? new PersonDto(proposal.getFenixSubmitter()) : null,
            proposal.getFenixSubmissionDate(),
            proposal.getDefenseScheduler() != null ? new PersonDto(proposal.getDefenseScheduler()) : null,
            proposal.getDefenseScheduleDate(),
            proposal.getPlannedDefenseDate(),
            proposal.getGrader() != null ? new PersonDto(proposal.getGrader()) : null,
            proposal.getGradingDate(),
            proposal.getGrade()
        );
    }
}