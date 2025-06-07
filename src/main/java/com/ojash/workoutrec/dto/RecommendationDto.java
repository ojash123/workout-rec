// PATH: src/main/java/com/ojash/workoutrec/dto/RecommendationDto.java
package com.ojash.workoutrec.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    private String exerciseName;
    private int targetSets;
    private int targetReps;
    private boolean endWorkout;
}