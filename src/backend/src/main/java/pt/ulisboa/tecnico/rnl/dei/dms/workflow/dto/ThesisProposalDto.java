package pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisProposal;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisState;

public record ThesisProposalDto(
    Long id,
    PersonDto student,
    Set<PersonDto> juryMembers,
    LocalDateTime submissionDate,
    ThesisState state
) {
    public ThesisProposalDto(ThesisProposal proposal) {
        this(
            proposal.getId(),
            new PersonDto(proposal.getStudent()),
            proposal.getJuryMembers().stream()
                .map(PersonDto::new)
                .collect(Collectors.toSet()),
            proposal.getSubmissionDate(),
            proposal.getState()
        );
    }
}