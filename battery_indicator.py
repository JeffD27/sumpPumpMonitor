from gpiozero import MCP3008 as MCP
import math
import RPi.GPIO as GPIO
from datetime import datetime 
from time import sleep
#mosi input miso: output

class BatteryIndicator:
    data = ""

    def __init__(self):
        print("BatteryIndicator is running")
        #while True:
        #print('battery indicator running')
        try:
            small = MCP(channel = 0, clock_pin =  24, miso_pin = 23, mosi_pin = 21, select_pin = 20)
            large =  MCP(channel = 5, clock_pin =  24, miso_pin = 23, mosi_pin = 21, select_pin = 20)
        except Exception as e:
            print(e)
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(5, GPIO.IN)
        charge = GPIO.input(5)
        if charge == 0:
            charge = False
        elif charge == 1:
            charge = True
        else:
            print("houston we have a problem in battery_indicator")
        try:
            s = small.value
            l = large.value
            print(l, "12v in battery indicator$$$$$$$$$$$$$$$$$$$$$$$$$$")
            s = (s - 0.65) * 666 #should give percentage
            l = (l * 100)
            if s > 100:
                s = 100
            
            #s = math.trunc(s)
            
            BatteryIndicator.data = {"voltage5": s, "charging5": charge, "voltage12": l}#c needs to be replaced
            
        except Exception as e:
            print(e, 'error in battery indactor end')

        
        
    
    
            #if circuit[0] > 0.1:
        #print(BatteryIndicator.data, 'battery indicator dot data%^')
        #tc_bi_data = __import__('startThreading').Threading_control.bi_data
        #tc_bi_data = BatteryIndicator.data
        #print(tc_bi_data, "This is here********************************")
        #    sleep(.300)


if __name__ == '__main__':
    bi = BatteryIndicator()
    '''
    with open('/home/sump-pump/Documents/battery_indicator/data.txt', 'a') as file1:
            file1.write(data)
            return(data)
    '''    
                
        
