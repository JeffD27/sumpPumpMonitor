import threading
import getData 
from time import sleep
class pumpThread(threading.Thread):
    
    def __init__(self, queue, first_run, getDataObj=None):
        threading.Thread.__init__(self)
        self.queue = queue
        self.first_run = first_run
        print("first_run", first_run)
        self.getDataObj = getData.getData() #just returns self (ie getdata)
        
        print(self.getDataObj, "This is the OBJECT\n\n\n\n")    
        

        
        



    def run(self):
        
        print('in run')
        print(self.first_run, 'first run in run')
        if self.first_run == True:
            self.getDataObj.call_parse_data()
        
        # Get the next object from the queue.
        '''
        self.queue.put(
            {"pumpData": self.getDataObj.pumpData,
             "waterLevel": self.getDataObj.wl_data,
             "batteryInfo": self.getDataObj.bi_data
            })
       
        print(self.queue.get(), "\n\n que.get in threadclass")
        # Do something with the object.
        print(self.getDataObj.pumpdata, 'inThreadClass@@')

        # Signal that the object has been processed.
        #self.queue.task_done()
        ''' 
        
       
        
    def get_data_from_queue(self):#not actually from que anymore *shrug*
        return {"pumpData": self.pumpData, "waterLevel": self.waterLevel, "batteryInfo": self.battery}
       #return self.queue.get()
    def updateData(self):
        print("updatePumpData is running")
       
        self.waterLevel = self.getDataObj.getWaterLevel()
        self.battery = self.getDataObj.getBatteryVolts()

