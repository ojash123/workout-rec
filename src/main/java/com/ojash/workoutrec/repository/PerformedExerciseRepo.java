// PATH: src/main/java/com/ojash/workoutrec/repository/PerformedExerciseRepo.java
package com.ojash.workoutrec.repository;

import com.ojash.workoutrec.entity.PerformedExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformedExerciseRepo extends JpaRepository<PerformedExercise, Long> {
}