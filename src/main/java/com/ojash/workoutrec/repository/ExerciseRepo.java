// PATH: src/main/java/com/ojash/workoutrec/repository/ExerciseRepo.java
package com.ojash.workoutrec.repository;

import com.ojash.workoutrec.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExerciseRepo extends JpaRepository<Exercise, Long> {
    Optional<Exercise> findByName(String name);
}