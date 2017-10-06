import time
from machine import Pin
from neopixel import NeoPixel

import uos
import math
p = Pin(2,  Pin.OUT)
np = NeoPixel(p, 12)

def colorWipe(color, delay=0.01):
  global np
  numPixels = np.n
  for i in range(numPixels):
    np[i] = color
    time.sleep(delay)
    np.write()

def randomWipe(delay=0.01):
  global np
  numPixels = np.n
  for t in range(0, 8):
    color = (math.floor(ord(uos.urandom(1))/4 * 3), math.floor(ord(uos.urandom(1))/3 * 4), math.floor(ord(uos.urandom(1))/2 * 2))
    for i in range(numPixels):
      np[i] = color
      time.sleep(delay)
      np.write()

def clearWipe():
  colorWipe((0, 0, 0))

def demo():
  global np
  numPixels = np.n
  colorWipe((0,0,100)) 
  time.sleep(0.2)
  