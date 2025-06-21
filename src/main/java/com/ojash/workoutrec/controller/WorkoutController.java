// PATH: src/main/java/com/ojash/workoutrec/controller/WorkoutController.java
package com.ojash.workoutrec.controller;

import com.ojash.workoutrec.dto.ExerciseSubmissionDto; // Add import
// ... other imports
import com.ojash.workoutrec.dto.RecommendationDto;
import com.ojash.workoutrec.entity.PerformedExercise;
import com.ojash.workoutrec.entity.Workout;
import com.ojash.workoutrec.service.RecommendationService;
import com.ojash.workoutrec.service.WorkoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // Add import
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Controller // Change to @Controller to serve both REST and HTML
@RequestMapping("/workout") // Change mapping to /workout
public class WorkoutController {

    // ... existing constructor

    // This method serves the main workout page using Thymeleaf
    @GetMapping
    public String workoutPage() {
        return "workout"; // This will resolve to workout.html
    }

    // --- API Endpoints ---
    // All API endpoints are now nested under /api for clarity

    @RestController
    @RequestMapping("/api/workout")
    public static class WorkoutApiController {
        private final WorkoutService workoutService;
        private final RecommendationService recommendationService;

        public WorkoutApiController(WorkoutService workoutService, RecommendationService recommendationService) {
            this.workoutService = workoutService;
            this.recommendationService = recommendationService;
        }

        @PostMapping("/start")
        public ResponseEntity<Workout> startWorkout() {
            String username = getCurrentUsername();
            Workout newWorkout = workoutService.startWorkout(username);
            return ResponseEntity.ok(newWorkout);
        }

        @GetMapping("/{workoutId}/recommendation")
        public ResponseEntity<RecommendationDto> getNextRecommendation(
                @PathVariable Long workoutId,
                @RequestParam(defaultValue = "1") int daysSinceLastWorkout) {

            // We pass the workoutId to the service
            RecommendationDto recommendation = recommendationService.getRecommendation(workoutId, daysSinceLastWorkout);
            return ResponseEntity.ok(recommendation);
        }

        @PostMapping("/{workoutId}/record-exercise")
        public ResponseEntity<PerformedExercise> recordNewExerciseForWorkout(@PathVariable Long workoutId, @RequestParam String exerciseName) {
            PerformedExercise performedExercise = workoutService.recordNewExercise(workoutId, exerciseName);
            return ResponseEntity.ok(performedExercise);
        }

        // New Batch Endpoint
        @PostMapping("/record-performance")
        public ResponseEntity<Void> recordPerformance(@RequestBody ExerciseSubmissionDto submissionDto) {
            workoutService.recordExercisePerformance(submissionDto);
            return ResponseEntity.ok().build();
        }

        // We can now remove the old /record-set endpoint

        private String getCurrentUsername() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                return ((UserDetails) authentication.getPrincipal()).getUsername();
            }
            return "anonymousUser";
        }
    }
}