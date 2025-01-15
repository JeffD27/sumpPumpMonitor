
import requests
import json
import sqlite3
from settingsDatabase import Database
import datetime
from TokenDatabaseHandler import TokenDataBaseHandler


class sendNotification:
    def __init__(self, deviceID, notifString, topic="all_users", title = "Sump Pump", message = "Default Message"):
        conn = Database.get_connection()
        cursor = conn.cursor()
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS device_settings (
            device_id TEXT PRIMARY KEY,
            settings JSON
        );
        """)
        
        # Use a parameterized query to fetch data for the specific deviceID
        cursor.execute("SELECT * FROM device_settings WHERE device_id = ?", (deviceID,))
        settings = cursor.fetchone()  # Use fetchone() since you expect a single row

        if settings:
            # Assuming the table columns are (device_id, settings), index 1 is the settings column
            self.settings = json.loads(settings[1])  # Access the settings column (e.g., JSON data)
        else:
            print(f"No settings found for deviceID: {deviceID}")
            default_settings = {
            'noPower': {'timeToMute': 1, 'unit': 'hours'},
            'serverError': {'timeToMute': 1, 'unit': 'hours'},
            'highWater': {'timeToMute': 2, 'unit': 'hours'},
            'backupRun': {'timeToMute': 10, 'unit': 'minutes'},
            'noWater': {'timeToMute': 1, 'unit': 'hours'}
            }
            self.settings = default_settings

         # Insert default values into the database for the new deviceID
            cursor.execute(
                "INSERT INTO device_settings (device_id, settings) VALUES (?, ?)",
                (deviceID, json.dumps(default_settings))  # Serialize the default settings to JSON
            )
            
            conn.commit()  # Commit the changes to save to the database
            # Assign the default settings to the instance variable
        
        if self.analyzeTimeDiff(notifString): #returns true if it hasn't been deployed within mute duration
            td = TokenDataBaseHandler()
            token = td.get_token_from_device(deviceID)
            self.buildNotification(message, title, topic, token)
            self.sendNotification()


    def analyzeTimeDiff(self, notifString):

        if notifString not in self.settings:
            print(f"Notification string '{notifString}' not found in settings.")
            return False
        conn = Database.get_connection("notification_time_log")
        cursor = conn.cursor()
        
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS notification_time_log (
            notification TEXT PRIMARY KEY,
            timeStamp DATETIME
        );
        """)

        cursor.execute("SELECT timeStamp FROM notification_time_log WHERE notification = ?", (notifString,))
        timeStamp = cursor.fetchone()  
        print(timeStamp, "TimeStamp")
        

        allowedMuteDurationINT = self.settings[notifString]["timeToMute"]
        unit = self.settings[notifString]["unit"].lower()
        print(unit, "unit")
        if unit == "minutes":
            allowedMuteDuration = datetime.timedelta(minutes = allowedMuteDurationINT)
        elif unit == "hours":
            allowedMuteDuration = datetime.timedelta(hours = allowedMuteDurationINT)
        print(allowedMuteDuration, "allowedMuteDuration")
        if timeStamp:
            last_time = datetime.datetime.strptime(timeStamp[0], "%Y-%m-%d %H:%M:%S")
            if datetime.datetime.now() - last_time < allowedMuteDuration:
              print("returning false")
              return False

        print("time check passes")
        newTimeStamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        cursor.execute("INSERT OR REPLACE INTO notification_time_log (notification, timeStamp) VALUES (?, ?)", (notifString, newTimeStamp))

        conn.commit()
        conn.close()

        return True
           



        


    def  buildNotification(self, message, title, topic, token):

        # Define the URL of your endpoint
        self.url = "http://sumppump.jeffs-handyman.net/sendNotification"

        # Define the headers
        self.headers = {
            "Content-Type": "application/json"
        }

        # Define the data payload
        self.data = {
            "topic": topic,
            "title": title,
            "body": message,
            "token": token
        }
    def sendNotification(self):
        print("sending notification")
        # Send the POST request
        response = requests.post(url=self.url, headers=self.headers, data=json.dumps(self.data))

        # Print the response
        print("Status Code:", response.status_code)
        print("Response Body:", response.json())

