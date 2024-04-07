import RPi.GPIO as GPIO

class Water_level():
    data = []
    
    def __init__(self):
        print("water_level is running")
        #print('water level check')
        GPIO.setmode(GPIO.BCM)
        pins = [["highFlooding", 13, False],["midFlooding", 6, False],["lowFlooding", 17, False]] #empty string to fill with "flooding" or "not flooding" later

        #while True:
        GPIO.setup(17, GPIO.IN)
        GPIO.setup(6, GPIO.IN)
        GPIO.setup(13, GPIO.IN)


        for pin in pins:
            #print(pin, "gpio read:", GPIO.input(pin))
            if GPIO.input(pin[1]) == 0:
                print("flooding on", pin[0])
                pin[2] = True #flooding
            else:
                print("no flooding on", pin[0])
                pin[2] = False #not flooding
        Water_level.data = {pins[0][0]: pins[0][2], pins[1][0]: pins[1][2], pins[2][0]: pins[2][2]} 
        #for pin in Water_level.pin_data:
            #print(pin[0],pin[2])
           # sleep(.3)
if __name__ == '__main__':
    w = Water_level()
    
