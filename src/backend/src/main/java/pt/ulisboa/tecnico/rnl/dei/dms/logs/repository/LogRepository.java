package pt.ulisboa.tecnico.rnl.dei.dms.logs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.rnl.dei.dms.logs.domain.Log;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findByActionContainingIgnoreCase(String action);
    List<Log> findByPersonContainingIgnoreCase(String person);
    List<Log> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<Log> findAllByOrderByTimestampDesc();
}