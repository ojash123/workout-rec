// PATH: src/main/java/com/ojash/workoutrec/controller/DashboardController.java
package com.ojash.workoutrec.controller;

import com.ojash.workoutrec.dto.WorkoutDetailDto;
import com.ojash.workoutrec.dto.WorkoutSummaryDto;
import com.ojash.workoutrec.service.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class DashboardController {

    private final WorkoutService workoutService;

    public DashboardController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    // Serve the main dashboard page
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    // Serve the workout detail page
    @GetMapping("/workout/{id}")
    public String workoutDetail() {
        return "workout-detail";
    }


    // --- API Endpoints for History ---
    @GetMapping("/api/workouts")
    @ResponseBody
    public ResponseEntity<List<WorkoutSummaryDto>> getWorkoutHistory() {
        try {
            String username = getCurrentUsername();
            return ResponseEntity.ok(workoutService.getWorkoutHistory(username));
        } catch (IllegalStateException e) {
            // This will now catch the error if the user is not found
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated", e);
        }
    }

    @GetMapping("/api/workouts/{id}")
    @ResponseBody
    public ResponseEntity<WorkoutDetailDto> getWorkoutDetails(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getWorkoutDetails(id));
    }

    // Made this method more robust
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        // If no authenticated user is found, throw an exception
        throw new IllegalStateException("Could not find an authenticated user.");
    }
}