const form = document.getElementById('task-form');
const list = document.getElementById('task-list');
const refreshBtn = document.getElementById('refresh');
const template = document.getElementById('task-item-template');

const priorityEmoji = {
  low: '🟢',
  medium: '🟡',
  high: '🔴',
};

async function fetchTasks() {
  const res = await fetch('/api/tasks');
  const tasks = await res.json();
  renderTasks(tasks);
}

function renderTasks(tasks) {
  list.innerHTML = '';
  if (!tasks.length) {
    const empty = document.createElement('li');
    empty.textContent = 'No tasks yet. Add one above.';
    list.appendChild(empty);
    return;
  }

  tasks.forEach((task) => {
    const node = template.content.firstElementChild.cloneNode(true);
    node.dataset.id = task.id;
    node.querySelector('.task-title').textContent = task.title;

    const date = new Date(task.reminder_at);
    node.querySelector('.task-meta').textContent = `${priorityEmoji[task.priority] ?? ''} ${task.priority.toUpperCase()} • ${date.toLocaleString()}`;
    node.querySelector('.task-notes').textContent = task.notes || 'No notes';

    const toggleBtn = node.querySelector('.toggle-btn');
    toggleBtn.textContent = task.completed ? 'Mark Incomplete' : 'Mark Complete';
    if (task.completed) {
      node.classList.add('completed');
    }

    toggleBtn.addEventListener('click', async () => {
      await fetch(`/api/tasks/${task.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ completed: !task.completed }),
      });
      fetchTasks();
    });

    node.querySelector('.delete-btn').addEventListener('click', async () => {
      await fetch(`/api/tasks/${task.id}`, { method: 'DELETE' });
      fetchTasks();
    });

    list.appendChild(node);
  });
}

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  const payload = {
    title: document.getElementById('title').value,
    reminder_at: document.getElementById('reminder_at').value,
    priority: document.getElementById('priority').value,
    notes: document.getElementById('notes').value,
  };

  await fetch('/api/tasks', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  form.reset();
  fetchTasks();
});

refreshBtn.addEventListener('click', fetchTasks);
fetchTasks();
