#!/usr/bin/python3.11
# import main Flask class and request object

from flask import Flask, render_template, request, url_for, jsonify, abort
from getData import getData
import threading
from time import sleep
import queue
import json
import sys
import subprocess as sp 
import firebase_admin
from firebase_admin import credentials, messaging
from TokenDatabaseHandler import TokenDataBaseHandler
import logging
import sqlite3
from settingsDatabase import Database

logging.basicConfig(filename='flask2.log', level=logging.DEBUG)



#note: DON'T FORGET TO RUN PUMPCONTROL.PY.
 
#https://www.java.com/en/download/help/linux_x64_install.html#install
#https://www.baeldung.com/java-home-on-windows-mac-os-x-linux

# create the Flask app
app = Flask(__name__)
dataObj = ""
cred = credentials.Certificate("sumppumpmonitor-6e852-firebase-adminsdk-pt2wb-009a409c70.json")
firebase_admin.initialize_app(cred)
print("Firebase Initialized!")
@app.route('/',methods=['GET'])
def get(first_run = True):
    print("flask Inititializing")
    if request.method == 'GET':
        print('server get request')
        args = request.values
        arg_dict = args.to_dict()
        print(arg_dict, "AAAAAARRRRRG")
        if "firstRun" in arg_dict.keys():
            if arg_dict["firstRun"] == "true":
                print("\nfirst run is true\n\!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n") 
                first_run = True
                
            elif arg_dict["firstRun"] == "false":
                print("first run is false!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n!")
                first_run = False
                
            else:
                print('yeah nice try')
  
        if first_run: #let's get this working NEXT
          
            print("firstRun geting data")
            dataObj = getData() #This doesn't do shit at the moment
            #lock = threading.Lock()
            print("PARSE DATA LOOP WILL BE CALLED------------------------------------------------------------------------------------")

            data = dataObj.call_parse_data()
            
            print(data)
            print(len(data), "data size")
            #print(data.length, "this could be it++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
            if len(data) > 0: 
                return jsonify(data)
            
        
@app.route('/sendSettings', methods=['POST'])
def sendSettings():
    
    try:
        data = request.get_json()
       
        deviceID = data["deviceID"]
        if not data:
            print("No data provided")
            return jsonify({"success": False, "error": "No data provided"}), 400
        
    except Exception as e:
        logging.exception(f"Error sending notification: {e}")
        return jsonify({"success": False, "error": str(e)}), 500
    conn = Database.get_connection()
    cursor = conn.cursor()
    conn.row_factory = sqlite3.Row
    
    # Create the table if it doesn't exist
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS device_settings (
            device_id TEXT PRIMARY KEY,
            settings JSON
        );
    """)
    cursor = conn.cursor()
    cursor.execute(
        "INSERT INTO device_settings (device_id, settings) VALUES (?, ?) "
        "ON CONFLICT(device_id) DO UPDATE SET settings = excluded.settings",
        (deviceID, json.dumps(data)),
    )
    
    conn.commit()
    conn.close()
@app.route('/newToken', methods=['POST'])
def logNewToken():
    try:
        data = request.get_json()
        if not data:
            logging.error("No data provided")
            return jsonify({"success": False, "error": "No data provided"}), 400

        deviceId = data.get('deviceId')
        token = data.get('token')
        conn = Database.get_connection("device_to_token")
        cursor = conn.cursor()
        conn.row_factory = sqlite3.Row
    
        # Create the table if it doesn't exist
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS device_settings (
                device_id TEXT PRIMARY KEY,
                settings JSON
            );
        """)
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO device_settings (device_id, settings) VALUES (?, ?) "
            "ON CONFLICT(device_id) DO UPDATE SET settings = excluded.settings",
            (deviceId, json.dumps(data)),
        )
        
        conn.commit()
        conn.close()
        if not token:
            logging.error("Token is missing or empty in the request.")
            return jsonify({"success": False, "error": "Token is missing or empty"}), 400
        
        else:
            logging.debug(f"new token coming, {token}")
            t = TokenDataBaseHandler()
            t.save_token(deviceId, token)
            tokens = t.get_all_tokens()
            for token_ in tokens:
                print("this is a token", str(token_))
                with open("tokens.txt", 'w') as f:
                    f.write(str(token_))
            t.closeConnection()
            return "New Token Databased"
    except Exception as e:
        logging.exception(f"Error sending notification: {e}")
        return jsonify({"success": False, "error": str(e)}), 500
    
@app.route('/sendNotification', methods=['POST'])
def send_notification():
    try:
        logging.debug("Incoming message(((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((())))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))")
        data = request.get_json()
        if not data:
            logging.error("No data provided")
            return jsonify({"success": False, "error": "No data provided"}), 400

        title = data.get('title', 'Sump Pump Monitor')
        deviceId = data.get('deviceId')
        body = data.get('body', 'Message from Sump Pump Monitor')
        token = data.get('token')
        topic = data.get('topic')
        

        if not token and not topic:
            logging.error("Token or topic not provided")
            return jsonify({"success": False, "error": "Token or topic not provided"}), 400
        
        
        logging.debug(f"Received token: {token}")
        if not token:
            logging.error("Token is missing or empty in the request.")
            return jsonify({"success": False, "error": "Token is missing or empty"}), 400

        else:
            notification = messaging.Notification(title=title, body=body)
            message = messaging.Message(notification=notification, token=token)
            response = messaging.send(message)
            logging.info(f"Notification sent to token: {token}")
            return jsonify({"success": True, "response": response}), 200

        if topic:
            message = messaging.Message(notification=notification, topic=topic)
            response = messaging.send(message)
            logging.info(f"Notification sent to topic: {topic}")
            return jsonify({"success": True, "response": response}), 200

    except Exception as e:
        logging.exception(f"Error sending notification: {e}")
        return jsonify({"success": False, "error": str(e)}), 500

    

if __name__ == "__main__":
    # run app in debug mode on port 5000
    print("Flask Started.")
    app.run(debug=False, host="0.0.0.0", port=8080)
    print("Flask Ended ¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬")
else:
    print("else in sumpPumpApp.py")
    
    #https://jbacoy3.wixsite.com/datascience/single-post/2017/05/26/simple-tutorial-on-flask
