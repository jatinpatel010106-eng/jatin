import json
import sqlite3
from datetime import datetime
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlparse

BASE_DIR = Path(__file__).resolve().parent
PUBLIC_DIR = BASE_DIR / "public"
DB_PATH = BASE_DIR / "tasks.db"


def init_db():
    with sqlite3.connect(DB_PATH) as conn:
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                notes TEXT,
                reminder_at TEXT NOT NULL,
                priority TEXT NOT NULL DEFAULT 'medium',
                completed INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL
            )
            """
        )
        conn.commit()


def dict_from_row(row):
    return {
        "id": row[0],
        "title": row[1],
        "notes": row[2],
        "reminder_at": row[3],
        "priority": row[4],
        "completed": bool(row[5]),
        "created_at": row[6],
    }


class TaskHandler(BaseHTTPRequestHandler):
    def _send_json(self, payload, status=200):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _serve_file(self, file_path):
        if not file_path.exists() or not file_path.is_file():
            self.send_error(404, "File not found")
            return

        content_type = "text/plain"
        if file_path.suffix == ".html":
            content_type = "text/html"
        elif file_path.suffix == ".css":
            content_type = "text/css"
        elif file_path.suffix == ".js":
            content_type = "application/javascript"

        body = file_path.read_bytes()
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _parse_body(self):
        length = int(self.headers.get("Content-Length", 0))
        if length == 0:
            return {}
        raw_body = self.rfile.read(length).decode("utf-8")
        try:
            return json.loads(raw_body)
        except json.JSONDecodeError:
            return parse_qs(raw_body)

    def do_GET(self):
        parsed = urlparse(self.path)

        if parsed.path == "/api/tasks":
            with sqlite3.connect(DB_PATH) as conn:
                rows = conn.execute(
                    "SELECT id, title, notes, reminder_at, priority, completed, created_at FROM tasks ORDER BY reminder_at ASC"
                ).fetchall()
            self._send_json([dict_from_row(row) for row in rows])
            return

        if parsed.path == "/":
            self._serve_file(PUBLIC_DIR / "index.html")
            return

        target = (PUBLIC_DIR / parsed.path.lstrip("/")).resolve()
        if PUBLIC_DIR in target.parents or target == PUBLIC_DIR:
            self._serve_file(target)
        else:
            self.send_error(403, "Forbidden")

    def do_POST(self):
        if self.path != "/api/tasks":
            self.send_error(404)
            return

        payload = self._parse_body()
        title = (payload.get("title") or "").strip()
        reminder_at = (payload.get("reminder_at") or "").strip()
        notes = (payload.get("notes") or "").strip()
        priority = (payload.get("priority") or "medium").strip().lower()

        if not title or not reminder_at:
            self._send_json({"error": "title and reminder_at are required"}, 400)
            return

        if priority not in {"low", "medium", "high"}:
            priority = "medium"

        created_at = datetime.utcnow().isoformat()
        with sqlite3.connect(DB_PATH) as conn:
            cur = conn.execute(
                "INSERT INTO tasks (title, notes, reminder_at, priority, completed, created_at) VALUES (?, ?, ?, ?, 0, ?)",
                (title, notes, reminder_at, priority, created_at),
            )
            conn.commit()
            task_id = cur.lastrowid

            row = conn.execute(
                "SELECT id, title, notes, reminder_at, priority, completed, created_at FROM tasks WHERE id = ?",
                (task_id,),
            ).fetchone()
        self._send_json(dict_from_row(row), 201)

    def do_PUT(self):
        parsed = urlparse(self.path)
        if not parsed.path.startswith("/api/tasks/"):
            self.send_error(404)
            return

        task_id = parsed.path.removeprefix("/api/tasks/").strip()
        if not task_id.isdigit():
            self._send_json({"error": "invalid task id"}, 400)
            return

        payload = self._parse_body()
        completed = payload.get("completed")
        if not isinstance(completed, bool):
            self._send_json({"error": "completed must be boolean"}, 400)
            return

        with sqlite3.connect(DB_PATH) as conn:
            conn.execute("UPDATE tasks SET completed = ? WHERE id = ?", (1 if completed else 0, int(task_id)))
            conn.commit()
            row = conn.execute(
                "SELECT id, title, notes, reminder_at, priority, completed, created_at FROM tasks WHERE id = ?",
                (int(task_id),),
            ).fetchone()

        if row is None:
            self._send_json({"error": "task not found"}, 404)
            return

        self._send_json(dict_from_row(row))

    def do_DELETE(self):
        parsed = urlparse(self.path)
        if not parsed.path.startswith("/api/tasks/"):
            self.send_error(404)
            return

        task_id = parsed.path.removeprefix("/api/tasks/").strip()
        if not task_id.isdigit():
            self._send_json({"error": "invalid task id"}, 400)
            return

        with sqlite3.connect(DB_PATH) as conn:
            cur = conn.execute("DELETE FROM tasks WHERE id = ?", (int(task_id),))
            conn.commit()

        if cur.rowcount == 0:
            self._send_json({"error": "task not found"}, 404)
            return

        self._send_json({"status": "deleted"})


if __name__ == "__main__":
    init_db()
    server = HTTPServer(("0.0.0.0", 8000), TaskHandler)
    print("Server running at http://localhost:8000")
    server.serve_forever()
