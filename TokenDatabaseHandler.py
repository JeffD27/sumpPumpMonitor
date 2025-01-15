import sqlite3
from datetime import datetime

class TokenDataBaseHandler:
    #handles the tokens for firebase messaging (sending notifications through the server to the phone directly)

    def __init__(self):
                
        # Connect to the database
        self.conn = sqlite3.connect('tokens.db')  # Persisted in a file
        self.cursor = self.conn.cursor()

        # Create the tokens table
        self.cursor.execute("""
        CREATE TABLE IF NOT EXISTS tokens (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            device_id TEXT,
            token TEXT UNIQUE,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )
        """)
        self.conn.commit()
        tokens = self.get_all_tokens()
        print("Tokens in database:", tokens)
    

        # Save or update a token
    def save_token(self, device_id, token):
        self.cursor.execute("""
        INSERT OR REPLACE INTO tokens (device_id, token) 
        VALUES (?, ?)
        """, (device_id, token))
        self.conn.commit()
        print("Token saved or updated successfully!")
    def get_token_from_device(self, deviceId):
        self.cursor.execute("""
        SELECT token FROM tokens WHERE device_id = ?
        """, (deviceId,))
        
        # Fetch the first row (since device_id is unique)
        result = self.cursor.fetchone()
        
        if result:
            return result[0]  # Return the token (the first column in the result)
        else:
            print(f"No token found for device_id: {deviceId}")
            return None  # Return None if no token is found
    # Retrieve all tokens
    def get_all_tokens(self):
        self.cursor.execute("SELECT token FROM tokens")
        return [row[0] for row in self.cursor.fetchall()]
    def closeConnection(self):
        self.conn.close()
    # Example usage
    
    
    # Close the connection

    
