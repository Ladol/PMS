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
}