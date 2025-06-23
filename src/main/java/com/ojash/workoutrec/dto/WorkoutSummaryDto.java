// PATH: src/main/java/com/ojash/workoutrec/dto/WorkoutSummaryDto.java
package com.ojash.workoutrec.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class WorkoutSummaryDto {
    private Long workoutId;
    private LocalDate date;
    private long exerciseCount;
    private long setCount;
}