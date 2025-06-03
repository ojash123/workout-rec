package com.ojash.workoutrec;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Result;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.index.Indices; // Import for Indices
import org.tensorflow.types.TFloat32;
import org.tensorflow.proto.framework.DataType; // Using the import path you found to work

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RLModelTester {

    // --- Configuration (should match your Python environment) ---
    private static final int NUM_EXERCISES = 10;
    private static final int NUM_MUSCLE_GROUPS = 3; // Legs, Push, Pull
    private static final int MAX_WORKOUT_LENGTH = 6;

    private static final List<String> ACTION_DESCRIPTIONS = new ArrayList<>();
    private static final List<int[]> ACTION_SET_REPS = new ArrayList<>();
    private static int END_WORKOUT_ACTION_INDEX;

    // --- Model input/output names (UPDATED from saved_model_cli's internal tensor names) ---
    // These are the 'name:' fields from your saved_model_cli output for the serving_default signature
    private static final String INPUT_TENSOR_NAME = "inputs";
    private static final String OUTPUT_ACTOR_LOGITS_NAME = "output_0"; // Corresponds to 'output_0'
    // private static final String OUTPUT_CRITIC_VALUE_NAME = "StatefulPartitionedCall:1"; // Corresponds to 'output_1'


    private static void initializeActionMap() {
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
        System.out.println("Initialized " + ACTION_DESCRIPTIONS.size() + " actions.");
    }

    private static int currentExerciseIndex;
    private static float[][] history; // [exercise_idx][0]=last_sets, [exercise_idx][1]=last_total_reps
    private static float[] currentSessionFatigue;
    private static int daysSinceLastWorkout;
    private static final int STATE_SIZE = 1 + NUM_EXERCISES * 2 + NUM_MUSCLE_GROUPS + 1; // Should be 25 based on your CLI output

    private static final String[] EXERCISE_NAMES = {
            "Squat", "Bench Press", "Barbell Row", "Overhead Press", "Deadlift",
            "Lat Pulldown", "Leg Press", "Bicep Curl", "Tricep Pushdown", "Leg Extension"
    };
    private static final String[] EXERCISE_TYPES = {
            "compound", "compound", "compound", "compound", "compound",
            "isolation", "isolation", "isolation", "isolation", "isolation"
    };
    private static final int[] EXERCISE_MUSCLE_GROUP_IDX = {
            0, 1, 2, 1, 2, 2, 0, 2, 1, 0 // 0:Legs, 1:Push, 2:Pull
    };

    private static float[] constructStateVector() {
        float[] stateVector = new float[STATE_SIZE];
        int k = 0;
        stateVector[k++] = (float) currentExerciseIndex;
        for (int i = 0; i < NUM_EXERCISES; i++) {
            stateVector[k++] = history[i][0]; // Last total sets performed
            stateVector[k++] = history[i][1]; // Last total reps achieved
        }
        for (int i = 0; i < NUM_MUSCLE_GROUPS; i++) {
            stateVector[k++] = currentSessionFatigue[i];
        }
        stateVector[k++] = (float) daysSinceLastWorkout;
        return stateVector;
    }

    private static float[] softmax(float[] logits) {
        float[] probabilities = new float[logits.length];
        float maxLogit = Float.NEGATIVE_INFINITY;
        for (float logit : logits) if (logit > maxLogit) maxLogit = logit;
        float sumExp = 0.0f;
        for (int i = 0; i < logits.length; i++) {
            probabilities[i] = (float) Math.exp(logits[i] - maxLogit);
            sumExp += probabilities[i];
        }
        if (sumExp == 0) { // Avoid division by zero if all exps are tiny
            Arrays.fill(probabilities, 1.0f / logits.length); // Uniform distribution
            return probabilities;
        }
        for (int i = 0; i < logits.length; i++) probabilities[i] /= sumExp;
        return probabilities;
    }

    private static int selectAction(float[] probabilities) {
        int bestAction = 0;
        float maxProb = -1.0f; // Initialize with a value lower than any possible probability
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                bestAction = i;
            }
        }
        return bestAction;
    }

    private static void updateFatigue(int setsPerformed, String exerciseType, int muscleGroupIndex) {
        float fatiguePerSet = "compound".equals(exerciseType) ? 1.5f : 0.75f;
        currentSessionFatigue[muscleGroupIndex] += setsPerformed * fatiguePerSet;
    }

    public static void main(String[] args) {
        initializeActionMap();
        Scanner scanner = new Scanner(System.in);

        // Ensure STATE_SIZE matches the model's expected input shape (e.g., 25 from your CLI output)
        if (STATE_SIZE != 25) {
            System.err.println("Warning: STATE_SIZE (" + STATE_SIZE + ") in Java code does not match model expected input dimension (25 from CLI). Please verify.");
        }


        String modelPath = RLModelTester.class.getClassLoader().getResource("rl_workout_saved_model").getPath();
        if (System.getProperty("os.name").toLowerCase().startsWith("windows") && modelPath.startsWith("/")) {
            modelPath = modelPath.substring(1);
        }

        try (SavedModelBundle bundle = SavedModelBundle.load(modelPath, "serve")) {
            Session session = bundle.session();
            System.out.println("TensorFlow SavedModel loaded successfully!");

            // Initialize User State (Example)
            currentExerciseIndex = 0;
            history = new float[NUM_EXERCISES][2]; // All zeros initially
            currentSessionFatigue = new float[NUM_MUSCLE_GROUPS]; // All zeros
            System.out.print("Enter days since last workout: ");
            daysSinceLastWorkout = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.println("\n--- Starting Workout Recommendation ---");

            for (int step = 0; step < MAX_WORKOUT_LENGTH; step++) {
                if (currentExerciseIndex >= NUM_EXERCISES) {
                    System.out.println("Error: Exercise index out of bounds during recommendation.");
                    break;
                }

                float[] stateVector = constructStateVector();

                try (Tensor inputTensor = TFloat32.tensorOf(Shape.of(1, STATE_SIZE), DataBuffers.of(stateVector))) {

                    Result result = session.runner()
                            .feed(INPUT_TENSOR_NAME, inputTensor) // Uses the updated INPUT_TENSOR_NAME
                            .fetch(OUTPUT_ACTOR_LOGITS_NAME)    // Uses the updated OUTPUT_ACTOR_LOGITS_NAME
                            // If you need the critic value too: .fetch("output_1")
                            .run();

                    try (Tensor actionLogitsTensorUntyped = result.get(OUTPUT_ACTOR_LOGITS_NAME).orElseThrow(
                            () -> new IllegalStateException("Output tensor '" + OUTPUT_ACTOR_LOGITS_NAME + "' not found.")
                    )) {

                        if (actionLogitsTensorUntyped.dataType() != DataType.DT_FLOAT) {
                            throw new IllegalStateException("Expected DataType.DT_FLOAT for action logits, got " + actionLogitsTensorUntyped.dataType());
                        }
                        FloatNdArray logitsNdArray = (TFloat32) actionLogitsTensorUntyped;


                        if (logitsNdArray.shape().numDimensions() != 2 || logitsNdArray.shape().size(0) != 1 ||
                                logitsNdArray.shape().size(1) != ACTION_DESCRIPTIONS.size()) {
                            throw new IllegalStateException("Unexpected shape for action logits tensor: " + logitsNdArray.shape() +
                                    ", expected: [1, " + ACTION_DESCRIPTIONS.size() + "], " +
                                    "Actual output shape from model: " + logitsNdArray.shape().size(1));
                        }

                        float[] outputLogits = new float[ACTION_DESCRIPTIONS.size()];
                        FloatNdArray firstRowLogits = logitsNdArray.slice(Indices.at(0));
                        firstRowLogits.read(DataBuffers.of(outputLogits));


                        float[] actionProbabilities = softmax(outputLogits);
                        int chosenActionId = selectAction(actionProbabilities);

                        System.out.println("\n--- Step " + (step + 1) + " ---");
                        System.out.println("Recommended Action ID: " + chosenActionId);

                        if (chosenActionId == END_WORKOUT_ACTION_INDEX) {
                            System.out.println("Agent recommends: End Workout");
                            break;
                        } else {
                            if (currentExerciseIndex >= EXERCISE_NAMES.length) { // Safety check
                                System.out.println("Error: currentExerciseIndex out of bounds for EXERCISE_NAMES.");
                                break;
                            }
                            String exerciseName = EXERCISE_NAMES[currentExerciseIndex];
                            String exerciseType = EXERCISE_TYPES[currentExerciseIndex];
                            int muscleGroupIdx = EXERCISE_MUSCLE_GROUP_IDX[currentExerciseIndex];
                            int[] setRepTarget = ACTION_SET_REPS.get(chosenActionId);
                            int targetSets = setRepTarget[0];
                            int targetRepsPerSet = setRepTarget[1];

                            System.out.println("Agent recommends for " + exerciseName + ": " +
                                    targetSets + " sets of " + targetRepsPerSet + " reps.");

                            int actualTotalReps = 0;
                            int actualSetsPerformed = 0;
                            List<Integer> repsAchievedPerSet = new ArrayList<>();

                            for (int s = 0; s < targetSets; s++) {
                                System.out.print("  Enter reps completed for set " + (s + 1) + " (or -1 to stop exercise): ");
                                int repsThisSet = scanner.nextInt();
                                scanner.nextLine(); // Consume newline
                                if (repsThisSet < 0) {
                                    System.out.println("  User stopped exercise early.");
                                    break;
                                }
                                repsAchievedPerSet.add(repsThisSet);
                                actualTotalReps += repsThisSet;
                                actualSetsPerformed++;
                            }
                            System.out.println("  User performed: " + repsAchievedPerSet.toString());

                            // Update history and fatigue
                            if (currentExerciseIndex < history.length) { // Safety check
                                history[currentExerciseIndex][0] = (float) actualSetsPerformed;
                                history[currentExerciseIndex][1] = (float) actualTotalReps;
                                updateFatigue(actualSetsPerformed, exerciseType, muscleGroupIdx);
                            } else {
                                System.out.println("Error: currentExerciseIndex out of bounds for history update.");
                            }
                            currentExerciseIndex++;
                        }
                    } // Auto-closes actionLogitsTensorUntyped
                } // Auto-closes inputTensor
                if (currentExerciseIndex >= MAX_WORKOUT_LENGTH) {
                    System.out.println("\nMaximum workout length reached.");
                    break;
                }
            }
            System.out.println("\n--- Workout Session Finished ---");
            System.out.println("Final History (last sets, last total reps): " + Arrays.deepToString(history));
            System.out.println("Final Session Fatigue: " + Arrays.toString(currentSessionFatigue));

        } catch (Exception e) {
            System.err.println("Error during TensorFlow session or processing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}


