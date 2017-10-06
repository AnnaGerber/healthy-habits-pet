import network
import mqttbluemix

# disable access point mode
ap_if=network.WLAN(network.AP_IF)
ap_if.active(False)
# configure connection to wireless network
sta_if = network.WLAN(network.STA_IF)
if not sta_if.isconnected():
  sta_if.active(True)
  sta_if.connect('<yourssid>', '<yourpassword>')
  while not sta_if.isconnected():
    pass

# start the main program    
mqttbluemix.main()
