// PATH: src/main/java/com/ojash/workoutrec/dto/ExerciseSubmissionDto.java
package com.ojash.workoutrec.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExerciseSubmissionDto {
    // We'll record the exercise first to get this ID, then submit performance
    private Long performedExerciseId;
    private List<SetData> sets;

    @Getter
    @Setter
    public static class SetData {
        private int setNumber;
        private int actualReps;
        private float weightUsed;
    }
}