"""MongoDB configuration helpers for the Parking Management app."""

import os
from pymongo import MongoClient


class MongoConfig:
    """Simple configuration class to manage MongoDB connection details."""

    MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017/")
    DB_NAME = os.getenv("MONGO_DB_NAME", "parking_management")


client = MongoClient(MongoConfig.MONGO_URI)
db = client[MongoConfig.DB_NAME]

# Collections used in this project
users_collection = db["users"]
vehicles_collection = db["vehicles"]
parking_history_collection = db["parking_history"]
