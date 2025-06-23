// PATH: src/main/resources/static/js/dashboard.js
document.addEventListener('DOMContentLoaded', async () => {
    const historyList = document.getElementById('history-list');

    try {
        const response = await fetch('/api/workouts');
        if (!response.ok) throw new Error('Failed to fetch workout history.');

        const workouts = await response.json();
        historyList.innerHTML = ''; // Clear "Loading..." message

        if (workouts.length === 0) {
            historyList.innerHTML = '<p>No workout history found. Start a new workout!</p>';
            return;
        }

        workouts.forEach(workout => {
            const date = new Date(workout.date).toLocaleDateString('en-US', {
                year: 'numeric', month: 'long', day: 'numeric'
            });

            const summaryText = `${workout.exerciseCount} exercises, ${workout.setCount} sets completed`;
            const workoutLink = document.createElement('a');
            workoutLink.href = `/workout/${workout.workoutId}`;
            workoutLink.className = 'list-group-item list-group-item-action';
            workoutLink.innerHTML = `
                <div class="d-flex w-100 justify-content-between">
                    <h5 class="mb-1">${date} workout</h5>
                </div>
                <p class="mb-1">${summaryText}</p>
            `;
            historyList.appendChild(workoutLink);
        });

    } catch (error) {
        console.error('Error loading history:', error);
        historyList.innerHTML = '<p class="text-danger">Could not load workout history.</p>';
    }
});