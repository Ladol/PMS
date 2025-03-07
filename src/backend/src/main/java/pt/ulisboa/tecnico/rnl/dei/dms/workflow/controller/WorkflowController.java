package pt.ulisboa.tecnico.rnl.dei.dms.workflow.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.person.service.PersonService;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto.JuryProposalRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.workflow.service.WorkflowService;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private PersonService personService;

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
}