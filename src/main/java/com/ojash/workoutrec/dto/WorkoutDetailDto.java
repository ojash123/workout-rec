// PATH: src/main/java/com/ojash/workoutrec/dto/WorkoutDetailDto.java
package com.ojash.workoutrec.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class WorkoutDetailDto {
    private Long workoutId;
    private LocalDate date;
    private List<PerformedExerciseDto> performedExercises;

    @Getter
    @Setter
    public static class PerformedExerciseDto {
        private String exerciseName;
        private String notes;
        private List<PerformedSetDto> performedSets;
    }

    @Getter
    @Setter
    public static class PerformedSetDto {
        private int setNumber;
        private int actualReps;
        private float weightUsed;
    }
}