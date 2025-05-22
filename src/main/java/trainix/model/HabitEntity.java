package trainix.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "habits")
public class HabitEntity implements Habit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate lastDone;
    private int    intervalDays;  // e.g. 1=daily, 7=weekly

    @Override public Long getId()       { return id; }
    @Override public String getName()   { return name; }

    @Override
    public LocalDate getNextDue() {
        return (lastDone == null
                ? LocalDate.now()
                : lastDone.plusDays(intervalDays));
    }

    @Override
    public void markDone() {
        this.lastDone = LocalDate.now();
    }

    // getters/setters for name, lastDone, intervalDays
    public void setName(String name)             { this.name = name; }
    public void setLastDone(LocalDate lastDone)  { this.lastDone = lastDone; }
    public void setIntervalDays(int days)         { this.intervalDays = days; }

    public Integer getIntervalDays() {
        return  intervalDays;
    }
}
