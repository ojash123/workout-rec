// PATH: src/main/resources/static/js/workout-detail.js
document.addEventListener('DOMContentLoaded', async () => {
    const workoutDate = document.getElementById('workout-date');
    const detailLog = document.getElementById('workout-detail-log');

    // Get workout ID from the URL path
    const workoutId = window.location.pathname.split('/').pop();

    try {
        const response = await fetch(`/api/workouts/${workoutId}`);
        if (!response.ok) throw new Error('Workout not found.');

        const details = await response.json();

        const date = new Date(details.date).toLocaleDateString('en-US', {
            year: 'numeric', month: 'long', day: 'numeric'
        });
        workoutDate.textContent = `Workout from ${date}`;

        details.performedExercises.forEach(exercise => {
            let setsHtml = '<ul class="list-group list-group-flush">';
            exercise.performedSets.forEach(set => {
                setsHtml += `<li class="list-group-item">Set ${set.setNumber}: ${set.actualReps} reps at ${set.weightUsed} kg/lbs</li>`;
            });
            setsHtml += '</ul>';

            detailLog.innerHTML += `
                <div class="card mb-3">
                    <div class="card-header"><h4>${exercise.exerciseName}</h4></div>
                    <div class="card-body">
                        ${setsHtml}
                        ${exercise.notes ? `<p class="mt-2">Notes: ${exercise.notes}</p>` : ''}
                    </div>
                </div>
            `;
        });

    } catch (error) {
        console.error('Error loading workout details:', error);
        workoutDate.textContent = 'Error';
        detailLog.innerHTML = `<p class="text-danger">${error.message}</p>`;
    }
});