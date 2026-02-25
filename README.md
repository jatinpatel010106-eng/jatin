# Daily Task Reminder Web App

A dynamic full-stack website for managing day-to-day tasks and reminders:

- **Frontend**: HTML/CSS/JavaScript (served from `public/`)
- **Backend**: Python HTTP server with REST-style API (`app.py`)
- **Database**: SQLite (`tasks.db`)

## Run locally

```bash
python3 app.py
```

Open: http://localhost:8000

## API endpoints

- `GET /api/tasks` - list tasks
- `POST /api/tasks` - create task
- `PUT /api/tasks/:id` - mark completed/incomplete
- `DELETE /api/tasks/:id` - delete task
