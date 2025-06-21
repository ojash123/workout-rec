// PATH: src/main/java/com/ojash/workoutrec/service/impl/RecommendationServiceImpl.java
package com.ojash.workoutrec.service.impl;

import com.ojash.workoutrec.dto.RecommendationDto;
import com.ojash.workoutrec.entity.Workout;
import com.ojash.workoutrec.repository.WorkoutRepo;
import com.ojash.workoutrec.service.RecommendationService;
// REMOVE jakarta.annotation.PostConstruct and jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.index.Indices;
import org.tensorflow.types.TFloat32;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    // --- Model Configuration ---
    private static final int NUM_EXERCISES = 10;
    private static final int NUM_MUSCLE_GROUPS = 3;
    private static final int STATE_SIZE = 1 + NUM_EXERCISES * 2 + NUM_MUSCLE_GROUPS + 1; // 25
    private static final String INPUT_TENSOR_NAME = "serving_default_inputs:0";
    private static final String OUTPUT_ACTOR_LOGITS_NAME = "StatefulPartitionedCall:0";
    private final List<String> ACTION_DESCRIPTIONS = new ArrayList<>();
    private final List<int[]> ACTION_SET_REPS = new ArrayList<>();
    private int END_WORKOUT_ACTION_INDEX;

    private final String[] EXERCISE_NAMES = {
            "Squat", "Bench Press", "Barbell Row", "Overhead Press", "Deadlift",
            "Lat Pulldown", "Leg Press", "Bicep Curl", "Tricep Pushdown", "Leg Extension"
    };

    // The model is now a final field, injected by Spring
    private final SavedModelBundle savedModelBundle;
    private final WorkoutRepo workoutRepo; // Add WorkoutRepo to fetch workout state
    // Use constructor injection to receive the bean
    public RecommendationServiceImpl(SavedModelBundle savedModelBundle, WorkoutRepo workoutRepo) {
        this.savedModelBundle = savedModelBundle;
        this.workoutRepo = workoutRepo;
        initializeActionMap();
        System.out.println("RecommendationServiceImpl initialized with TensorFlow model.");
    }

    // The @PostConstruct and @PreDestroy methods for loadModel() and closeModel() are no longer needed here.
    // Spring manages the bean's lifecycle.

    @Override
    @Transactional(readOnly = true) // Use a transaction to lazily load exercises
    public RecommendationDto getRecommendation(Long workoutId, int daysSinceLastWorkout) {
        // Fetch the current workout from the database
        Workout currentWorkout = workoutRepo.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found with ID: " + workoutId));

        // STATEFUL LOGIC: Determine the index based on exercises already performed
        int currentExerciseIndex = currentWorkout.getPerformedExercises().size();

        // Check if the workout should end based on length
        if (currentExerciseIndex >= NUM_EXERCISES) {
            return new RecommendationDto(null, 0, 0, true);
        }

        // (Future enhancement: Build history and fatigue from the DB based on the user)
        float[][] history = new float[NUM_EXERCISES][2];
        float[] currentSessionFatigue = new float[NUM_MUSCLE_GROUPS];
        float[] stateVector = constructStateVector(currentExerciseIndex, history, currentSessionFatigue, daysSinceLastWorkout);

        // Get the session from the bundle, but DO NOT put it in a try-with-resources block.
        Session session = savedModelBundle.session();

        // ONLY manage the Tensors with try-with-resources
        try (Tensor inputTensor = TFloat32.tensorOf(Shape.of(1, STATE_SIZE), DataBuffers.of(stateVector))) {
            Result result = session.runner()
                    .feed(INPUT_TENSOR_NAME, inputTensor)
                    .fetch(OUTPUT_ACTOR_LOGITS_NAME)
                    .run();

            try (Tensor resultTensor = result.get(OUTPUT_ACTOR_LOGITS_NAME)
                    .orElseThrow(() -> new IllegalStateException("Output tensor not found."))) {

                FloatNdArray logitsNdArray = (TFloat32) resultTensor;
                float[] outputLogits = new float[ACTION_DESCRIPTIONS.size()];
                logitsNdArray.slice(Indices.at(0)).read(DataBuffers.of(outputLogits));

                float[] actionProbabilities = softmax(outputLogits);
                int chosenActionId = selectAction(actionProbabilities);

                if (chosenActionId == END_WORKOUT_ACTION_INDEX) {
                    return new RecommendationDto(null, 0, 0, true);
                } else {
                    String exerciseName = EXERCISE_NAMES[currentExerciseIndex];
                    int[] setRepTarget = ACTION_SET_REPS.get(chosenActionId);
                    return new RecommendationDto(exerciseName, setRepTarget[0], setRepTarget[1], false);
                }
            }
        }
    }

    // --- All helper methods (initializeActionMap, constructStateVector, etc.) remain the same ---
    private void initializeActionMap() {
        int minSets = 2, maxSets = 4, minReps = 6, maxReps = 15;
        for (int s = minSets; s <= maxSets; s++) {
            for (int r = minReps; r <= maxReps; r++) {
                ACTION_DESCRIPTIONS.add(s + " sets of " + r + " reps");
                ACTION_SET_REPS.add(new int[]{s, r});
            }
        }
        END_WORKOUT_ACTION_INDEX = ACTION_DESCRIPTIONS.size();
        ACTION_DESCRIPTIONS.add("End Workout");
        ACTION_SET_REPS.add(null);
    }

    private float[] constructStateVector(int currentExerciseIndex, float[][] history, float[] currentSessionFatigue, int daysSinceLastWorkout) {
        float[] stateVector = new float[STATE_SIZE];
        int k = 0;
        stateVector[k++] = (float) currentExerciseIndex;
        for (int i = 0; i < NUM_EXERCISES; i++) {
            stateVector[k++] = history[i][0];
            stateVector[k++] = history[i][1];
        }
        for (int i = 0; i < NUM_MUSCLE_GROUPS; i++) {
            stateVector[k++] = currentSessionFatigue[i];
        }
        stateVector[k++] = (float) daysSinceLastWorkout;
        return stateVector;
    }

    private float[] softmax(float[] logits) {
        float[] probabilities = new float[logits.length];
        float maxLogit = Float.NEGATIVE_INFINITY;
        for (float logit : logits) if (logit > maxLogit) maxLogit = logit;
        float sumExp = 0.0f;
        for (int i = 0; i < logits.length; i++) {
            probabilities[i] = (float) Math.exp(logits[i] - maxLogit);
            sumExp += probabilities[i];
        }
        for (int i = 0; i < logits.length; i++) probabilities[i] /= sumExp;
        return probabilities;
    }

    private int selectAction(float[] probabilities) {
        int bestAction = 0;
        float maxProb = -1.0f;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                bestAction = i;
            }
        }
        return bestAction;
    }
}