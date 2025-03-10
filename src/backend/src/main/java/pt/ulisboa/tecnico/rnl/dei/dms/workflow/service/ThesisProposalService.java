package pt.ulisboa.tecnico.rnl.dei.dms.workflow.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisProposal;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisState;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.DefenseState;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.repository.ThesisProposalRepository;

@Service
public class ThesisProposalService {
    
    @Autowired
    private ThesisProposalRepository thesisProposalRepository;
    
    public List<ThesisProposal> findByThesisStateAndDefenseState(ThesisState thesisState, DefenseState defenseState) {
        if (defenseState == null) {
            return thesisProposalRepository.findByThesisStateAndDefenseStateIsNull(thesisState);
        } else {
            return thesisProposalRepository.findByThesisStateAndDefenseState(thesisState, defenseState);
        }
    }
}