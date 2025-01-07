import subprocess
import json
import sqlite3
import time
import matplotlib.pyplot as plt
from datetime import datetime

# SQLite setup
DATABASE_NAME = "battery_data.db"

def setup_database():
    conn = sqlite3.connect(DATABASE_NAME)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS battery_data (
            timestamp TEXT,
            voltage5 INTEGER,
            charging5 INTEGER,
            voltage12 INTEGER
        )
    ''')
    conn.commit()
    conn.close()

def insert_data(timestamp, voltage5, charging5, voltage12):
    conn = sqlite3.connect(DATABASE_NAME)
    cursor = conn.cursor()
    cursor.execute('''
        INSERT INTO battery_data (timestamp, voltage5, charging5, voltage12)
        VALUES (?, ?, ?, ?)
    ''', (timestamp, voltage5, charging5, voltage12))
    conn.commit()
    conn.close()

def get_all_data():
    conn = sqlite3.connect(DATABASE_NAME)
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM battery_data')
    rows = cursor.fetchall()
    conn.close()
    return rows

def plot_data(data):
    timestamps = [datetime.strptime(row[0], "%Y-%m-%d %H:%M:%S") for row in data]
    voltage5 = [row[1] for row in data]
    charging5 = [row[2] for row in data]
    voltage12 = [row[3] for row in data]

    plt.figure(figsize=(10, 6))
    
    plt.plot(timestamps, voltage5, label='5V Voltage', marker='o')
    plt.plot(timestamps, charging5, label='Charging 5V', marker='o')
    plt.plot(timestamps, voltage12, label='12V Voltage', marker='o')

    plt.xlabel("Time")
    plt.ylabel("Values")
    plt.title("Battery Data Over Time")
    plt.legend()
    plt.grid()
    plt.gcf().autofmt_xdate()  # Format the x-axis for timestamps
    plt.tight_layout()
    plt.show()

def main():
    setup_database()
    try:
        while True:
            # Call the battery_indicator.py script and get its output
            result = subprocess.run(
                ["python3", "battery_indicator.py"], 
                capture_output=True, 
                text=True
            )
            output = result.stdout.strip()
            
            # Parse the output as a dictionary
            data = json.loads(output)
            
            # Collect data
            timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            voltage5 = data.get("voltage5", 0)
            charging5 = data.get("charging5", 0)
            voltage12 = data.get("voltage12", 0)
            
            # Insert into database
            insert_data(timestamp, voltage5, charging5, voltage12)
            
            print(f"Inserted data: {timestamp}, {voltage5}, {charging5}, {voltage12}")
            
            time.sleep(0.5)
    except KeyboardInterrupt:
        print("Stopped data collection.")
        # Fetch and graph data when stopped
        data = get_all_data()
        if data:
            plot_data(data)
        else:
            print("No data to plot.")

if __name__ == "__main__":
    main()