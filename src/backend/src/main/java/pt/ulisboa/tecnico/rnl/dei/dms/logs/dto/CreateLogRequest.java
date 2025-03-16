package pt.ulisboa.tecnico.rnl.dei.dms.logs.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLogRequest(
    @NotBlank(message = "Action cannot be empty")
    String action,
    
    @NotBlank(message = "Person cannot be empty")
    String person,
    
    String details
) {
}