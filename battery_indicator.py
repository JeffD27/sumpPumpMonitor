from gpiozero import MCP3008 as MCP #mcp3008 is the chip that reads voltages
import math
import RPi.GPIO as GPIO
import re
from datetime import datetime 
from time import sleep
import os
#mosi input miso: output

class BatteryIndicator:
    data = ""
    errorRecoveryRunTimes = 0
    errorRecoveryOkToRun = True
    
    def __init__(self):
        
        print("BatteryIndicator is running")
        #while True:
        #print('battery indicator running')
        try:
            small = MCP(channel = 0, clock_pin =  24, miso_pin = 23, mosi_pin = 21, select_pin = 20)
            sleep(.2)
            large =  MCP(channel = 5, clock_pin =  24, miso_pin = 23, mosi_pin = 21, select_pin = 20)
        except Exception as e:
            print(e)
            runErrorRecovery()
            return None
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(27, GPIO.IN)
        charge = GPIO.input(27)
        print("charging?!", charge)
        if charge == 0:
            charge = False
        elif charge == 1:
            charge = True
        else:
            print("houston we have a problem in battery_indicator")
        trial = 0    
        try:
            trial += 1
            sm = small.value
            l = large.value
            BatteryIndicator.rawData= (sm,l, charge)
           
            print(l, "12v in battery indicator$$$$$$$$$$$$$$$$$$$$$$$$$$")
            print(sm, "5v voltage pre calc")
            sm = (sm*100)*(100/79)
            l = (l * 100)*(100/92) #92 is the actual reading at 12.2v

            #this proved my problem was on the android side...not here. I don't need this anymore
            #if sm < 50 or l < 50:
             #   with open("lowVoltageLog.txt", "a+") as file:
              #      string = "\n"+"small: "+str(sm)+" Large: "+str(l)
               #     file.write(string)


            if sm > 100:
                sm = 100
            elif sm < 0:
                print("LESS ThAN ZERO", sm)
                
                sm = 0
            
            if l > 100:
                l = 100
            elif l < 0:
                l = 0
           
            #s = math.trunc(s)
            print(sm, "5v after calc")
            print(l, "12v after calc")
            sm = f"{sm:.2f}"
            l = f"{l:.2f}"
            
            dataString = "voltage5: %s, charging5: %s, voltage12: %s" %(sm, charge, l) 
            #writeString ="voltage5: %s, voltage12: %s" %(str(sm), str(l))
            #write_line_with_limit("BatteryData.txt", dataString)
            
            lineNumber = 0
            voltage5Sum = 0
            voltage12Sum = 0

            with open("BatteryData.txt", "r") as file:
                lines = file.readlines()
                lines = [item for item in lines if item and str(item).strip()]
                linesN = len(lines)
                count = 0
                for line in lines:
                    count += 1
                    
                    
                    lineNumber += 1
                    print(line)
                    line = (line.strip())  # Use strip() to remove any leading/trailing whitespace, including newlines
                    dataTupple = regexSearch(line)
        
                    print(dataTupple, "dataTupple##")
                    oldVoltage5 = float(dataTupple[0])
                    oldVoltage12 = float(dataTupple[2])
                    print(str(oldVoltage5), "oldvoltage5")
                    print(str(oldVoltage12), "oldVoltage12")
                    if count == linesN: #last line 
                        print(str(oldVoltage5 - float(sm)),"old5-new5")
                        print(sm, "sm") 
                        print(str(oldVoltage12 - float(l)),"old12-new12 *************************************************")
                        print(sm, "sm")
                        if (abs(oldVoltage5 - float(sm))) > 35 or (abs(oldVoltage12 - float(l)) > 35):
                            print("large Voltage-Swing Error")
                            if not runErrorRecovery(): #returns false if it has been run > 3 times in the past
                                print("writing, we're done with error revcovering. the error is stable so we'll keep it!")
                                write_line_with_limit("BatteryData.txt", dataString) #write the data if it's been 3 times in a row
                                  
                                
                        else:
                            write_line_with_limit("BatteryData.txt", dataString.strip()) #write the data if it didn't drop by 35. this is what we want
                            with open("errorRecoveryRunTimes.txt", 'w') as file:  #reset the error runtime counter
                                    file.write(str("0"))
                                
                    
                    voltage5Sum += oldVoltage5
                    voltage12Sum += oldVoltage12    
                print(voltage5Sum, lineNumber, "voltage5sum, linenumber about to divide")
                if lineNumber > 0:
                    average5 = str(voltage5Sum/lineNumber)
                    average12 = str(voltage12Sum/lineNumber)
                else:
                    BatteryIndicator.data = {"voltage5": sm, "charging5": charge, "voltage12": l}
                    return None
                    
                print("average5", average5)
               
                print("average12", average12)
                averagesString = "voltage5: %s, charging5: %s voltage12: %s" %(average5, charge, average12) 

            print('writing$', averagesString)


                    
            BatteryIndicator.data = {"voltage5": average5, "charging5": charge, "voltage12": average12}
            print(BatteryIndicator.data, "data (((((((((((((((())))))))))))))))")    
        except Exception as e:
            print(e, 'error in battery indactor end')
            runErrorRecovery()

