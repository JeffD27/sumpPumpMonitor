import sqlite3

class Database:
    _connection = None

    @staticmethod
    def get_connection(databaseName = "device_settings.db"):
        if Database._connection is None:
            Database._connection = sqlite3.connect(databaseName)
        return Database._connection
