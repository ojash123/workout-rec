// PATH: src/main/java/com/ojash/workoutrec/entity/PerformedExercise.java
package com.ojash.workoutrec.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "performed_exercises")
public class PerformedExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to Workout
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    // Relationship to the base Exercise
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Relationship to Sets
    @JsonManagedReference
    @OneToMany(mappedBy = "performedExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerformedSet> performedSets = new ArrayList<>();
}