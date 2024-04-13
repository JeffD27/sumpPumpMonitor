#!/usr/bin/python3.11

from time import sleep 
from datetime import *
import re
import sys
class parseDataFiles():
      
       relayData = {}
      
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
              self.writeTimeStamp()
              print(self.main_run_warning, 'main Run waring')
              print(self.backup_run_warning, 'backup run warning')
              
              self.all_data = {
                     "mainRunning": self.mainRunning, 
                     "backupRunning": self.backupRunning,
                     "timeStartedMain": self.timeStartedMain,
                     "timeStartedBackup": self.timeStartedBackup,
                     "main_run_time": self.timeRunningMainStr,
                     "backup_run_time": self.timeRunningBackupStr,
                     "main_run_warning": self.main_run_warning,
                     "backup_run_warning": self.backup_run_warning,
                     
                     }
              return self.all_data

       def writeTimeStamp(self):
              if self.timeStartedMain is not None and self.timeStartedBackup is not None:
                     with open("timeStamps.txt", 'w') as f:
                            print("saving Time started = %s" %self.timeStartedBackup,"/n(((((((((((((((((((((((((((()))))))))))))))))))))))))))))")
                            f.write(str({'MainRunning': self.timeStartedMain, 'BackupRunning': self.timeStartedBackup}))
                     with open("previouslyRunning.txt", 'w') as f: #this is to know if the pumps were running so we can update start times
                            print("saveing previously run")
                            f.write(str({'MainRunning': self.mainRunning, 'BackupRunning': self.backupRunning}))
                     
       def readDataFiles(self ):
              
              with open ("timeStamps.txt", 'r') as f:
                     
                     data = f.read()
                     
                     print(data, 'data%^')
                     
                     
                     
                     mainPrevTimeStampRe = re.search(r"MainRunning\'\s*:\s*datetime\.datetime\((\d{4})\s*\,\s*(\d{1,2})\s*,\s*(\d{1,2})\s*\,\s*(\d{1,2})\s*\,\s*(\d{1,2})\s*\,\s*(\d{1,2})", data) #creaates 6 capturing groups, 1 = yr, 2= month, 3= dayOfMonth, 4=hour, 5= min, 6 =sec

                     self.main_date_dict = {}
                     convert_dict = {1: "Year", 2: "Month", 3: "Day_of_Mo", 4: "Hour", 5: "Minute", 6: "Second"}  
                     random_starter_data = (2024,1,1,1,1,1 )
                     for i in range(1,7):
                            try:
                                   print(mainPrevTimeStampRe.group(i), "mainPRevTimeStamp")
                                   self.main_date_dict[convert_dict[i]] = int(mainPrevTimeStampRe.group(i) )#should be: "Year" : 2024 etc.
                            except Exception as e:
                                   print("%s IN Read data\n    +++++++++++++" %e)
                                   
                                   print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n\n\n\!!!!!!!!!!!!!!!!!\n\n\n","data=%s"% data,"\n~~~~~~~~~~~~~~~~~~~~~~~~~~","i =",i)
                                   if i == 1:
                                          self.main_date_dict[convert_dict[i]] = int(datetime.now().year) 
                                   elif i == 2:
                                          self.main_date_dict[convert_dict[i]] = int(datetime.now().month)
                                   elif i == 3:
                                          self.main_date_dict[convert_dict[i]] = int(datetime.now().day)
                                   elif i == 4:
                                          self.main_date_dict[convert_dict[i]] = int(datetime.now().hour)
                                   elif i == 5:
                                          self.main_date_dict[convert_dict[i]] = int(datetime.now().minute)
                                   elif i == 6:
                                          self.main_date_dict[convert_dict[i]] = int(datetime.now().second)      
                                   #self.main_date_dict[convert_dict[i]] = 69           #for testing DELETE!                  

                     backupPrevTimeStampRe = re.search(r"BackupRunning\'\s*:\s*datetime\.datetime\((\d{4})\s*\,\s*(\d{1,2})\s*,\s*(\d{1,2})\s*\,\s*(\d{1,2})\s*\,\s*(\d{1,2})\s*\,\s*(\d{1,2})", data) #creaates 6 capturing groups, 1 = yr, 2= month, 3= dayOfMonth, 4=hour, 5= min, 6 =sec
                     
                     
                     for i in range(1,7):
                            try:
                                   
                                   self.backup_date_dict[convert_dict[i]] = int(backupPrevTimeStampRe.group(i) )#should be: "Year" : 2024 etc.
                            except Exception as e:
                                   print("%s IN Read data\n    +++++++++++++" %e)
                                   
                                   
                                   print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n\n\n\!!!!!!!!!!!!!!!!!\n\n\n","data=%s"% data,"\n~~~~~~~~~~~~~~~~~~~~~~~~~~","i =",i)
                                   if i == 1:
                                          self.backup_date_dict[convert_dict[i]] = int(datetime.now().year) 
                                   elif i == 2:
                                          self.backup_date_dict[convert_dict[i]] = int(datetime.now().month)
                                   elif i == 3:
                                          self.backup_date_dict[convert_dict[i]] = int(datetime.now().day)
                                   elif i == 4:
                                          self.backup_date_dict[convert_dict[i]] = int(datetime.now().hour)
                                   elif i == 5:
                                          self.backup_date_dict[convert_dict[i]] = int(datetime.now().minute)
                                   elif i == 6:
                                          self.backup_date_dict[convert_dict[i]] = int(datetime.now().second)      
                                   #self.backup_date_dict[convert_dict[i]] = 69     #for testing DELETE!
                     print(str(self.main_date_dict), "mainDateDict")
                     print(str(self.backup_date_dict), "BackupDateDict")
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
                            
                            with open("previouslyRunning.txt", 'r') as f:
                                   prev_running_data = f.read() #if the pump just started and this var was not updated yet
                                   main_prev_run_reg = re.search("MainRunning..\s*(\w*)", prev_running_data)
                                   if main_prev_run_reg.group(1) == "False":

                                          self.timeStartedMain = datetime.now()
                           
                            self.mainRunning = True
                     
                     if backupRunningStr == "False":
                            self.backupRunning = False
                     elif backupRunningStr == "True":
                            
                            with open("previouslyRunning.txt", 'r') as f:
                                   prev_running_data = f.read() #if the pump just started and this var was not updated yet
                                   backup_prev_run_reg = re.search("BackupRunning..\s*(\w*)", prev_running_data)
                                   if backup_prev_run_reg.group(1) == "False":

                                          self.timeStartedBackup = datetime.now()
                           
                                  
                            self.backupRunning = True
                            
              
                     print(self.mainRunning, self.backupRunning, str(self.timeStartedMain), str(self.timeStartedBackup),
                           "\n~~~~~~~~~~~~~~~~~~~~~~~IN PARSE DAT READ DATA````````````````````")
       
      
                     
       def checkRunTime(self):#somehow the clock is being reset...but i'm going to do this in android
              if self.mainRunning: 
                     now = datetime.now()
                     print(self.timeStartedMain, 'time started main')
                     self.timeRunningMain = now - self.timeStartedMain 
                     print(self.timeRunningMain, "&&&&&&&&&&&&&&MAIN")
                     if self.timeRunningMain> timedelta(seconds = 30):
                            self.main_run_warning = True
                     else:
                            self.main_run_warning = False

                     self.timeRunningMainStr = str(self.timeRunningMain)
              else: self.timeRunningMainStr = "Not Running"
              if self.backupRunning:
                     now = datetime.now()
                     print(self.timeStartedBackup, 'time started backup')
                     self.timeRunningBackup = now - self.timeStartedBackup 
                     print(self.timeRunningBackup, "&&&&&&&&&&&&&&BACKUP")
                     if self.timeRunningBackup > timedelta(seconds = 2):
                            self.backup_run_warning = True
                     else:
                            self.backup_run_warning = False    

                     self.timeRunningBackupStr = str(self.timeRunningBackup)

                     #if now - self.timeStartedMain:
      
              else: self.timeRunningBackupStr = "Not Running"
      
if __name__ == '__main__':
       r = parseDataFiles()
       
