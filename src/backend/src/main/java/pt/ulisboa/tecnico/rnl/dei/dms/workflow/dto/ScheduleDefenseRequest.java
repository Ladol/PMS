package pt.ulisboa.tecnico.rnl.dei.dms.workflow.dto;

import java.time.LocalDateTime;

public record ScheduleDefenseRequest(Long coordinatorId, LocalDateTime defenseDate) {}