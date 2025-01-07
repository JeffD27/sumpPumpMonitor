import battery_indicator
import time

def run():
   
    while True:
        bi =battery_indicator.BatteryIndicator()
        data = bi.data
        for i in data:
             print(i, "i")
             
        print('`````````````````RAW ````~~~~~~~~~~~~~~~~~~~~~DATA`````````\n\n',bi.rawData)

        time.sleep(.5)

if __name__ == "__main__":

        run()