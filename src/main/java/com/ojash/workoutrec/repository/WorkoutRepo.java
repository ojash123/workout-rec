// PATH: src/main/java/com/ojash/workoutrec/repository/WorkoutRepo.java
package com.ojash.workoutrec.repository;

import com.ojash.workoutrec.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepo extends JpaRepository<Workout, Long> {
}