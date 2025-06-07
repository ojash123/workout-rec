// PATH: src/main/java/com/ojash/workoutrec/entity/PerformedSet.java
package com.ojash.workoutrec.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "performed_sets")
public class PerformedSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to PerformedExercise
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_exercise_id", nullable = false)
    private PerformedExercise performedExercise;

    @Column(nullable = false)
    private int setNumber;

    private int targetReps;
    private int actualReps;
    private float weightUsed;
}