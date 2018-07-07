package geotweetz.location.type;

/**
 * LocationCommonalityValue is used to keep the commonality value for a
 * location. Location estimation method aims to find location(s) with the
 * highest commonality value. The class is implemented as a Comparable class for
 * easier sorting.
 * 
 * @author oozdikis
 *
 */
public class LocationCommonalityValue implements Comparable<LocationCommonalityValue> {

	/**
	 * Id of the location
	 */
	private long locationId;

	/**
	 * Commonality value calculated for the location
	 */
	private double commonalityValue;

	/**
	 * 
	 * @param locationId
	 *            location id to set
	 * @param commonalityValue
	 *            commonality value to set
	 */
	public LocationCommonalityValue(long locationId, double commonalityValue) {
		this.locationId = locationId;
		this.commonalityValue = commonalityValue;
	}

	/**
	 * 
	 * @return location id
	 */
	public long getLocationId() {
		return locationId;
	}

	/**
	 * 
	 * @param locationId
	 *            location id to set
	 */
	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	/**
	 * 
	 * @return commonality value
	 */
	public double getCommonalityValue() {
		return commonalityValue;
	}

	/**
	 * 
	 * @param commonalityValue
	 *            commmonality value to set
	 */
	public void setCommonalityValue(double commonalityValue) {
		this.commonalityValue = commonalityValue;
	}

	@Override
	public int compareTo(LocationCommonalityValue o) {
		if (o.getCommonalityValue() > commonalityValue) {
			return 1;
		} else if (o.getCommonalityValue() < commonalityValue) {
			return -1;
		}
		return 0;
	}

	public String toString() {
		return "(" + locationId + "," + String.format("%.3f", commonalityValue) + ")";
	}

}
