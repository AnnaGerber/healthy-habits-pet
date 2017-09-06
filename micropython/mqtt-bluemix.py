import machine
import time
import ubinascii
import webrepl
import oled
import soundfx

from umqtt.simple import MQTTClient

def remind():
    # eyes on
    oled.pupils()
    # make a sound
    soundfx.question()
    # light up
    lights.randomWipe()


def update_pet(topic, msg):
    # update the pet status and config based on message received from backend
    print((topic, msg))


def beHappy():
  # hearts in eyes, clear lights and make a happy sound
  oled.heartBeat()
  soundfx.happy()
  time.sleep(0.5)
  oled.love()
  lights.clearWipe()

def beSleepy():
  # clear lights if on
  lights.clearWipe()
  # sleepy eyes
  oled.sleepy()

def main():
    button = machine.Pin(12, machine.Pin.IN, machine.Pin.PULL_UP)
    server = "<orgid>.messaging.internetofthings.ibmcloud.com"
    clientId = "d:<orgid>:ESP8266:pet2"
    user = "use-token-auth"
    token = "<token>"

    client = MQTTClient(clientId, server, port = 8883, ssl = True, user=user, password=token)
    client.set_callback(update_pet)

    while True:
        # non-blocking check for messages
        try:
          client.connect()
          client.subscribe(b"‘iot-2/cmd/update-tracker/fmt/json")
          client.check_msg()

          # detect button presses and publish if pressed
          firstButtonReading = button.value()
          time.sleep(0.01)
          secondButtonReading = button.value()
          if firstButtonReading and not secondButtonReading:
            # connect and send event on press
            print("connecting to MQTT broker")
            try:
              client.publish(b"iot-2/evt/habit/fmt/json", b"{\"responded\":\"true\"}")
            except:
              err = sys.exc_info()[0]
              print("Error publishing to MQTT broker: {0}".format(err))
          try:
            print("disconnecting from MQTT broker")
            client.disconnect()
            beHappy()
          except:
            err = sys.exc_info()[0]
            print("Error disconnecting from MQTT broker: {0}".format(err))
        except:
          err = sys.exc_info()[0]
          print("Error connecting to and subscribing to MQTT broker: {0}".format(err))
if __name__ == '__main__':
    main()