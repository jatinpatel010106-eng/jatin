from datetime import datetime
from math import ceil

from bson.objectid import ObjectId
from flask import Flask, jsonify, redirect, render_template, request, session, url_for
from werkzeug.security import check_password_hash, generate_password_hash

from database.mongo_config import (
    parking_history_collection,
    users_collection,
    vehicles_collection,
)

app = Flask(__name__)
app.secret_key = "replace_this_with_a_secure_secret_key"

# Hourly rates in INR by vehicle type
PARKING_RATES = {
    "Car": 50,
    "Bike": 20,
    "Truck": 80,
}


# ---------- Utility helpers ----------
def calculate_fee(vehicle_type: str, entry_time: datetime, exit_time: datetime) -> tuple[int, float]:
    """Calculate parked hours (rounded up) and fee."""
    duration_seconds = max((exit_time - entry_time).total_seconds(), 0)
    hours = max(1, ceil(duration_seconds / 3600))
    rate = PARKING_RATES.get(vehicle_type, 30)
    return hours, float(hours * rate)


def current_user():
    """Return the currently logged in user document, if any."""
    user_id = session.get("user_id")
    if not user_id:
        return None
    return users_collection.find_one({"_id": ObjectId(user_id)})


def login_required():
    """Simple guard for endpoints that require a logged in user."""
    if "user_id" not in session:
        return jsonify({"error": "Please login first."}), 401
    return None


# ---------- Page routes ----------
@app.route("/")
def home_page():
    return render_template("index.html")


@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "GET":
        return render_template("login.html")

    data = request.get_json(silent=True) or request.form
    email = (data.get("email") or "").strip().lower()
    password = (data.get("password") or "").strip()

    if not email or not password:
        return jsonify({"error": "Email and password are required."}), 400

    user = users_collection.find_one({"email": email})
    if not user or not check_password_hash(user.get("password_hash", ""), password):
        return jsonify({"error": "Invalid email or password."}), 401

    session["user_id"] = str(user["_id"])
    session["username"] = user["username"]
    session["role"] = user.get("role", "user")

    return jsonify(
        {
            "message": "Login successful.",
            "role": user.get("role", "user"),
            "redirect": url_for("dashboard"),
        }
    )


@app.route("/register", methods=["GET", "POST"])
def register():
    if request.method == "GET":
        return render_template("register.html")

    data = request.get_json(silent=True) or request.form
    username = (data.get("username") or "").strip()
    email = (data.get("email") or "").strip().lower()
    password = (data.get("password") or "").strip()

    if not username or not email or not password:
        return jsonify({"error": "All fields are required."}), 400

    if len(password) < 6:
        return jsonify({"error": "Password must be at least 6 characters."}), 400

    if users_collection.find_one({"email": email}):
        return jsonify({"error": "Email already registered."}), 409

    user_data = {
        "username": username,
        "email": email,
        "password_hash": generate_password_hash(password),
        "role": "user",
        "created_at": datetime.utcnow(),
    }
    users_collection.insert_one(user_data)

    return jsonify({"message": "Registration successful. Please login."}), 201


@app.route("/dashboard")
def dashboard():
    if "user_id" not in session:
        return redirect(url_for("login"))

    return render_template(
        "dashboard.html",
        username=session.get("username", "User"),
        role=session.get("role", "user"),
    )


@app.route("/logout")
def logout():
    session.clear()
    return redirect(url_for("home_page"))


# ---------- Parking API routes ----------
@app.route("/add_vehicle", methods=["POST"])
def add_vehicle():
    auth_error = login_required()
    if auth_error:
        return auth_error

    data = request.get_json(silent=True) or request.form
    owner_name = (data.get("owner_name") or "").strip()
    vehicle_number = (data.get("vehicle_number") or "").strip().upper()
    vehicle_type = (data.get("vehicle_type") or "").strip().title()

    if not owner_name or not vehicle_number or vehicle_type not in PARKING_RATES:
        return jsonify({"error": "Provide valid owner name, number, and type."}), 400

    if vehicles_collection.find_one({"vehicle_number": vehicle_number, "status": "parked"}):
        return jsonify({"error": "Vehicle is already parked."}), 409

    vehicle_doc = {
        "vehicle_number": vehicle_number,
        "owner_name": owner_name,
        "vehicle_type": vehicle_type,
        "entry_time": datetime.utcnow(),
        "status": "parked",
        "created_by": session.get("username"),
    }

    result = vehicles_collection.insert_one(vehicle_doc)

    return jsonify(
        {
            "message": "Vehicle entry added.",
            "vehicle_id": str(result.inserted_id),
            "entry_time": vehicle_doc["entry_time"].isoformat(),
        }
    ), 201


