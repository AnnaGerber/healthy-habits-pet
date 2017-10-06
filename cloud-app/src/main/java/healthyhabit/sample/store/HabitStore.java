package healthyhabit.sample.store;

import java.util.Collection;

import com.cloudant.client.api.Database;

import healthyhabit.sample.HabitNotification;

/**
 * Defines the API for a Habit store.
 *
 */
public interface HabitStore {

  	/**
	 * Get the target db object.
	 * 
	 * @return Database.
  	 * @throws Exception 
	 */
  public Database getDB();
  
  	/**
	 * Gets all Habits from the store.
	 * 
	 * @return All Habits
  	 * @throws Exception 
	 */
  public Collection<HabitNotification> getAll();

  /**
   * Gets an individual Habit from the store.
   * @param id The ID of the Habit to get.
   * @return The Habit.
   */
  public HabitNotification get(String id);

  /**
   * Persists a Habit to the store.
   * @param habit The Habit to persist.
   * @return The persisted Habit.  The Habit will not have a unique ID..
   */
  public HabitNotification persist(HabitNotification habit);

  /**
   * Updates a Habit in the store.
   * @param id The ID of the Habit to update.
   * @param habit The Habit with updated information.
   * @return The updated Habit
   */
  public HabitNotification update(String id, HabitNotification habit);

  /**
   * Deletes a Habit from the store.
   * @param id The ID of the Habit to delete.
   */
  public void delete(String id);
  
  /**
   * Counts the number of Habits
   * @return The total number of Habits
 * @throws Exception 
   */
  public int count() throws Exception;
}
