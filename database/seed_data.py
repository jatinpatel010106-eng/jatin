"""Seed script to insert dummy users and parking records."""

from datetime import datetime, timedelta

from werkzeug.security import generate_password_hash

from mongo_config import parking_history_collection, users_collection, vehicles_collection


def run_seed():
    # Dummy users
    dummy_users = [
        {
            "username": "John",
            "email": "john@example.com",
            "password_hash": generate_password_hash("john1234"),
            "role": "user",
            "created_at": datetime.utcnow(),
        },
        {
            "username": "Sara",
            "email": "sara@example.com",
            "password_hash": generate_password_hash("sara1234"),
            "role": "user",
            "created_at": datetime.utcnow(),
        },
    ]

    for user in dummy_users:
        if not users_collection.find_one({"email": user["email"]}):
            users_collection.insert_one(user)

    # Dummy parked vehicle
    if not vehicles_collection.find_one({"vehicle_number": "DL01AB1234", "status": "parked"}):
        vehicles_collection.insert_one(
            {
                "vehicle_number": "DL01AB1234",
                "owner_name": "John",
                "vehicle_type": "Car",
                "entry_time": datetime.utcnow() - timedelta(hours=2),
                "status": "parked",
            }
        )

    # Dummy parking history
    if not parking_history_collection.find_one({"vehicle_number": "MH12XY7777"}):
        entry = datetime.utcnow() - timedelta(hours=5)
        exit_time = datetime.utcnow() - timedelta(hours=1)
        parking_history_collection.insert_one(
            {
                "vehicle_number": "MH12XY7777",
                "owner_name": "Sara",
                "vehicle_type": "Bike",
                "entry_time": entry,
                "exit_time": exit_time,
                "duration_hours": 4,
                "total_fee": 80,
            }
        )

    print("Dummy data seeded successfully.")


if __name__ == "__main__":
    run_seed()
