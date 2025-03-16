package pt.ulisboa.tecnico.rnl.dei.dms.logs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.rnl.dei.dms.logs.domain.Log;
import pt.ulisboa.tecnico.rnl.dei.dms.logs.dto.CreateLogRequest;
import pt.ulisboa.tecnico.rnl.dei.dms.logs.dto.LogDto;
import pt.ulisboa.tecnico.rnl.dei.dms.logs.repository.LogRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final LogRepository logRepository;

    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public LogDto createLog(CreateLogRequest request) {
        Log log = new Log(request.action(), request.person(), request.details());
        Log savedLog = logRepository.save(log);
        return new LogDto(savedLog);
    }

    public List<LogDto> getAllLogs() {
        return logRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(LogDto::new)
                .collect(Collectors.toList());
    }

    public List<LogDto> getLogsByAction(String action) {
        return logRepository.findByActionContainingIgnoreCase(action)
                .stream()
                .map(LogDto::new)
                .collect(Collectors.toList());
    }

    public List<LogDto> getLogsByPerson(String person) {
        return logRepository.findByPersonContainingIgnoreCase(person)
                .stream()
                .map(LogDto::new)
                .collect(Collectors.toList());
    }

    public List<LogDto> getLogsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        return logRepository.findByTimestampBetween(startOfDay, endOfDay)
                .stream()
                .map(LogDto::new)
                .collect(Collectors.toList());
    }
}