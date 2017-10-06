package healthyhabit.sample.mqtt;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.google.gson.JsonObject;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import java.util.logging.Level;

import healthyhabit.sample.store.VCAPHelper;

public class ReminderPublisher  {
	
	private static final String CLASS_NAME = ReminderPublisher.class.getName();
	private static Logger LOGGER = Logger.getLogger(CLASS_NAME);
	protected static final int MQTT_PORT = 8883;
	protected static final String MQTT_PROTOCOL = "ssl://";
	
	protected static final String topic = "iot-2/type/ESP8266/id/pet2/cmd/update-tracker/fmt/json";
	//"iot-2/cmd/update-tracker/fmt/json";
	
	MqttAsyncClient mqttAsyncClient = null;
	MqttConnectOptions mqttOptions;
	MqttCallback mqttCallback;
	
	private static final MemoryPersistence DATA_STORE = new MemoryPersistence();
	
	/* Wait for 1 second after each attempt for the first 10 attempts*/
	private static final long RATE_0 = TimeUnit.SECONDS.toMillis(1);
	
	/* After 5 attempts throttle the rate of connection attempts to 1 per 10 second */
	private static final int THROTTLE_1 = 5;
	private static final long RATE_1 = TimeUnit.SECONDS.toMillis(10);
	
	/* After 10 attempts throttle the rate of connection attempts to 1 per minute */
	private static final int THROTTLE_2 = 10;
	private static final long RATE_2 = TimeUnit.MINUTES.toMillis(1);
	
	/* After 20 attempts throttle the rate of connection attempts to 1 per 5 minutes */
	private static final int THROTTLE_3 = 20;
	private static final long RATE_3 = TimeUnit.MINUTES.toMillis(5);
	
	
	public ReminderPublisher() throws Exception {
		this.mqttAsyncClient = createClient();
		this.setUpReminders();
	}
	
	
	
	public void setUpReminders() {
		Timer timer = new Timer();
		long daily = 1000 * 60 * 60 * 24;
		
		// Schedule automatic reminders
		Calendar morningReminderTime = Calendar.getInstance();
		morningReminderTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		morningReminderTime.set(Calendar.HOUR, 10);
		morningReminderTime.set(Calendar.MINUTE, 30);
		morningReminderTime.set(Calendar.SECOND, 0);
		morningReminderTime.set(Calendar.MILLISECOND, 0);

		Calendar afternoonReminderTime = Calendar.getInstance();
		afternoonReminderTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		afternoonReminderTime.set(Calendar.HOUR, 10);
		afternoonReminderTime.set(Calendar.MINUTE, 30);
		afternoonReminderTime.set(Calendar.SECOND, 0);
		afternoonReminderTime.set(Calendar.MILLISECOND, 0);

		System.out.println("Setting up morning reminder");
		timer.scheduleAtFixedRate(
			new ReminderTask(),
			morningReminderTime.getTime(),
			daily
		);
		System.out.println("Setting up afternoon reminder");
		timer.scheduleAtFixedRate(
			new ReminderTask(),
			afternoonReminderTime.getTime(),
			daily
		);
	}
	public void remind(String status) {
		System.out.println("Publishing reminder / update with status " + status);
		if (!isConnected()) {
			try {
				this.connect(3);
			} catch (MqttException e) {
				e.printStackTrace();
				return;
			}
		}
			
		JsonObject payload = new JsonObject();
		payload.addProperty("status", status);
		
		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setRetained(false);
		msg.setQos(0);	
		try {
			if (isConnected()) {
				mqttAsyncClient.publish(topic, msg).waitForCompletion();
			} 
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return;
		} catch (MqttException e) {
			e.printStackTrace();
			return;
		} finally {
			this.disconnect();
		}
	}
	
