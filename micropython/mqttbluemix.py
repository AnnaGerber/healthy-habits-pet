import machine
import time
import ubinascii
import webrepl
import oled
import soundfx
import lights
import ujson
from umqtt.simple import MQTTClient

currentStatus = 'good'
def remind(status='sleep'):
  global currentStatus
  currentStatus = status
  updateEyes(status)
  if status != 'sleep':
    # make a sound
    soundfx.question()
    # light up
    lights.randomWipe()

def updateEyes(status):
  # eyes indicate current status
  if status == 'sleep':
    beSleepy()
  elif status == 'fair':
    oled.pupils()
  elif status == 'good':
    oled.love()
  elif status == 'great':
    oled.bigLove()

def updatePet(topic, msg):
  # update the pet status based on message received from backend
  print(msg)
  status = ujson.load(msg).status
  remind(status)


def beHappy():
  global currentStatus
  # hearts in eyes, clear lights and make a happy sound
  lights.clearWipe()
  oled.heartBeat()
  soundfx.happy()
  time.sleep(0.5)
  updateEyes(currentStatus)
  

def beSleepy():
  # clear lights if on
  lights.clearWipe()
  # sleepy eyes
  oled.sleepy() 

def main():
  button = machine.Pin(14, machine.Pin.IN, machine.Pin.PULL_UP)
  orgid = "replace with your 6 character org id"
  token = "replace with your token"
  user = "use-token-auth"
  # Make sure this matches up with the device type you configured through the IoT platform
  deviceType = "ESP8266"
  # Change to match your device Id
  deviceId = "pet2"

  server = '{}.messaging.internetofthings.ibmcloud.com'.format(orgid)
  clientId = 'd:{}:{}:{}'.format(orgid, deviceType, deviceId)
  try:
    client = MQTTClient(clientId, server, port = 8883, ssl = True, user=user, password=token)
    
  except:
    print('MQTT client setup error')

  try:
    client.set_callback(updatePet)
    
  except:
    print('MQTT callback error')
  

  pendingNotification = False
  reminded = False
  counter = 0
  while True:
    counter = counter + 1
    
    # every so many runs through the loop, connect to the MQTT broker to publish and check for messages
    # prevents repeated button press spam
    if counter >= 800:
      counter = 0
      if (reminded == False):
        remind('good')
        reminded = True
      client.connect()
      # non-blocking check for messages
      client.subscribe(b"iot-2/type/ESP8266/id/pet2/cmd/update-tracker/fmt/json")
      client.check_msg()
      client.disconnect()
      time.sleep(0.01)
      
      # send notification if button was pressed since last time
      if pendingNotification == True:
        print('connecting to MQTT broker...')
        client.connect()
        client.publish(b"iot-2/evt/habit/fmt/json", b"{\"responded\":\"true\"}")
        pendingNotification = False
        print('disconnecting from MQTT broker')
        client.disconnect()
    

    # detect button presses
    firstButtonReading = button.value()
    time.sleep(0.01)
    secondButtonReading = button.value()
    if firstButtonReading and not secondButtonReading:
      # notification will be sent
      pendingNotification = True
      beHappy()