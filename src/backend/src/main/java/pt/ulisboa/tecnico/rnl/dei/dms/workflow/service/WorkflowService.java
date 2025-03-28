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
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.DefenseState;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisProposal;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisState;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.ThesisProposalDto;
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
        proposal.setThesisState(ThesisState.PROPOSTA_JURI_SUBMETIDA);
        
        proposalRepository.save(proposal);
    }
    
    @Transactional
    public List<ThesisProposalDto> getPendingProposals() {
        List<ThesisProposal> proposals = proposalRepository.findByThesisState(ThesisState.PROPOSTA_JURI_SUBMETIDA);
        System.out.println("Found " + proposals.size() + " pending proposals");
        
        return proposals.stream()
            .map(ThesisProposalDto::new)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public List<ThesisProposalDto> getProposalsWithPresident() {
        List<ThesisProposal> proposals = proposalRepository.findByThesisState(ThesisState.PRESIDENTE_JURI_ATRIBUIDO);
        System.out.println("Found " + proposals.size() + " proposals with president assigned");
            
        return proposals.stream()
            .map(ThesisProposalDto::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public List<ThesisProposalDto> getApprovedProposals() {
        List<ThesisProposal> proposals = proposalRepository.findByThesisState(ThesisState.APROVADO_PELO_SC);
        System.out.println("Found " + proposals.size() + " approved proposals");
        
        return proposals.stream()
            .map(ThesisProposalDto::new)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void approveProposal(Long id, Long scId) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NOT_FOUND, "Proposal with id " + id + " not found"));
        
        Person sc = personRepository.findById(scId)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, scId.toString()));
            
        if (!sc.getType().equals(PersonType.SC)) {
            throw new DEIException(ErrorMessage.INVALID_USER_TYPE, "Only SC members can approve proposals");
        }
        
        if (proposal.getThesisState() != ThesisState.PROPOSTA_JURI_SUBMETIDA) {
            throw new DEIException(ErrorMessage.INVALID_STATE_TRANSITION, 
                "Proposal must be in PROPOSTA_JURI_SUBMETIDA state to be approved");
        }
        
        proposal.setThesisState(ThesisState.APROVADO_PELO_SC);
        proposal.setScApprover(sc);
        proposal.setScApprovalDate(LocalDateTime.now());
        
        proposalRepository.save(proposal);
    }
    
    @Transactional
    public void rejectProposal(Long id, Long scId) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NOT_FOUND, "Proposal with id " + id + " not found"));
        
        Person sc = personRepository.findById(scId)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, scId.toString()));
            
        if (!sc.getType().equals(PersonType.SC)) {
            throw new DEIException(ErrorMessage.INVALID_USER_TYPE, "Only SC members can reject proposals");
        }
        
        if (proposal.getThesisState() != ThesisState.PROPOSTA_JURI_SUBMETIDA) {
            throw new DEIException(ErrorMessage.INVALID_STATE_TRANSITION, 
                "Proposal must be in PROPOSTA_JURI_SUBMETIDA state to be rejected");
        }
        
        proposal.setThesisState(ThesisState.REJEITADO_PELO_SC);
        proposal.setScApprover(sc);
        proposal.setScApprovalDate(LocalDateTime.now());
        
        proposalRepository.save(proposal);
    }
    
    @Transactional
    public void assignJuryPresident(Long id, Long coordinatorId, Long presidentId) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NOT_FOUND, "Proposal with id " + id + " not found"));
        
        Person coordinator = personRepository.findById(coordinatorId)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, coordinatorId.toString()));
            
        if (!coordinator.getType().equals(PersonType.COORDINATOR)) {
            throw new DEIException(ErrorMessage.INVALID_USER_TYPE, "Only coordinators can assign jury presidents");
        }
        
        Person president = personRepository.findById(presidentId)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, presidentId.toString()));
            
        if (!president.getType().equals(PersonType.TEACHER)) {
            throw new DEIException(ErrorMessage.INVALID_USER_TYPE, "Only teachers can be jury presidents");
        }
        
        if (!proposal.getJuryMembers().contains(president)) {
            throw new DEIException(ErrorMessage.INVALID_OPERATION, "President must be a member of the jury");
        }
        
        if (proposal.getThesisState() != ThesisState.APROVADO_PELO_SC) {
            throw new DEIException(ErrorMessage.INVALID_STATE_TRANSITION, 
                "Proposal must be in APROVADO_PELO_SC state to assign a president");
        }
        
        proposal.setThesisState(ThesisState.PRESIDENTE_JURI_ATRIBUIDO);
        proposal.setJuryPresident(president);
        proposal.setCoordinatorAssigner(coordinator);
        proposal.setPresidentAssignmentDate(LocalDateTime.now());
        
        proposalRepository.save(proposal);
    }
    
    @Transactional
    public void signDocument(Long id, Long coordinatorId, String documentPath) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NOT_FOUND, "Proposal with id " + id + " not found"));
        
        Person coordinator = personRepository.findById(coordinatorId)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, coordinatorId.toString()));
            
        if (!coordinator.getType().equals(PersonType.COORDINATOR)) {
            throw new DEIException(ErrorMessage.INVALID_USER_TYPE, "Only coordinators can sign documents");
        }
        
        if (proposal.getThesisState() != ThesisState.PRESIDENTE_JURI_ATRIBUIDO) {
            throw new DEIException(ErrorMessage.INVALID_STATE_TRANSITION, 
                "Proposal must be in PRESIDENTE_JURI_ATRIBUIDO state to sign document");
        }
        
        proposal.setThesisState(ThesisState.DOCUMENTO_ASSINADO);
        proposal.setDocumentSigner(coordinator);
        proposal.setDocumentSignDate(LocalDateTime.now());
        proposal.setSignedDocumentPath(documentPath);
        
        proposalRepository.save(proposal);
    }

    @Transactional
    public List<ThesisProposalDto> getSignedProposals() {
        List<ThesisProposal> proposals = proposalRepository.findByThesisState(ThesisState.DOCUMENTO_ASSINADO);
        System.out.println("Found " + proposals.size() + " signed proposals");
        
        return proposals.stream()
            .map(ThesisProposalDto::new)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void submitToFenix(Long id, Long staffId) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NOT_FOUND, "Proposal with id " + id + " not found"));
        
        Person staff = personRepository.findById(staffId)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, staffId.toString()));
            
        if (!staff.getType().equals(PersonType.STAFF)) {
            throw new DEIException(ErrorMessage.INVALID_USER_TYPE, "Only staff can submit to Fenix");
        }
        
        if (proposal.getThesisState() != ThesisState.DOCUMENTO_ASSINADO) {
            throw new DEIException(ErrorMessage.INVALID_STATE_TRANSITION, 
                "Proposal must be in DOCUMENTO_ASSINADO state to submit to Fenix");
        }
        
        proposal.setThesisState(ThesisState.TESE_SUBMETIDO_AO_FENIX);
        proposal.setFenixSubmitter(staff);
        proposal.setFenixSubmissionDate(LocalDateTime.now());
        
        proposalRepository.save(proposal);
    }
    
    @Transactional
    public void scheduleDefense(Long id, Long coordinatorId, LocalDateTime defenseDate) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NOT_FOUND, "Proposal with id " + id + " not found"));
        
        Person coordinator = personRepository.findById(coordinatorId)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, coordinatorId.toString()));
            
        if (!coordinator.getType().equals(PersonType.COORDINATOR)) {
            throw new DEIException(ErrorMessage.INVALID_USER_TYPE, "Only coordinators can schedule defenses");
        }
        
        if (proposal.getThesisState() != ThesisState.TESE_SUBMETIDO_AO_FENIX) {
            throw new DEIException(ErrorMessage.INVALID_STATE_TRANSITION, 
                "Proposal must be in SUBMETIDO_AO_FENIX state to schedule defense");
        }
        
        proposal.setDefenseState(DefenseState.DEFESA_AGENDADA);
        proposal.setDefenseScheduler(coordinator);
        proposal.setDefenseScheduleDate(LocalDateTime.now());
        proposal.setPlannedDefenseDate(defenseDate);
        
        proposalRepository.save(proposal);
    }
    
    @Transactional
    public List<ThesisProposalDto> getSubmittedProposals() {
        List<ThesisProposal> proposals = proposalRepository.findByThesisState(ThesisState.TESE_SUBMETIDO_AO_FENIX);
        System.out.println("Found " + proposals.size() + " submitted proposals");
        
        return proposals.stream()
            .map(ThesisProposalDto::new)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public List<ThesisProposalDto> getScheduledDefenses() {
        List<ThesisProposal> proposals = proposalRepository.findByDefenseState(DefenseState.DEFESA_AGENDADA);
        proposals.addAll(proposalRepository.findByDefenseState(DefenseState.EM_REVISAO));
        
        System.out.println("Found " + proposals.size() + " scheduled defenses");
        
        return proposals.stream()
            .map(ThesisProposalDto::new)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void updateDefenseState(Long id, DefenseState defenseState) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NOT_FOUND, "Proposal with id " + id + " not found"));
        
        proposal.setDefenseState(defenseState);
        
        proposalRepository.save(proposal);
    }

    @Transactional
    public void gradeThesis(Long id, Long coordinatorId, Integer grade) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NOT_FOUND, "Proposal with id " + id + " not found"));
        
        Person coordinator = personRepository.findById(coordinatorId)
            .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, coordinatorId.toString()));
            
        if (!coordinator.getType().equals(PersonType.COORDINATOR)) {
            throw new DEIException(ErrorMessage.INVALID_USER_TYPE, "Only coordinators can grade theses");
        }
        
        if (proposal.getDefenseState() != DefenseState.EM_REVISAO) {
            throw new DEIException(ErrorMessage.INVALID_STATE_TRANSITION, 
                "Defense must be in EM_REVISAO state to grade thesis");
        }
        
        if (grade < 0 || grade > 20) {
            throw new DEIException(ErrorMessage.INVALID_OPERATION, "Grade must be between 0 and 20");
        }
        
        proposal.setDefenseState(DefenseState.DEFESA_SUBMETIDO_AO_FENIX);
        proposal.setGrader(coordinator);
        proposal.setGradingDate(LocalDateTime.now());
        proposal.setGrade(grade);
        
        proposalRepository.save(proposal);
    }

    @Transactional
    public void revertState(Long id, Long coordinatorId) {
        ThesisProposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new DEIException(ErrorMessage.NOT_FOUND, "Proposal with id " + id + " not found"));
            
        // Handle defense state reversion first
        if (proposal.getDefenseState() != null) {
            switch (proposal.getDefenseState()) {
                case DEFESA_AGENDADA:
                    proposal.setDefenseState(null);
                    proposal.setDefenseScheduler(null);
                    proposal.setDefenseScheduleDate(null);
                    proposal.setPlannedDefenseDate(null);
                    break;
                case EM_REVISAO:
                    proposal.setDefenseState(DefenseState.DEFESA_AGENDADA);
                    break;
                case DEFESA_SUBMETIDO_AO_FENIX:
                    proposal.setDefenseState(DefenseState.EM_REVISAO);
                    break;
            }
        } 
        // If no defense state, revert thesis state
        else {
            switch (proposal.getThesisState()) {
                case PROPOSTA_JURI_SUBMETIDA:
                    // Can't revert initial state
                    throw new DEIException(ErrorMessage.INVALID_STATE_TRANSITION, 
                        "Cannot revert from initial state PROPOSTA_JURI_SUBMETIDA");
                case APROVADO_PELO_SC:
                    proposal.setThesisState(ThesisState.PROPOSTA_JURI_SUBMETIDA);
                    proposal.setScApprover(null);
                    proposal.setScApprovalDate(null);
                    break;
                case REJEITADO_PELO_SC:
                    proposal.setThesisState(ThesisState.PROPOSTA_JURI_SUBMETIDA);
                    proposal.setScApprover(null);
                    proposal.setScApprovalDate(null);
                    break;
                case PRESIDENTE_JURI_ATRIBUIDO:
                    proposal.setThesisState(ThesisState.APROVADO_PELO_SC);
                    proposal.setJuryPresident(null);
                    proposal.setCoordinatorAssigner(null);
                    proposal.setPresidentAssignmentDate(null);
                    break;
                case DOCUMENTO_ASSINADO:
                    proposal.setThesisState(ThesisState.PRESIDENTE_JURI_ATRIBUIDO);
                    proposal.setDocumentSigner(null);
                    proposal.setDocumentSignDate(null);
                    proposal.setSignedDocumentPath(null);
                    break;
                case TESE_SUBMETIDO_AO_FENIX:
                    proposal.setThesisState(ThesisState.DOCUMENTO_ASSINADO);
                    proposal.setFenixSubmitter(null);
                    proposal.setFenixSubmissionDate(null);
                    break;
            }
        }
        
        proposalRepository.save(proposal);
    }
}