// PATH: src/main/java/com/ojash/workoutrec/dto/SetDto.java
package com.ojash.workoutrec.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetDto {
    private long performedExerciseId;
    private int setNumber;
    private int actualReps;
    private float weightUsed;
}