package healthyhabit.sample;

import java.util.Date;

/**
 * Represents the HabitTracker device notification document stored in Cloudant.
 * Used to record details of a habit notification  (e.g. exercise completed) 
 */

public class HabitNotification {
	private String _id;
	private String _rev;
	private Date timestamp = null;
	private String deviceId = null;
	private String deviceType = null;
	private String eventType = null;

	
	public HabitNotification() {
		this.deviceId = "";
		this.deviceType = "ESP8266";
		this.eventType = "habit";
	}

	/**
	 * Gets the ID.
	 * 
	 * @return The ID.
	 */
	public String get_id() {
		return _id;
	}

	/**
	 * Sets the ID
	 * 
	 * @param _id
	 *            The ID to set.
	 */
	public void set_id(String _id) {
		this._id = _id;
	}

	/**
	 * Gets the revision of the document.
	 * 
	 * @return The revision of the document.
	 */
	public String get_rev() {
		return _rev;
	}

	/**
	 * Sets the revision.
	 * 
	 * @param _rev
	 *            The revision to set.
	 */
	public void set_rev(String _rev) {
		this._rev = _rev;
	}
	
	/**
	 * Gets the deviceId of the document.
	 * 
	 * @return The deviceId of the document.
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Sets the name of the device through which the habit was notified
	 * 
	 * @param name
	 * The deviceName to set.
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	/**
	 * Sets the type of the device through which the habit was notified
	 * 
	 * @param name
	 * The deviceName to set.
	 */
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
	/** Gets the deviceType of the document.
	 * 
	 * @return The deviceType of the document.
	 */
	public String getDeviceType() {
		return deviceType;
	}
	/**
	 * Sets the event type through which the habit was notified
	 * 
	 * @param name
	 * The eventType to set.
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
	/** Gets the deviceType of the document.
	 * 
	 * @return The deviceType of the document.
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * Gets the timestamp of the document.
	 * 
	 * @return The timestamp of the document.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp
	 * 
	 * @param timestamp
	 * The timestamp to set.
	 */
	public void setTimestamp(Date ts) {
		this.timestamp = ts;
	}

}