import cscore
from cscore import VideoMode
from cscore import CameraServer
from networktables import NetworkTables

import cv2
import json
import numpy as np
import time

def main():
   with open('/boot/config.json') as f:
      config = json.load(f)
   camera = config['cameras'][0]

   width = camera['width']
   height = camera['height']
   
   #vm = cscore.VideoMode(pixelFormat=VideoMode.PixelFormat.kYUYV,width=320,height=240,fps=125)
   
   cs = CameraServer.getInstance()
   
  
   cs.enableLogging()
   # cs.startAutomaticCapture(dev=0)
   vs = cscore.VideoSource(cs.startAutomaticCapture(dev=0)) # get the video source
   set_format = cscore.VideoSource.setPixelFormat(vs, VideoMode.PixelFormat.kYUYV) # setting video source format to yuyv
   print('video format set', vs)
   

   input_stream = cs.getVideo()
   output_stream = cs.putVideo('Processed', width, height)

   # Table for vision output information
   vision_nt = NetworkTables.getTable('Vision')

   # Allocating new images is very expensive, always try to preallocate
   img = cv2.cvtColor(np.zeros(shape=(240, 320, 3), dtype=np.uint8),cv2.COLOR_BGR2YUV) # have to change to YUYV format

   # Wait for NetworkTables to start
   time.sleep(0.5)

   while True:
      start_time = time.time()
      frame_time, input_img = input_stream.grabFrame(img)
      # input_img = cv2.cvtColor(input_img,cv2.COLOR_YUV2RGB) # convert input image to rgb
      # output_img = cv2.cvtColor(np.copy(input_img),cv2.COLOR_YUV2RGB) # convert to BGR so it displays
      # output_img = np.copy(input_img) #output image is rgb
      # Notify output of error and skip iteration
      if frame_time == 0:
         output_stream.notifyError(input_stream.getError())
         continue

      # Convert to HSV and threshold image
      # hsv_img = cv2.cvtColor(input_img, cv2.COLOR_BGR2HSV)
    
      processing_time = time.time() - start_time
      fps = 1 / processing_time
      # cv2.putText(output_img, str(round(fps, 1)), (0, 40), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255))
      # output_stream.putFrame(output_img) # output processed frame

main()