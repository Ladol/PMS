package pt.ulisboa.tecnico.rnl.dei.dms.logs.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_logs")
public class Log {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "person", nullable = false)
    private String person;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    protected Log() {
    }

    public Log(String action, String person, String details) {
        this.timestamp = LocalDateTime.now();
        this.action = action;
        this.person = person;
        this.details = details;
    }
}