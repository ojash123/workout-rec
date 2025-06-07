// PATH: src/main/java/com/ojash/workoutrec/service/impl/WorkoutServiceImpl.java
package com.ojash.workoutrec.service.impl;

import com.ojash.workoutrec.dto.SetDto;
import com.ojash.workoutrec.entity.*;
import com.ojash.workoutrec.repository.*;
import com.ojash.workoutrec.service.WorkoutService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutRepo workoutRepo;
    private final UserRepo userRepo;
    private final ExerciseRepo exerciseRepo;
    private final PerformedExerciseRepo performedExerciseRepo;
    private final PerformedSetRepo performedSetRepo;

    public WorkoutServiceImpl(WorkoutRepo workoutRepo, UserRepo userRepo, ExerciseRepo exerciseRepo,
                              PerformedExerciseRepo performedExerciseRepo, PerformedSetRepo performedSetRepo) {
        this.workoutRepo = workoutRepo;
        this.userRepo = userRepo;
        this.exerciseRepo = exerciseRepo;
        this.performedExerciseRepo = performedExerciseRepo;
        this.performedSetRepo = performedSetRepo;
    }

    @Override
    @Transactional
    public Workout startWorkout(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        Workout workout = new Workout();
        workout.setUser(user);
        workout.setDate(LocalDate.now());
        return workoutRepo.save(workout);
    }

    @Override
    @Transactional
    public PerformedExercise recordNewExercise(Long workoutId, String exerciseName) {
        Workout workout = workoutRepo.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));
        Exercise exercise = exerciseRepo.findByName(exerciseName)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        PerformedExercise performedExercise = new PerformedExercise();
        performedExercise.setWorkout(workout);
        performedExercise.setExercise(exercise);
        return performedExerciseRepo.save(performedExercise);
    }

    @Override
    @Transactional
    public void recordSet(SetDto setDto) {
        PerformedExercise performedExercise = performedExerciseRepo.findById(setDto.getPerformedExerciseId())
                .orElseThrow(() -> new RuntimeException("Performed Exercise not found"));

        PerformedSet performedSet = new PerformedSet();
        performedSet.setPerformedExercise(performedExercise);
        performedSet.setSetNumber(setDto.getSetNumber());
        performedSet.setActualReps(setDto.getActualReps());
        performedSet.setWeightUsed(setDto.getWeightUsed());
        // 'targetReps' would likely come from the recommendation step

        performedSetRepo.save(performedSet);
    }
}