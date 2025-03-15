package pt.ulisboa.tecnico.rnl.dei.dms.person.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisProposal;

public record StudentDto(
    long id, 
    String name, 
    String istId, 
    String email, 
    String type,
    String thesisState,
    String defenseState
) {
    public StudentDto(Person person, ThesisProposal proposal) {
        this(
            person.getId(), 
            person.getName(), 
            person.getIstId(), 
            person.getEmail(),
            person.getType().toString(),
            proposal != null ? proposal.getThesisState().toString() : null,
            proposal != null ? (proposal.getDefenseState() != null ? proposal.getDefenseState().toString() : null) : null
        );
    }
    
    public StudentDto(Person person) {
        this(
            person.getId(), 
            person.getName(), 
            person.getIstId(), 
            person.getEmail(),
            person.getType().toString(),
            null,
            null
        );
    }
}