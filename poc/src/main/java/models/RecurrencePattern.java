package models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class RecurrencePattern {

    public enum RecurrenceType { DAILY, WEEKLY, MONTHLY }

    private final RecurrenceType type;
    private final List<DayOfWeek> weekdays;
    private final Integer dayOfMonth;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int interval;

    public RecurrencePattern(RecurrenceType type, List<DayOfWeek> weekdays,
                             Integer dayOfMonth, LocalDate startDate,
                             LocalDate endDate, int interval) {
        this.type = type;
        this.weekdays = weekdays;
        this.dayOfMonth = dayOfMonth;
        this.startDate = startDate;
        this.endDate = endDate;
        this.interval = interval;
    }

    public RecurrenceType getType() { return type; }
    public List<DayOfWeek> getWeekdays() { return weekdays; }
    public Integer getDayOfMonth() { return dayOfMonth; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getInterval() { return interval; }
}
