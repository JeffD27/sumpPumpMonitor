#!/usr/bin/python3.11
# import main Flask class and request object
from flask import Flask, render_template, request, url_for, jsonify, abort
from getData import *
import threading
import threadClass
from time import sleep
import queue
import multiprocessing as mp
import re


#note: DON'T FORGET TO RUN PUMPCONTROL.PY. IT WON'T RUN ON IT'S OWN
 
#https://www.java.com/en/download/help/linux_x64_install.html#install
#https://www.baeldung.com/java-home-on-windows-mac-os-x-linux

# create the Flask app
app = Flask(__name__)

print("createdApp")
@app.route('/',methods=['GET', 'POST'])
def __init__(first_run = True, que = queue.LifoQueue()):
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
        
        

        #return render_template('inputpage.html')
        #first_run = argv[0]
    
        
       # data_thread = threadClass.pumpThread(queue=que, first_run= first_run)
        if first_run:
            print("firstRun geting data")
            getDataObj = getData()
            print("PARSE DATA LOOP WILL BE CALLED------------------------------------------------------------------------------------")
            data_thread = threading.Thread(target = lambda: getDataObj.call_parse_data(que))
            data_thread.start()
       
        
        #data_thread.join()
        sleep(.5)
       
        #data_thread.start()
       
        
       # print(getDataObj.bi_data, "ouuurr newest edition$%^#^$^&%&%")
        #data_thread.join() #this stops the code below from running until the above is completed
        print("<<<<<<<<<<<<<<<<<<<<<<<<getdatafromthread in app>>>>>>>>>>>>>>>>>>>>>>")
        
        #que.put()
        #print(que.get(), 'in getdatafromthread in app')
        try:
            while not que.empty():
                data = que.get()
            
            
            
            print(data)
            print(data["pumpData"], "Pump Data in try in app^^^^^^^^^^^^^^^^^^^^^^^^^^^")
            
            #que.task_done()
            print(data, "this is in try((((((((()))))))))")
           # que.task_done()
        except Exception as e:
            print(e, "ERRROR IN GET DATA FROM THREAD")
        #print(pumpData, "in server@@@@<<<<<<<<<<<<<<<>>>>>>\n\n", pumpData, "\n\<<<<<<<>>>>>>>>\n")
        #print(data, '_)_)_)_)_)_)_)+)_')
        
        #print(type(data), '&&')
        if data is None:
            print("no data!!!!!!!!!!!!!!!!!!!")
            return "No data found"
        else:
            print(data, "\n<!<!<!<!<!<!<<!<<!<>!>!>!>>!>!>$$$$$$$$$$")
        #print("returning data", data)
       
        return jsonify(data)
           
        for thread in threads:
            thread.join()

    elif request.method == 'POST':
        print("\n<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>\nPOST REQUEST\n")    
        args = request.get_data().decode()
        print(args, "yaaarrg")
       
        if args == "firstRun: False":
            print("\n9999999999999\n \n\nFALSE\n")
            first_run = False
        elif args == "firstRun: True":
            print("\n9999999999999\n \nTRUE\n")
            first_run = True
        else:
            print("FAILED!!")
        return(jsonify("message Received"))
    



if __name__ == '__main__':
    # run app in debug mode on port 5000

    app.run(debug=True, host="0.0.0.0", port= 8080)

#https://jbacoy3.wixsite.com/datascience/single-post/2017/05/26/simple-tutorial-on-flask
