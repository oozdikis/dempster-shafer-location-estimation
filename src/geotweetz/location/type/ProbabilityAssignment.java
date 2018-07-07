package geotweetz.location.type;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * The probability assignment for a set of locations represented by their ids.
 * It also includes the number of elements that are assigned to this location
 * set.
 * 
 * @author oozdikis
 *
 */
public class ProbabilityAssignment implements Comparable<ProbabilityAssignment> {

	/**
	 * Set of location ids.
	 */
	private HashSet<Long> locationIds;

	/**
	 * Number of elements that are mapped to this set of locations.
	 */
	private int elementCount;

	/**
	 * Probability value that is assigned for this set of locations.
	 */
	private double probabilityValue;

	/**
	 * Initializes ProbabilityAssignment for the given set of location ids with
	 * default values.
	 * 
	 * @param locationIds
	 *            set of location ids
	 */
	public ProbabilityAssignment(HashSet<Long> locationIds) {
		this.locationIds = locationIds;
		this.elementCount = 0;
		this.probabilityValue = 0;
	}

	/**
	 * 
	 * @return set of location ids
	 */
	public HashSet<Long> getLocationIds() {
		return locationIds;
	}

	/**
	 * 
	 * @param locationIds
	 *            location ids to set for this probability assignment
	 */
	public void setLocationIds(HashSet<Long> locationIds) {
		this.locationIds = locationIds;
	}

	/**
	 * 
	 * @return number of elements that are mapped to the set of locations
	 */
	public int getElementCount() {
		return elementCount;
	}

	/**
	 * 
	 * @param elementCount
	 *            number of elements to set
	 */
	public void setElementCount(int elementCount) {
		this.elementCount = elementCount;
	}

	/**
	 * 
	 * @return the probability value for the locations
	 */
	public double getProbabilityValue() {
		return probabilityValue;
	}

	/**
	 * 
	 * @param probabilityValue
	 *            probability value for the locations to set
	 */
	public void setProbabilityValue(double probabilityValue) {
		this.probabilityValue = probabilityValue;
	}

	/**
	 * Generates a String that can be used as a hashcode for a set of location
	 * ids. It returns ordered location ids separated by a '-' character as a
	 * hashcode String.
	 * 
	 * @param locationIds
	 *            Location ids to generate the String hashcode.
	 * @return The String generated as a hashcode.
	 */
	public static String generateHashcodeForSet(Collection<Long> locationIds) {
		String idString = "";
		List<Long> sortedIds = new LinkedList<Long>(locationIds);
		Collections.sort(sortedIds);
		for (Long id : sortedIds) {
			idString += id + "-";
		}
		return idString;
	}

	@Override
	public int compareTo(ProbabilityAssignment o) {
		if (o.getProbabilityValue() > probabilityValue) {
			return 1;
		} else if (o.getProbabilityValue() < probabilityValue) {
			return -1;
		}
		return 0;
	}

	public String toString() {
		return "(" + elementCount + "," + String.format("%.3f", probabilityValue) + ")";
	}

}
