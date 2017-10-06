package healthyhabit.sample.store;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.google.gson.JsonObject;

import healthyhabit.sample.HabitNotification;

public class CloudantHabitStore implements HabitStore{
	
	private Database db = null;
	
	public CloudantHabitStore(){
		CloudantClient cloudant = createClient();
		String databaseName = createDatabaseName();
		if(cloudant!=null){
			System.out.println("Connecting to db " + databaseName);
			db = cloudant.database(databaseName, true);
		}
	}
	
	public Database getDB(){
		return db;
	}
	
	private static String createDatabaseName() {
		// get the current month's bucket
		SimpleDateFormat dbBucketFormatter = new SimpleDateFormat("YYYY-MM");
		String dbBucketName = dbBucketFormatter.format(new Date());
		return VCAPHelper.getLocalProperties("cloudant.properties").getProperty("cloudant_db_prefix") + dbBucketName;
	}
	private static CloudantClient createClient() {
		
		String url;
		if (System.getenv("VCAP_SERVICES") != null) {
			// When running in Bluemix, the VCAP_SERVICES env var will have the credentials for all bound/connected services
			// Parse the VCAP JSON structure looking for cloudant.
			JsonObject cloudantCredentials = VCAPHelper.getCloudCredentials("cloudant");
			if(cloudantCredentials == null){
				System.out.println("No cloudant database service bound to this application");
				return null;
			}
			url = cloudantCredentials.get("url").getAsString();
		} else {
			System.out.println("Running locally. Looking for credentials in cloudant.properties");
			url = VCAPHelper.getLocalProperties("cloudant.properties").getProperty("cloudant_url");
			if(url == null || url.length()==0){
				System.out.println("To use a database, set the Cloudant url in src/main/resources/cloudant.properties");
				return null;
			}
		}
		try {
			System.out.println("Connecting to Cloudant");
			CloudantClient client = ClientBuilder.url(new URL(url)).build();
			System.out.println("Connected to Cloudant");
			return client;
		} catch (Exception e) {
			System.out.println("Unable to connect to database");
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Collection<HabitNotification> getAll(){
        List<HabitNotification> docs;
		try {
			docs = db.getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(HabitNotification.class);
		} catch (IOException e) {
			return null;
		}
        return docs;
	}

	@Override
	public HabitNotification get(String id) {
		return db.find(HabitNotification.class, id);
	}

	@Override
	public HabitNotification persist(HabitNotification td) {
		String id = db.save(td).getId();
		return db.find(HabitNotification.class, id);
	}

	@Override
	public HabitNotification update(String id, HabitNotification newHabit) {
		HabitNotification habit = db.find(HabitNotification.class, id);
		habit.setDeviceId(newHabit.getDeviceId());
		db.update(habit);
		return db.find(HabitNotification.class, id);
		
	}

	@Override
	public void delete(String id) {
		HabitNotification habit = db.find(HabitNotification.class, id);
		db.remove(id, habit.get_rev());
		
	}

	@Override
	public int count() throws Exception {
		return getAll().size();
	}

}
