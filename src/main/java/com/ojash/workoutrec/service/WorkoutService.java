// PATH: src/main/java/com/ojash/workoutrec/service/WorkoutService.java
package com.ojash.workoutrec.service;

import com.ojash.workoutrec.dto.SetDto;
import com.ojash.workoutrec.entity.PerformedExercise;
import com.ojash.workoutrec.entity.Workout;

public interface WorkoutService {
    Workout startWorkout(String username);
    PerformedExercise recordNewExercise(Long workoutId, String exerciseName);
    void recordSet(SetDto setDto);
}