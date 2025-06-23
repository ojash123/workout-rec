// PATH: src/main/resources/static/js/workout.js
document.addEventListener('DOMContentLoaded', () => {
    const startBtn = document.getElementById('start-workout-btn');
    const workoutLog = document.getElementById('workout-log');

    let currentWorkoutId = null;

    // 1. Start the workout
    startBtn.addEventListener('click', async () => {
        try {
            const response = await fetch('/api/workout/start', { method: 'POST' });
            if (!response.ok) throw new Error('Failed to start workout');

            const workout = await response.json();
            currentWorkoutId = workout.id;

            console.log(`Workout started with ID: ${currentWorkoutId}`);
            startBtn.style.display = 'none'; // Hide the start button

            fetchNextRecommendation();
        } catch (error) {
            console.error('Error starting workout:', error);
            alert('Could not start workout. Please try again.');
        }
    });

    // 2. Fetch the next recommendation from the API
    async function fetchNextRecommendation() {
        if (!currentWorkoutId) {
            alert('Cannot get recommendation without an active workout.');
            return;
        }
        try {
            // UPDATED: Pass the currentWorkoutId in the URL
            const response = await fetch(`/api/workout/${currentWorkoutId}/recommendation`);
            if (!response.ok) throw new Error('Failed to get recommendation');

            const recommendation = await response.json();
            displayRecommendation(recommendation);
        } catch (error) {
            console.error('Error fetching recommendation:', error);
            alert('Could not get next recommendation.');
        }
    }


    // 3. Display the recommendation in a new card
    function displayRecommendation(rec) {
        if (rec.endWorkout) {
            workoutLog.innerHTML += `<div class="alert alert-success">Great job! The model recommends ending the workout now. Redirecting to dashboard...</div>`;

            // Redirect back to the dashboard after a short delay
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 3000); // 3-second delay

            return;
        }

        const exerciseCard = document.createElement('div');
        exerciseCard.className = 'exercise-card';
        exerciseCard.id = `exercise-${rec.exerciseName.replace(/\s+/g, '-')}`;

        let formHtml = '';
        for (let i = 1; i <= rec.targetSets; i++) {
            formHtml += `
                <div class="row mb-2 align-items-center">
                    <div class="col-md-2"><strong>Set ${i}</strong></div>
                    <div class="col-md-5">
                        <label for="reps-set-${i}" class="form-label visually-hidden">Reps</label>
                        <input type="number" class="form-control" placeholder="Actual Reps (Target: ${rec.targetReps})" data-set-number="${i}" name="reps">
                    </div>
                    <div class="col-md-5">
                        <label for="weight-set-${i}" class="form-label visually-hidden">Weight</label>
                        <input type="number" step="0.5" class="form-control" placeholder="Weight (kg/lbs)" name="weight">
                    </div>
                </div>
            `;
        }

        exerciseCard.innerHTML = `
            <div class="card-header"><h3>${rec.exerciseName}</h3></div>
            <div class="card-body">
                <p class="card-text">Recommended: <strong>${rec.targetSets} sets of ${rec.targetReps} reps</strong></p>
                <form data-exercise-name="${rec.exerciseName}">
                    ${formHtml}
                    <button type="submit" class="btn btn-success mt-3">Log Exercise</button>
                </form>
            </div>
        `;

        workoutLog.appendChild(exerciseCard);
        exerciseCard.querySelector('form').addEventListener('submit', handleExerciseSubmit);
    }

    // 4. Handle the submission of a completed exercise
    async function handleExerciseSubmit(event) {
        event.preventDefault();
        const form = event.target;
        const exerciseName = form.dataset.exerciseName;
        const exerciseCard = form.closest('.exercise-card');

        try {
            // First, create the PerformedExercise record to get its ID
            const exerciseResponse = await fetch(`/api/workout/${currentWorkoutId}/record-exercise?exerciseName=${encodeURIComponent(exerciseName)}`, { method: 'POST' });
            if (!exerciseResponse.ok) throw new Error('Failed to record exercise.');
            const performedExercise = await exerciseResponse.json();

            // Collect set data from the form
            const sets = [];
            const repInputs = form.querySelectorAll('input[name="reps"]');
            const weightInputs = form.querySelectorAll('input[name="weight"]');

            repInputs.forEach((repInput, index) => {
                if (repInput.value) { // Only submit sets that have been filled out
                    sets.push({
                        setNumber: parseInt(repInput.dataset.setNumber, 10),
                        actualReps: parseInt(repInput.value, 10),
                        weightUsed: parseFloat(weightInputs[index].value) || 0
                    });
                }
            });

            // Prepare the batch submission DTO
            const submission = {
                performedExerciseId: performedExercise.id,
                sets: sets
            };

            // Send the batch request to record performance
            const performanceResponse = await fetch('/api/workout/record-performance', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(submission)
            });

            if (!performanceResponse.ok) throw new Error('Failed to save workout performance.');

            // Visually update the UI
            form.style.display = 'none'; // Hide form
            exerciseCard.classList.add('completed-exercise');
            alert(`${exerciseName} logged successfully!`);

            // Fetch the next recommendation
            fetchNextRecommendation();

        } catch (error) {
            console.error('Error submitting exercise:', error);
            alert('Failed to log exercise. Please check the console and try again.');
        }
    }
});