	private MqttAsyncClient createClient() {
		final String METHOD = "createClient";
		String threadName = Thread.currentThread().getName();
		String mqttHost;
		String orgId;
		String apiKey;
		String apiToken;
		if (System.getenv("VCAP_SERVICES") != null) {
			// When running in Bluemix, the VCAP_SERVICES env var will have the credentials for all bound/connected services
			// Parse the VCAP JSON structure looking for iotf.
			JsonObject iotfCredentials = VCAPHelper.getCloudCredentials("iotf");
			if(iotfCredentials == null){
				System.out.println("No IOTF service bound to this application");
				return null;
			}
			mqttHost = iotfCredentials.get("mqtt_host").getAsString();
			orgId = iotfCredentials.get("org").getAsString();
			apiKey = iotfCredentials.get("apiKey").getAsString();
			apiToken = iotfCredentials.get("apiToken").getAsString();
		} else {
			System.out.println("Running locally. Looking for credentials in iotf.properties");
			mqttHost = VCAPHelper.getLocalProperties("iotf.properties").getProperty("mqtt_host");
			orgId = VCAPHelper.getLocalProperties("iotf.properties").getProperty("org");
			apiKey = VCAPHelper.getLocalProperties("iotf.properties").getProperty("apiKey");
			apiToken = VCAPHelper.getLocalProperties("iotf.properties").getProperty("apiToken");
			if(mqttHost == null || mqttHost.length()==0){
				System.out.println("To use MQTT, set the IOTF properties in src/main/resources/iotf.properties");
				return null;
			}
		}
		try {
			System.out.println("Creating MQTT client");
			String serverURI = MQTT_PROTOCOL + mqttHost + ":" + MQTT_PORT;
			String clientId = "a:" + orgId + ":heathyhabitsbackend";
			MqttAsyncClient client = new MqttAsyncClient(serverURI, clientId, DATA_STORE);
			mqttOptions = new MqttConnectOptions();
			mqttOptions.setPassword(apiToken.toCharArray());
			mqttOptions.setUserName(apiKey);
			
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

			sslContext.init(null, null, null);
			
			mqttOptions.setSocketFactory(sslContext.getSocketFactory());
			LOGGER.logp(Level.INFO, CLASS_NAME, METHOD, threadName + ": " + "MQTT client created");
			return client;
		} catch (Exception e) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, threadName + ": " + "Unable to create MQTT client");
			e.printStackTrace();
			return null;
		}
	}
	
	public void connect(int numberOfRetryAttempts) throws MqttException {
		final String METHOD = "connect";
		String threadName = Thread.currentThread().getName();
		// return if its already connected
		if(mqttAsyncClient != null && mqttAsyncClient.isConnected()) {
			LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD, threadName + ": Client is already connected");
			return;
		}
		boolean tryAgain = true;
		int connectAttempts = 0;
		
				LOGGER.logp(Level.INFO, CLASS_NAME, METHOD, threadName + ": Initiating Token based authentication");
				//connectUsingToken();
			
				DisconnectedBufferOptions disconnectedOpts = new DisconnectedBufferOptions();
				disconnectedOpts.setBufferEnabled(true);
				disconnectedOpts.setBufferSize(5000);
				mqttAsyncClient.setBufferOpts(disconnectedOpts);
		
		while (tryAgain) {
			connectAttempts++;
			
			try {
				mqttAsyncClient.connect(this.mqttOptions).waitForCompletion(1000 * 60);
			} catch (MqttSecurityException e) {
				System.err.println("One or more connection parameters are wrong !!!");
				LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, threadName + ": Connecting to Watson IoT Platform failed - " +
						"one or more connection parameters are wrong !!!", e);
				throw e;
				
			} catch (MqttException e) {
				if(connectAttempts > numberOfRetryAttempts) {
					LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD, threadName + ": Connecting to Watson IoT Platform failed", e);
	                throw e;
	            }
				e.printStackTrace();
			}
			
			if (mqttAsyncClient.isConnected()) {
				LOGGER.logp(Level.INFO, CLASS_NAME, METHOD, threadName + ": Successfully connected "
						+ "to the IBM Watson IoT Platform");
				
				tryAgain = false;
			} else {
				waitBeforeNextConnectAttempt(connectAttempts);
			}
		}
	}
	private void waitBeforeNextConnectAttempt(final int attempts) {
		final String METHOD = "waitBeforeNextConnectAttempt";
		String threadName = Thread.currentThread().getName();
		// Log when throttle boundaries are reached
		if (attempts == THROTTLE_3) {
			String message = String.valueOf(attempts) + 
					" consecutive failed attempts to connect.  Retry delay increased to " + String.valueOf(RATE_3) + "ms";
			LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD, threadName + ": "+ message);
		}
		else if (attempts == THROTTLE_2) {
			String message = String.valueOf(attempts) + 
					" consecutive failed attempts to connect.  Retry delay increased to " + String.valueOf(RATE_2) + "ms";
			LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD, threadName + ": " + message);
		}
		else if (attempts == THROTTLE_1) {
			String message = String.valueOf(attempts) + 
					" consecutive failed attempts to connect.  Retry delay set to " + String.valueOf(RATE_1) + "ms";
			LOGGER.logp(Level.INFO, CLASS_NAME, METHOD, threadName + ": " + message);
		}

		try {
			long delay = RATE_0;
			if (attempts >= THROTTLE_3) {
				delay = RATE_3;
			} else if (attempts >= THROTTLE_2) {
				delay = RATE_2;
			} else if (attempts >= THROTTLE_1) {
				delay = RATE_1;
			}
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public boolean isConnected() {
		boolean connected = false;
		if (mqttAsyncClient != null) {
			connected = mqttAsyncClient.isConnected();
		} 
		return connected;
	}
	public void disconnect() {
		final String METHOD = "disconnect";
		String threadName = Thread.currentThread().getName();
		LOGGER.logp(Level.FINE, CLASS_NAME, METHOD, threadName + ": " + "Disconnecting from the IOTF service");
		try {
			mqttAsyncClient.disconnect();
			LOGGER.logp(Level.INFO, CLASS_NAME, METHOD, "Successfully disconnected from the IOTF service");
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
