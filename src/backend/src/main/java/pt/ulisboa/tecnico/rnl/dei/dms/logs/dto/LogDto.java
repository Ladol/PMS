package pt.ulisboa.tecnico.rnl.dei.dms.logs.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.logs.domain.Log;
import java.time.LocalDateTime;

public record LogDto(
    Long id,
    LocalDateTime timestamp,
    String action,
    String person,
    String details
) {
    public LogDto(Log log) {
        this(
            log.getId(),
            log.getTimestamp(),
            log.getAction(),
            log.getPerson(),
            log.getDetails()
        );
    }
}