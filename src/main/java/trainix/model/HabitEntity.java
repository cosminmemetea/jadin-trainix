// src/main/java/trainix/model/HabitEntity.java
package trainix.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Entity
@Table(name = "habits")
public class HabitEntity implements Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Getter
    private LocalDate lastDone;

    @Getter
    @Column(nullable = false)
    private Integer intervalDays;

    @Getter
    @Column(nullable = false)
    private int timesCompleted = 0;

    public HabitEntity() {
    }

    // ── Habit interface methods ───────────────────────────────
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * If never done, due today; else lastDone + intervalDays.
     */
    @Override
    public LocalDate getNextDue() {
        return (lastDone == null)
                ? LocalDate.now()
                : lastDone.plusDays(intervalDays);
    }

    @Override
    public void markDone() {
        this.lastDone = LocalDate.now();
        this.timesCompleted++;
    }

    // ── Getters & setters ─────────────────────────────────────

}
