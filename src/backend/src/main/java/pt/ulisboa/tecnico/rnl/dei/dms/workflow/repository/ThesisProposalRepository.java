package pt.ulisboa.tecnico.rnl.dei.dms.workflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisProposal;

@Repository
@Transactional
public interface ThesisProposalRepository extends JpaRepository<ThesisProposal, Long> {
    Optional<ThesisProposal> findByStudentId(Long studentId);
}