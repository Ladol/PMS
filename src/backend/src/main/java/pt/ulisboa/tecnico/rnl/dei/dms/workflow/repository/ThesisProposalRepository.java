package pt.ulisboa.tecnico.rnl.dei.dms.workflow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisProposal;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisState;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.DefenseState;

@Repository
@Transactional
public interface ThesisProposalRepository extends JpaRepository<ThesisProposal, Long> {
    Optional<ThesisProposal> findByStudentId(Long studentId);
    List<ThesisProposal> findByThesisState(ThesisState thesisState);

    List<ThesisProposal> findByThesisStateAndDefenseState(ThesisState thesisState, DefenseState defenseState);
    List<ThesisProposal> findByThesisStateAndDefenseStateIsNull(ThesisState thesisState);
}