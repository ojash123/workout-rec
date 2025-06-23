// PATH: src/main/java/com/ojash/workoutrec/controller/DashboardController.java
package com.ojash.workoutrec.controller;

import com.ojash.workoutrec.dto.WorkoutDetailDto;
import com.ojash.workoutrec.dto.WorkoutSummaryDto;
import com.ojash.workoutrec.service.WorkoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
        return "dashboard"; // Renders dashboard.html
    }

    // Serve the workout detail page
    @GetMapping("/workout/{id}")
    public String workoutDetail() {
        return "workout-detail"; // Renders workout-detail.html
    }

    // --- API Endpoints for History ---

    // API to get list of past workouts
    @GetMapping("/api/workouts")
    @ResponseBody
    public ResponseEntity<List<WorkoutSummaryDto>> getWorkoutHistory() {
        String username = getCurrentUsername();
        return ResponseEntity.ok(workoutService.getWorkoutHistory(username));
    }

    // API to get details for a single workout
    @GetMapping("/api/workouts/{id}")
    @ResponseBody
    public ResponseEntity<WorkoutDetailDto> getWorkoutDetails(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getWorkoutDetails(id));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }
}