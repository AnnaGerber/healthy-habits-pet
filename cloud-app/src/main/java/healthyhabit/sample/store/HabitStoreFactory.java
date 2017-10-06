package healthyhabit.sample.store;

public class HabitStoreFactory {
	
	private static HabitStore instance;
	static {
		CloudantHabitStore cvif = new CloudantHabitStore();	
		if(cvif.getDB() != null){
			instance = cvif;
		}
	}
	
	public static HabitStore getInstance() {
		return instance;
	}

}
