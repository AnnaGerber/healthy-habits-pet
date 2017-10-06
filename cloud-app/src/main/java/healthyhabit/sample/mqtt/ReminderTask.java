package healthyhabit.sample.mqtt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import healthyhabit.sample.HabitNotification;
import healthyhabit.sample.store.HabitStore;
import healthyhabit.sample.store.HabitStoreFactory;

public class ReminderTask extends TimerTask {
	
	// Threshold for suppressing reminders in minutes
	private static int REMINDER_THRESHOLD = 60 * 2;
	
	// Determine status based on habit notification ratio
	public String calculateStatus(int numReminders) {
		String status = "fair";
		int dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		float ratio = numReminders / dayOfMonth;
		if (ratio > 0.9 || dayOfMonth <= 1) {
			status = "great";
		} else if (ratio > 0.6 || dayOfMonth <= 2) {
			status = "good";
		} else if (ratio < 0.3) {
			status = "sleep";
		}
		return status;
	}
	public void run() {
		Date today = new Date();
	    // Trigger sending a reminder if there hasn't been a recent habit notification
		HabitStore store = HabitStoreFactory.getInstance();
		ReminderPublisher reminderPublisher = ReminderPublisherFactory.getInstance();
		if (store == null) System.out.println("Can not find store");
		if (reminderPublisher == null) System.out.println("Can not find reminder publisher");
		if (store != null && reminderPublisher != null) {
			List<Date> deviceDates = new ArrayList<Date>();
			for (HabitNotification doc : store.getAll()) {
				Date timestamp = doc.getTimestamp();
				if (timestamp != null){
					deviceDates.add(timestamp);
				}
				Collections.sort(deviceDates);
				Collections.reverse(deviceDates);
			}
			
			if (deviceDates.size() > 0) {
				Date mostRecent = deviceDates.get(0);
				System.out.println(mostRecent);
				long diff = today.getTime() - mostRecent.getTime();
				if (diff >= 0) {
					long diffMinutes = diff / (60 * 1000);
					if (diffMinutes >= REMINDER_THRESHOLD) {
						reminderPublisher.remind(calculateStatus(deviceDates.size()));
					} else {
						System.out.println("Supressing reminder due to recent habit notification");
					}
				}
			}
		}
	}
}
