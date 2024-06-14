#!/usr/bin/python3

import RPi.GPIO as GPIO 
from time import sleep 
from datetime import datetime
from datetime import time

class pumpControl():
    
    main_pump_running = "No Data"
    backup_pump_running = "No data"                  
    def __init__(self):
        sleep(5)
        print("relay started")
        print("\n Pump Control is running. This should not be on the server! \n<<<<<<<<<>>>>>>>>>\nIf you are seeing this message on the server, please spend 30 hours troubleshooting!")
        GPIO.setwarnings(False) 
        GPIO.setmode(GPIO.BCM) 

        pins = [4, 22, 26, 6]
        for pin in pins:
                GPIO.setup(pin, GPIO.OUT)
        GPIO.setup(19, GPIO.IN)
        GPIO.setup(18, GPIO.IN)
        GPIO.setup(4, GPIO.OUT)
        
        GPIO.setup(12, GPIO.OUT)
        while True:
            main_float = GPIO.input(19)
            backup_float = GPIO.input(18)
            self.checkFloats_RunPumps(main_float, backup_float)
            self.writeToDataFile()
            sleep(.2)

    def checkFloats_RunPumps(self, main_float, backup_float):
       
                
        print(main_float, backup_float, "MAIN<<<<BACKUP>>>>>>>>>")
        if main_float == 0:
            GPIO.output(12, GPIO.HIGH)
            self.main_pump_running = True
            print("main ACTIVATED!!!!!!!!!!!!!!!!!")
        else:
            GPIO.output(12, GPIO.LOW)
            self.main_pump_running = False
            print("NOT ACTIVATED on Main")
        if backup_float  == 0:
            print("backup ACTIVATED!!!!!!!!!!!!!!!!!")
            GPIO.output(4, GPIO.HIGH)
            self.backup_pump_running = True

        else:
            GPIO.output(4, GPIO.LOW)
            self.backup_pump_running = False
            print("NOT ACTIVATED on Backup")

           
        
    def writeToDataFile(self):
        with open("/home/sp_server_beta/sp_monitor_beta4/pumpData.txt","w") as f:
            relayData = {"mainRunning": self.main_pump_running, "backupRunning": self.backup_pump_running, "Time!:": datetime.now()}
            f.write(str(relayData))


        #pumpRunningDict = {"mainRunning": main_pump_running, "backupRunning": backup_pump_running}
        #self.pumpDataSetter(pumpRunningDict)

    
if __name__ == '__main__':
    pc = pumpControl()