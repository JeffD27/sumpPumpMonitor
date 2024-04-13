#!/usr/bin/python3.11
# import main Flask class and request object

from flask import Flask, render_template, request, url_for, jsonify, abort
from getData import *
import threading
from time import sleep
import queue
import json
import sys
import subprocess as sp 


#note: DON'T FORGET TO RUN PUMPCONTROL.PY. IT WON'T RUN ON IT'S OWN
 
#https://www.java.com/en/download/help/linux_x64_install.html#install
#https://www.baeldung.com/java-home-on-windows-mac-os-x-linux

# create the Flask app
app = Flask(__name__)
dataObj = ""
print("createdApp")
@app.route('/',methods=['GET', 'POST'])
def __init__(first_run = True):
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
  
        if first_run: #let's get this working NEXT4425
          
            print("firstRun geting data")
            dataObj = getData()
            #lock = threading.Lock()
            print("PARSE DATA LOOP WILL BE CALLED------------------------------------------------------------------------------------")

            data = dataObj.call_parse_data()
            print('''
              


                %s






             '''%data) 
            return jsonify(data)
            
        

            
       
        
        #data_thread.join()
       
       
        #data_thread.start()
       
        
       # print(getDataObj.bi_data, "ouuurr newest edition$%^#^$^&%&%")
        #data_thread.join() #this stops the code below from running until the above is completed
        
        
        #que.put()
        #print(que.get(), 'in getdatafromthread in app')
       

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

    app.run(debug=False, host="0.0.0.0", port= 8080)

#https://jbacoy3.wixsite.com/datascience/single-post/2017/05/26/simple-tutorial-on-flask