def runErrorRecovery():
    print("WARNING ERROR RECOVERY IS RUNNING DATA IS LESS VALID")
    with open("errorRecoveryRunTimes.txt", 'r') as file:
        data = file.read()
        print(data, "data")
        if data is None or data == "":
            data = "0"
        print(int(data), "read file")
        runTimes = int(data)
       
    runTimes += 1
    print(str(runTimes), "runTimes")
    with open("errorRecoveryRunTimes.txt", 'w') as file:
        file.write(str(runTimes))
    if runTimes > 3:
        runTimes = 0
        with open("errorRecoveryRunTimes.txt", 'w') as file:
            file.write(str(runTimes)) 
        print("returning false")
        return False
   
    with open("BatteryData.txt", "r") as file:
        data = file.readlines()
        data_no_blanks = [item for item in data if item and str(item).strip()]
        print(data_no_blanks)
        if data is not None:
            last_line = data_no_blanks[-1].strip()
            print(last_line)
            
                
            dataTupple = regexSearch(last_line)
            oldVoltage5 = dataTupple[0]
            oldCharging5 = dataTupple[1]
            oldVoltage12 = dataTupple[2]
            return True
        else: 
            print("NO DATA")
            return True
        
        
        
         
    BatteryIndicator.data = {"voltage5": oldVoltage5, "charging5": oldCharging5, "voltage12": oldVoltage12}
    print(BatteryIndicator.data, "data ERROR RECOVERED (((((((())))))))")
    print(oldVoltage5, oldCharging5, oldVoltage12, "in error recovery")
    return True


def write_line_with_limit(file_path, new_line, max_lines=10):
    # Read the existing lines from the file if it exists
    if os.path.exists(file_path):
        with open(file_path, 'r') as file:
            print("reading")
            lines = file.readlines()
            lines = [item for item in lines if item and str(item).strip()]
            print(lines)
    else:
        print("else in write")
        lines = []
    
    # Add the new line
    print(new_line, "new_line")
    lines.append(new_line)
    print(lines, "lines")

    # Keep only the last max_lines lines
    if len(lines) > max_lines:
        lines = lines[-max_lines:]

    # Write the updated lines back to the file
    pattern = r"voltage5:\s*(\d{1,3}\.{0,1}\d*),\s*charging5:\s*(\w{4,}),\s*voltage12:\s*(\d{1,3}\.{0,1}\d*)"
    
    
    BatteryIndicator.filtered_lines = filter_lines(lines, pattern) 
    print(BatteryIndicator.filtered_lines, "here's your problem")

    # Filter the lines
    #BatteryIndicator.filtered_lines = filter_lines(lines, pattern) #make sure we don't write the wrong format to file

    # Write only the filtered lines to the file
    with open(file_path, 'w') as file:
        file.writelines(BatteryIndicator.filtered_lines)

    print(f"Filtered lines written to {file_path}.")

# Function to filter lines based on the regex pattern
def filter_lines(lines, regex_pattern):
    print("filtering" )
    print(lines)
    newLines = []
    for line in lines:

        if re.match(regex_pattern, line):
            print("match is successful\nline", line)
            newLines.append(line.strip()+"\n")
        else:
            print("match    FAILURE -----------------------------------------------------------------------\n((((((((((((((((()))))))))))))))))")
            
    return newLines


    

def regexSearch(string):
    print(string, "string in regex search")
    pattern = r"\[?'?voltage5:\s*(\d{1,3}\.{0,1}\d*),\s*charging5:\s*(\w{4,}),\s*voltage12:\s*(\d{1,3}\.{0,1}\d*)"
    match = re.search(pattern, string)
    if match != None:
        print("match FOUND")
        oldVoltage5 = match.group(1)
        print(oldVoltage5, "oldVoltage5")
        oldCharging5 = match.group(2)
        oldVoltage12 = match.group(3)
        return((oldVoltage5, oldCharging5, oldVoltage12))
    else:
        print("no match!")
        return None

if __name__ == '__main__':
    bi = BatteryIndicator()
    print(bi.rawData, "RAW")
    
                
        
