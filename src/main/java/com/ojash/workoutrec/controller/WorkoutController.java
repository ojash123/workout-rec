// PATH: src/main/java/com/ojash/workoutrec/controller/WorkoutController.java
package com.ojash.workoutrec.controller;

import com.ojash.workoutrec.dto.RecommendationDto;
import com.ojash.workoutrec.dto.SetDto;
import com.ojash.workoutrec.entity.PerformedExercise;
import com.ojash.workoutrec.entity.Workout;
import com.ojash.workoutrec.service.RecommendationService;
import com.ojash.workoutrec.service.WorkoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workout")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final RecommendationService recommendationService;

    public WorkoutController(WorkoutService workoutService, RecommendationService recommendationService) {
        this.workoutService = workoutService;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/start")
    public ResponseEntity<Workout> startWorkout() {
        String username = getCurrentUsername();
        Workout newWorkout = workoutService.startWorkout(username);
        return ResponseEntity.ok(newWorkout);
    }

    @GetMapping("/recommendation")
    public ResponseEntity<RecommendationDto> getNextRecommendation(@RequestParam(defaultValue = "1") int daysSinceLastWorkout) {
        // This is simplified; in a real app, you'd get the user ID
        Long currentUserId = 1L; // Placeholder
        RecommendationDto recommendation = recommendationService.getRecommendation(currentUserId, daysSinceLastWorkout);
        return ResponseEntity.ok(recommendation);
    }

    @PostMapping("/{workoutId}/record-exercise")
    public ResponseEntity<PerformedExercise> recordExercise(@PathVariable Long workoutId, @RequestParam String exerciseName) {
        PerformedExercise performedExercise = workoutService.recordNewExercise(workoutId, exerciseName);
        return ResponseEntity.ok(performedExercise);
    }


    @PostMapping("/record-set")
    public ResponseEntity<Void> recordSet(@RequestBody SetDto setDto) {
        workoutService.recordSet(setDto);
        return ResponseEntity.ok().build();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        // Fallback for unauthenticated or different principal types
        return "anonymousUser";
    }
}