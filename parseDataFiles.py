#!/usr/bin/python3.11

from time import sleep 
from datetime import *
import re

class parseDataFiles():
       
       relayData = {}
       mainRunning = False
       backupRunning = False
       timeStartedMain = datetime.now()
       timeStartedBackup = datetime.now()
       prevTimeStamp = ""
       pumpRunningDict = {}
       main_date_dict = {}
       backup_date_dict = {}
       main_run_warning = False
       backup_run_warning = False
       
       def run(self):
       #while True: #for testing...i'll want to delete this while loop later
              self.readDataFiles()
              self.checkRunTime()
              print(self.main_run_warning, 'main Run waring')
              print(self.backup_run_warning, 'backup run warning')
              self.all_data = {
                     "mainRunning": self.mainRunning, 
                     "backupRunning": self.backupRunning,
                     "timeStartedMain": self.timeStartedMain,
                     "timeStartedBackup": self.timeStartedBackup,
                     "main_run_warning": self.main_run_warning,
                     "backup_run_warning": self.backup_run_warning
                     }

       def readDataFiles(self):
              with open ("timeStamps.txt", 'r') as f:
                     data = f.read()
                     print(data, 'data%^')
                     mainPrevTimeStampRe = re.search(r"MainRunning\'\s*:\s*datetime\.datetime\((\d{4})\s*\,\s*(\d{1,2})\s*,\s*(\d{1,2})\s*\,\s*(\d{1,2})\s*\,\s*(\d{1,2})\s*\,\s*(\d{1,2})", data) #creaates 6 capturing groups, 1 = yr, 2= month, 3= dayOfMonth, 4=hour, 5= min, 6 =sec
                     self.main_date_dict = {}
                     convert_dict = {1: "Year", 2: "Month", 3: "Day_of_Mo", 4: "Hour", 5: "Minute", 6: "Second"}  
                     random_starter_data = (2024,1,1,1,1,1 )
                     for i in range(1,7):
                            try:
                                   print(mainPrevTimeStampRe.group(i))
                                   self.main_date_dict[convert_dict[i]] = int(mainPrevTimeStampRe.group(i) )#should be: "Year" : 2024 etc.
                            except Exception as e:
                                   print(e)
                                   self.main_date_dict[convert_dict[i]] = random_starter_data[i-1]

                     backupPrevTimeStampRe = re.search(r"BackupRunning\'\s*:\s*datetime\.datetime\((\d{4})\s*\,\s*(\d{1,2})\s*,\s*(\d{1,2})\s*\,\s*(\d{1,2})\s*\,\s*(\d{1,2})\s*\,\s*(\d{1,2})", data) #creaates 6 capturing groups, 1 = yr, 2= month, 3= dayOfMonth, 4=hour, 5= min, 6 =sec
                     
                     
                     for i in range(1,7):
                            try:
                                   print(mainPrevTimeStampRe.group(i))
                                   self.backup_date_dict[convert_dict[i]] = int(backupPrevTimeStampRe.group(i) )#should be: "Year" : 2024 etc.
                            except Exception as e:
                                   print(e)
                                   self.backup_date_dict[convert_dict[i]] = random_starter_data[i-1]
                     print(str(self.main_date_dict))
                     #print(mainPrevTimeStampRe.string, "re")
                     #print(backupPrevTimeStampRe.string)
                     self.timeStartedMain = datetime(
                            year = self.main_date_dict["Year"],
                            month = self.main_date_dict["Month"],
                            day = self.main_date_dict["Day_of_Mo"],
                            hour = self.main_date_dict["Hour"],
                            minute = self.main_date_dict["Minute"],
                            second = self.main_date_dict["Second"]
                            )
                            
                     self.timeStartedBackup = datetime(
                            year = self.backup_date_dict["Year"],
                            month = self.backup_date_dict["Month"],
                            day = self.backup_date_dict["Day_of_Mo"],
                            hour = self.backup_date_dict["Hour"],
                            minute = self.backup_date_dict["Minute"],
                            second = self.backup_date_dict["Second"]
                            )
                     self.prevTimeStamp = [mainPrevTimeStampRe, backupPrevTimeStampRe]
                     #self.timeStartedMain = 
              with open("pumpData.txt", 'r') as f:
                     data = f.read()
                     while len(data)<1:
                            print("data is empty in parse data files!")
                            data = f.read() 
                     print(data, "in parsedatafiles+++++++++++++++++++++++")
                     try:
                            mainRunningRe = re.search(r"mainRunning\'{0,1}:\s*(\w*)", data)
                     except: 
                            mainRunningRe = re.search(r"(False)", "False")
                     print(mainRunningRe.string, 'here')
                     try:
                            backupRunningRe = re.search(r"backupRunning\'{0,1}:\s*(\w*)",data)
                     except: 
                            backupRunningRe = re.search(r"(False)", "False")
                     
                     
                     try:
                            mainRunningStr = mainRunningRe.group(1)
                     except AttributeError as e:
                            print(e)
                     try:
                            backupRunningStr = backupRunningRe.group(1)
                     except AttributeError as e:
                            print(e)
                     

                     if mainRunningStr == "False":
                            self.mainRunning = False
                            
                     elif mainRunningStr == "True":
                            if self.mainRunning == False:
                                   self.timeStartedMain = datetime.now()
                            self.mainRunning = True

                     
                     if backupRunningStr == "False":
                            self.backupRunning = False
                     elif backupRunningStr == "True":
                            if self.backupRunning == False: #if the pump just started and this var was not updated yet
                                   self.timeStartedBackup = datetime.now()
                            self.backupRunning = True
              
                     print(self.mainRunning, self.backupRunning, str(self.timeStartedMain), str(self.timeStartedBackup))
              with open("timeStamps.txt", 'w') as f:
                     f.write(str({'MainRunning': self.timeStartedMain, 'BackupRunning': self.timeStartedBackup}))
                  
       def checkRunTime(self):#somehow the clock is being reset...but i'm going to do this in android
              if self.mainRunning: 
                     now = datetime.now()
                     print(self.timeStartedMain, 'time started main')
                     timeRunning = now - self.timeStartedMain 
                     print(timeRunning, "&&&&&&&&&&&&&&")
                     if timeRunning > timedelta(minutes = 10):
                            self.main_run_warning = True
                     else:
                            self.main_run_warning = False
              if self.backupRunning:
                     now = datetime.now()
                     print(self.timeStartedBackup, 'time started backup')
                     timeRunning = now - self.timeStartedBackup 
                     print(timeRunning, "&&&&&&&&&&&&&&")
                     if timeRunning > timedelta(seconds = 2):
                            self.backup_run_warning = True
                     else:
                            self.backup_run_warning = False    
                     #if now - self.timeStartedMain:
      

      
if __name__ == '__main__':
       r = parseDataFiles()
