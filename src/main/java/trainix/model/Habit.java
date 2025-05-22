package trainix.model;

import java.time.LocalDate;

public interface Habit {
    Long   getId();
    String getName();
    LocalDate getNextDue();
    void   markDone();
}
