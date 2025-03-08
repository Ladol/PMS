package pt.ulisboa.tecnico.rnl.dei.dms.workflow.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisProposal;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.repository.ThesisProposalRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.ThesisProposalDto;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisState;


@Service
@Transactional
public class WorkflowService {
    
    @Autowired
    private ThesisProposalRepository proposalRepository;
    
    @Autowired
    private PersonRepository personRepository;

    @Transactional
    public void submitJuryProposal(Long studentId, List<Long> professorIds) {
        Person student = personRepository.findById(studentId)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, studentId.toString()));

        if (!student.getType().equals(PersonType.STUDENT)) {
            throw new DEIException(ErrorMessage.INVALID_USER_TYPE, "Only students can submit proposals");
        }

        Set<Person> jury = professorIds.stream()
            .map(id -> personRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, id.toString())))
            .collect(Collectors.toSet());

        if (jury.size() < 1 || jury.size() > 5) {
            throw new DEIException(ErrorMessage.INVALID_JURY_SIZE, "Jury must have 1-5 members");
        }

        ThesisProposal proposal = new ThesisProposal(student, jury);
        
        proposalRepository.save(proposal);
    }

    @Transactional
    public List<ThesisProposalDto> getPendingProposals() {
        List<ThesisProposal> proposals = proposalRepository.findByState(ThesisState.PROPOSTA_JURI_SUBMETIDA);
        System.out.println("Found " + proposals.size() + " pending proposals");
        
        return proposals.stream()
            .map(ThesisProposalDto::new)
            .collect(Collectors.toList());
    }
    @Transactional
    public void approveProposal(Long id) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PROPOSAL, id.toString()));
        
        if (proposal.getState() != ThesisState.PROPOSTA_JURI_SUBMETIDA) {
            throw new DEIException(ErrorMessage.INVALID_STATE_TRANSITION, 
                "Proposal must be in PROPOSTA_JURI_SUBMETIDA state to be approved");
        }
        
        proposal.setState(ThesisState.APROVADO_PELO_SC);
        proposalRepository.save(proposal);
    }
}