package healthyhabit.sample.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

import com.google.gson.Gson;

import healthyhabit.sample.HabitNotification;
import healthyhabit.sample.store.HabitStore;
import healthyhabit.sample.store.HabitStoreFactory;
import healthyhabit.sample.mqtt.ReminderPublisher;
import healthyhabit.sample.mqtt.ReminderPublisherFactory;

@ApplicationPath("api")
@Path("/tracker")
public class HabitAPI extends Application {
	
	HabitStore store = HabitStoreFactory.getInstance();
	ReminderPublisher reminderPublisher = ReminderPublisherFactory.getInstance();
	
  /**
   * Gets a list of times when habit was tracked this month
   * GET http://localhost:9080/HealthyHabitsBackend/api/tracker
   * @return A collection of all the Habit Notification times
   */
    @GET
    @Path("/")
    @Produces({"application/json"})
    public String getHabits() {
		
		if (store == null) {
			return "[]";
		}
		
		List<Date> deviceDates = new ArrayList<Date>();
		for (HabitNotification doc : store.getAll()) {
			String deviceId = doc.getDeviceId();
			Date timestamp = doc.getTimestamp();
			if (timestamp != null){
				deviceDates.add(timestamp);
			}
			Collections.sort(deviceDates);
		}
		return new Gson().toJson(deviceDates);
    }
    
    /**
     * Trigger a reminder
     * GET http://localhost:9080/HealthyHabitsBackend/api/tracker/remind
     */
      @GET
      @Path("/remind")
      public String remind() {
  		reminderPublisher.remind("good");
  		return "OK";
      }
}