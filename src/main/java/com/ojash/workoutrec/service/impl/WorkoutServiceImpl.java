// PATH: src/main/java/com/ojash/workoutrec/service/impl/WorkoutServiceImpl.java
package com.ojash.workoutrec.service.impl;

import com.ojash.workoutrec.dto.SetDto;
import com.ojash.workoutrec.dto.ExerciseSubmissionDto;
import com.ojash.workoutrec.dto.WorkoutDetailDto;
import com.ojash.workoutrec.dto.WorkoutSummaryDto;
import com.ojash.workoutrec.entity.*;
import com.ojash.workoutrec.repository.*;
import com.ojash.workoutrec.service.WorkoutService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public void recordExercisePerformance(ExerciseSubmissionDto submissionDto) {
        PerformedExercise performedExercise = performedExerciseRepo.findById(submissionDto.getPerformedExerciseId())
                .orElseThrow(() -> new RuntimeException("Performed Exercise not found with ID: " + submissionDto.getPerformedExerciseId()));

        for (ExerciseSubmissionDto.SetData setData : submissionDto.getSets()) {
            PerformedSet performedSet = new PerformedSet();
            performedSet.setPerformedExercise(performedExercise);
            performedSet.setSetNumber(setData.getSetNumber());
            performedSet.setActualReps(setData.getActualReps());
            performedSet.setWeightUsed(setData.getWeightUsed());
            performedSetRepo.save(performedSet);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutSummaryDto> getWorkoutHistory(String username) {
        User user = userRepo.findByUsername(username);
        return user.getWorkouts().stream()
                .map(workout -> {
                    long exerciseCount = workout.getPerformedExercises().size();
                    long setCount = workout.getPerformedExercises().stream()
                            .mapToLong(pe -> pe.getPerformedSets().size())
                            .sum();
                    return new WorkoutSummaryDto(workout.getId(), workout.getDate(), exerciseCount, setCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutDetailDto getWorkoutDetails(Long workoutId) {
        Workout workout = workoutRepo.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        WorkoutDetailDto dto = new WorkoutDetailDto();
        dto.setWorkoutId(workout.getId());
        dto.setDate(workout.getDate());

        List<WorkoutDetailDto.PerformedExerciseDto> exerciseDtos = workout.getPerformedExercises().stream()
                .map(pe -> {
                    WorkoutDetailDto.PerformedExerciseDto peDto = new WorkoutDetailDto.PerformedExerciseDto();
                    peDto.setExerciseName(pe.getExercise().getName());
                    peDto.setNotes(pe.getNotes());

                    List<WorkoutDetailDto.PerformedSetDto> setDtos = pe.getPerformedSets().stream()
                            .map(ps -> {
                                WorkoutDetailDto.PerformedSetDto psDto = new WorkoutDetailDto.PerformedSetDto();
                                psDto.setSetNumber(ps.getSetNumber());
                                psDto.setActualReps(ps.getActualReps());
                                psDto.setWeightUsed(ps.getWeightUsed());
                                return psDto;
                            }).collect(Collectors.toList());

                    peDto.setPerformedSets(setDtos);
                    return peDto;
                }).collect(Collectors.toList());

        dto.setPerformedExercises(exerciseDtos);
        return dto;
    }
}