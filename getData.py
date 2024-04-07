from battery_indicator import BatteryIndicator 
from waterlevel import Water_level
import threading
from time import sleep
import parseDataFiles
from threadClass import *
#import queue 
from pumpControl import pumpControl
from datetime import datetime

#global data_all = [relay_data, bi_data, wl_data]
class getData():
    run = True
    pumpData = ""
    bi_data = ""
    wl_data = ""
    lock = threading.Lock()

   
    
    def __init__(self):
       
       print("get data initialized")
       #self.call_parse_data()
       
       '''
        q = queue.Queue()
        
        if first_run(): #if parse data was not called yet
            threadPump = threading.Thread(target=self.call_parse_data)
       
        threadEverythingElse = threading.Thread(target=self.pulse_hardware)
        threads = [threadPump, threadEverythingElse]
        self.q = queue.Queue()
        for thread in threads:
            thread.start()
            #thread.join()
        #self.pulse_hardware()
        #sleep(.5)
        '''    
    def call_parse_data(self, queue):
        data = parseDataFiles.parseDataFiles()
        while True: #you need this so that python can keep track of time variable (ie pump running)
            print("starting while loop in getdata.callparsedata")
            data.run()
           
            mainRunning = data.mainRunning
            self.pumpData = data.all_data #reading data from parsedatafiles (NOT FROM GPIO)
            
            
            print(str(self.pumpData), "TTTTTTTTTTTTTTTTTTTTTTTTTTT\n\n")
            self.pulse_hardware()
            print(self.pumpData)
            
            queue.put({"time":datetime.now(), "pumpData": self.pumpData, "wl_data": self.wl_data, "bi_data": self.bi_data}) #this adds pumpdata to the queue which can be grabbed later
            
            print("----------------------PUTTED-------------------------")
            print(datetime.now())
            sleep(.2)# i think the problem is parsing data above.
    def pulse_hardware(self):
        print("pulsing hardware")
        self.wl_data = self.getWaterLevel()
        self.bi_data = self.getBatteryVolts()
        #pumpControl() #the whole idea is to not have to call this hear and have pumpcontrol run independently.
        #self.getPumpData(queue)
        print("hardware pulsed")
        # print(self.threadPump.result, '^^^^^^^^^^^^^^^^^^^^^\n')
        #sleep(.3)

        
    def getPumpData(self, queue): #not running
                
        print('pumps function is running')
        print(queue, "queueu")
        print(queue.get(), ';') #this hangs
        try:
            print("trying")
            print(queue.get(), 'queue.get')
        except Exception as e:
            print(e)
        self.pumpData = queue.get()
        print("inGETPUMPDATA<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>\n\n",self.pumpData)
            
          

    def getWaterLevel(self):
       
        #while True:
        #with lock:
        #print('gettingWL')
        wl_obj = Water_level()
        self.wl_data = wl_obj.data
        print(self.wl_data, "wl_data ~~~~~~~~~~~~~~~~~~~~~~~~~")

        return wl_obj.data
        #sleep(.300)
            
    def getBatteryVolts(self):
       
        #while True:
        #with lock:
        #print('gettingBI')
        bi_obj = BatteryIndicator()
        self.bi_data = bi_obj.data
        
        print(self.bi_data, "bi data ~~~~~~~~~~~~~~~~~~~~~~~~~")
        return bi_obj.data
        #sleep(.300)
    
    @classmethod
    def giveData(self, queue):
        lock = getData.lock
        
        print(self.bi_data, 'bi data')
        print(self.wl_data, 'wl data')
        with lock:
            try:
                data = ({"pumpData": self.pumpData, "waterLevel": self.wl_data, "batteries": self.bi_data})
                print(data, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
                return data
            except IndexError:
                print("data is Null")

    '''def updateData(self, relay_data, bi_data, wl_data):
            print('update data is running ^^^^^^^^^^^^^^^^^^^%^%^^^^^^^^^^^^^^^^^^^^^^^^^^^^')
            with self._lockP:
                while True:
                    bi_data = self.bi_obj
                    if "Threading_control.bi_obj" in locals():
                        print(Threading_control.bi_obj.data, "k4$#@")
                    else:
                        print("failed((((((((((((((((()))))))))))))))))))))no bi obj")
                    print(bi_data, "!!!!!!!!!!!!!!!!!!!!!!BI,BI,BI,data!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    sleep(.300)'''
       

       
    print('here') 
           
           
            
if __name__ == '__main__':
    s = getData()
    sleep(.5)
    #d = getData.giveData()
   
