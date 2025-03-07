package pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto;

import java.util.List;

public record JuryProposalRequest(Long studentId, List<Long> professorIds) {}