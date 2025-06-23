// PATH: src/main/java/com/ojash/workoutrec/service/WorkoutService.java
package com.ojash.workoutrec.service;

import com.ojash.workoutrec.dto.ExerciseSubmissionDto;
import com.ojash.workoutrec.dto.SetDto;
import com.ojash.workoutrec.dto.WorkoutDetailDto;
import com.ojash.workoutrec.dto.WorkoutSummaryDto;
import com.ojash.workoutrec.entity.PerformedExercise;
import com.ojash.workoutrec.entity.Workout;

import java.util.List;

public interface WorkoutService {
    Workout startWorkout(String username);
    PerformedExercise recordNewExercise(Long workoutId, String exerciseName);
    void recordSet(SetDto setDto);
    void recordExercisePerformance(ExerciseSubmissionDto submissionDto);
    List<WorkoutSummaryDto> getWorkoutHistory(String username);
    WorkoutDetailDto getWorkoutDetails(Long workoutId);
}