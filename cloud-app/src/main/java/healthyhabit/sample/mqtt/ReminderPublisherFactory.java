package healthyhabit.sample.mqtt;

public class ReminderPublisherFactory {
	private static ReminderPublisher instance;
	static {
		ReminderPublisher publisher;
		try {
			publisher = new ReminderPublisher();
			if (publisher.mqttAsyncClient != null) {
				instance = publisher;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static ReminderPublisher getInstance() {
		return instance;
	}
}