@app.route("/exit_vehicle", methods=["POST"])
def exit_vehicle():
    auth_error = login_required()
    if auth_error:
        return auth_error

    data = request.get_json(silent=True) or request.form
    vehicle_number = (data.get("vehicle_number") or "").strip().upper()

    if not vehicle_number:
        return jsonify({"error": "Vehicle number is required."}), 400

    vehicle = vehicles_collection.find_one({"vehicle_number": vehicle_number, "status": "parked"})
    if not vehicle:
        return jsonify({"error": "No parked vehicle found with this number."}), 404

    entry_time = vehicle.get("entry_time", datetime.utcnow())
    exit_time = datetime.utcnow()
    duration_hours, fee = calculate_fee(vehicle.get("vehicle_type", "Car"), entry_time, exit_time)

    vehicles_collection.update_one(
        {"_id": vehicle["_id"]},
        {
            "$set": {
                "status": "exited",
                "exit_time": exit_time,
                "duration_hours": duration_hours,
                "total_fee": fee,
            }
        },
    )

    parking_history_collection.insert_one(
        {
            "vehicle_number": vehicle_number,
            "owner_name": vehicle.get("owner_name"),
            "vehicle_type": vehicle.get("vehicle_type"),
            "entry_time": entry_time,
            "exit_time": exit_time,
            "duration_hours": duration_hours,
            "total_fee": fee,
            "processed_by": session.get("username"),
        }
    )

    return jsonify(
        {
            "message": "Vehicle exited successfully.",
            "duration_hours": duration_hours,
            "total_fee": fee,
        }
    )


@app.route("/vehicles", methods=["GET"])
def list_vehicles():
    auth_error = login_required()
    if auth_error:
        return auth_error

    query_number = (request.args.get("number") or "").strip().upper()
    filter_query = {"status": "parked"}

    if query_number:
        filter_query["vehicle_number"] = {"$regex": query_number, "$options": "i"}

    vehicles = list(vehicles_collection.find(filter_query).sort("entry_time", 1))
    response = []
    for vehicle in vehicles:
        response.append(
            {
                "id": str(vehicle["_id"]),
                "vehicle_number": vehicle.get("vehicle_number"),
                "owner_name": vehicle.get("owner_name"),
                "vehicle_type": vehicle.get("vehicle_type"),
                "entry_time": vehicle.get("entry_time").isoformat() if vehicle.get("entry_time") else None,
                "status": vehicle.get("status"),
            }
        )

    return jsonify(response)


@app.route("/vehicles/<vehicle_id>", methods=["DELETE"])
def delete_vehicle(vehicle_id):
    auth_error = login_required()
    if auth_error:
        return auth_error

    try:
        result = vehicles_collection.delete_one({"_id": ObjectId(vehicle_id)})
    except Exception:
        return jsonify({"error": "Invalid vehicle ID."}), 400

    if result.deleted_count == 0:
        return jsonify({"error": "Vehicle record not found."}), 404

    return jsonify({"message": "Vehicle record deleted."})


# ---------- Admin API routes ----------
@app.route("/admin/users", methods=["GET"])
def admin_users():
    auth_error = login_required()
    if auth_error:
        return auth_error

    if session.get("role") != "admin":
        return jsonify({"error": "Admin access only."}), 403

    users = list(users_collection.find({}, {"password_hash": 0}).sort("created_at", -1))
    formatted_users = [
        {
            "id": str(user["_id"]),
            "username": user.get("username"),
            "email": user.get("email"),
            "role": user.get("role", "user"),
        }
        for user in users
    ]

    return jsonify(formatted_users)


@app.route("/admin/history", methods=["GET"])
def admin_history():
    auth_error = login_required()
    if auth_error:
        return auth_error

    if session.get("role") != "admin":
        return jsonify({"error": "Admin access only."}), 403

    history = list(parking_history_collection.find({}).sort("exit_time", -1))
    formatted_history = []
    total_earnings = 0.0

    for item in history:
        total_earnings += float(item.get("total_fee", 0))
        formatted_history.append(
            {
                "id": str(item["_id"]),
                "vehicle_number": item.get("vehicle_number"),
                "owner_name": item.get("owner_name"),
                "vehicle_type": item.get("vehicle_type"),
                "entry_time": item.get("entry_time").isoformat() if item.get("entry_time") else None,
                "exit_time": item.get("exit_time").isoformat() if item.get("exit_time") else None,
                "duration_hours": item.get("duration_hours"),
                "total_fee": item.get("total_fee"),
            }
        )

    return jsonify({"total_earnings": round(total_earnings, 2), "history": formatted_history})


# ---------- Startup ----------
def bootstrap_indexes():
    """Create useful indexes and ensure an admin account exists."""
    users_collection.create_index("email", unique=True)
    vehicles_collection.create_index([("vehicle_number", 1), ("status", 1)])
    parking_history_collection.create_index("vehicle_number")

    admin_email = "admin@parking.com"
    if not users_collection.find_one({"email": admin_email}):
        users_collection.insert_one(
            {
                "username": "Admin",
                "email": admin_email,
                "password_hash": generate_password_hash("admin123"),
                "role": "admin",
                "created_at": datetime.utcnow(),
            }
        )


if __name__ == "__main__":
    bootstrap_indexes()
    app.run(debug=True)
