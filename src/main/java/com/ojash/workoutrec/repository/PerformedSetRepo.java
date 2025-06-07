// PATH: src/main/java/com/ojash/workoutrec/repository/PerformedSetRepo.java
package com.ojash.workoutrec.repository;

import com.ojash.workoutrec.entity.PerformedSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformedSetRepo extends JpaRepository<PerformedSet, Long> {
}