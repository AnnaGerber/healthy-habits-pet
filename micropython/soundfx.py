from machine import Pin
import utime
import math
p = Pin(12, Pin.OUT)

def soundFX(amplitude=1000.0, period=1000.0, repeat=100):
  for i in range(1,repeat):
    uDelay = math.floor(1.5 + amplitude + amplitude * math.sin(utime.ticks_ms() / period))
    p.value(1)
    utime.sleep_us(uDelay)
    p.value(0)
    utime.sleep_us(uDelay)

def worried():
    soundFX(8000.0, 20.0, 100)

def happy():
    soundFX(90.0,60.0,400)

def question():
  soundFX(2200.0,100.0,200)

def sing():
  soundFX(90.0,600.0,4800)