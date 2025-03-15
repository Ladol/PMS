package pt.ulisboa.tecnico.rnl.dei.dms.workflow.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.person.service.PersonService;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.JuryProposalRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.ScheduleDefenseRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.SignDocumentRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.SubmitFenixRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.ThesisProposalDto;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.AssignPresidentRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.GradeThesisRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.UpdateDefenseStateRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.RevertStateRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.domain.ThesisState;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.service.WorkflowService;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.service.ThesisProposalService;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private PersonService personService;
    
    @Autowired
    private ThesisProposalService thesisProposalService;

    @GetMapping("/professors")
    public List<PersonDto> getProfessors() {
        return personService.getPeopleByType("TEACHER");
    }

    @PostMapping("/proposta-juri")
    public ResponseEntity<Void> submitJuryProposal(
            @RequestBody JuryProposalRequest request) {
        workflowService.submitJuryProposal(request.studentId(), request.professorIds());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/proposals/with-president")
    public List<ThesisProposalDto> getProposalsWithPresident() {
        return workflowService.getProposalsWithPresident();
    }
    
    @GetMapping("/proposals/pending")
    public List<ThesisProposalDto> getPendingProposals() {
        return workflowService.getPendingProposals();
    }
    @GetMapping("/proposals/approved")
    public List<ThesisProposalDto> getApprovedProposals() {
        return workflowService.getApprovedProposals();
    }
    @PostMapping("/proposals/{id}/approve")
    public ResponseEntity<Void> approveProposal(
            @PathVariable Long id,
            @RequestBody Long scId) {
        workflowService.approveProposal(id, scId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/proposals/{id}/reject")
    public ResponseEntity<Void> rejectProposal(
            @PathVariable Long id,
            @RequestBody Long scId) {
        workflowService.rejectProposal(id, scId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/proposals/{id}/assign-president")
    public ResponseEntity<Void> assignJuryPresident(
            @PathVariable Long id,
            @RequestBody AssignPresidentRequest request) {
        workflowService.assignJuryPresident(id, request.coordinatorId(), request.presidentId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/proposals/{id}/sign-document")
    public ResponseEntity<Void> signDocument(
            @PathVariable Long id,
            @RequestBody SignDocumentRequest request) {
        workflowService.signDocument(id, request.coordinatorId(), request.documentPath());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/proposals/signed")
    public List<ThesisProposalDto> getSignedProposals() {
        return workflowService.getSignedProposals();
    }

    @GetMapping("/proposals/submitted")
    public List<ThesisProposalDto> getSubmittedProposals() {
        return workflowService.getSubmittedProposals();
    }

    @PostMapping("/proposals/{id}/submit-fenix")
    public ResponseEntity<Void> submitToFenix(
            @PathVariable Long id,
            @RequestBody SubmitFenixRequest request) {
        workflowService.submitToFenix(id, request.staffId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/proposals/{id}/schedule-defense")
    public ResponseEntity<Void> scheduleDefense(
            @PathVariable Long id,
            @RequestBody ScheduleDefenseRequest request) {
        workflowService.scheduleDefense(id, request.coordinatorId(), request.defenseDate());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/proposals/{id}/grade")
    public ResponseEntity<Void> gradeThesis(
            @PathVariable Long id,
            @RequestBody GradeThesisRequest request) {
        workflowService.gradeThesis(id, request.coordinatorId(), request.grade());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/proposals/for-defense-scheduling")
    public List<ThesisProposalDto> getProposalsForDefenseScheduling() {
        return thesisProposalService.findByThesisStateAndDefenseState(
            ThesisState.TESE_SUBMETIDO_AO_FENIX, null)
            .stream()
            .map(proposal -> new ThesisProposalDto(proposal))
            .collect(Collectors.toList());
    }

    @GetMapping("/proposals/scheduled-defenses")
    public List<ThesisProposalDto> getScheduledDefenses() {
        return workflowService.getScheduledDefenses();
    }

    @PostMapping("/proposals/{id}/update-defense-state")
    public ResponseEntity<Void> updateDefenseState(
            @PathVariable Long id,
            @RequestBody UpdateDefenseStateRequest request) {
        workflowService.updateDefenseState(id, request.defenseState());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/proposals/{id}/revert-state")
    public ResponseEntity<Void> revertState(
            @PathVariable Long id,
            @RequestBody RevertStateRequest request) {
        workflowService.revertState(id, request.coordinatorId());
        return ResponseEntity.ok().build();
    }
}