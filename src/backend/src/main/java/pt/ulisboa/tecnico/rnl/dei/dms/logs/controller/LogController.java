package pt.ulisboa.tecnico.rnl.dei.dms.logs.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.rnl.dei.dms.logs.dto.CreateLogRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.logs.dto.LogDto;
import pt.ulisboa.tecnico.rnl.dei.dms.logs.service.LogService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public ResponseEntity<LogDto> createLog(@Valid @RequestBody CreateLogRequest request) {
        LogDto logDto = logService.createLog(request);
        return ResponseEntity.ok(logDto);
    }

    @GetMapping
    public ResponseEntity<List<LogDto>> getLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String person,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<LogDto> logs;
        
        if (action != null && !action.isEmpty()) {
            logs = logService.getLogsByAction(action);
        } else if (person != null && !person.isEmpty()) {
            logs = logService.getLogsByPerson(person);
        } else if (date != null) {
            logs = logService.getLogsByDate(date);
        } else {
            logs = logService.getAllLogs();
        }
        
        return ResponseEntity.ok(logs);
    }
}