// PATH: src/main/java/com/ojash/workoutrec/service/RecommendationService.java
package com.ojash.workoutrec.service;

import com.ojash.workoutrec.dto.RecommendationDto;

public interface RecommendationService {
    // We'll need a more complex state object later, but this is a start
    RecommendationDto getRecommendation(Long workoutId, int daysSinceLastWorkout);}