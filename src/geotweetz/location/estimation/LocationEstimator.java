package geotweetz.location.estimation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import geotweetz.location.dbo.CityDBO;
import geotweetz.location.dbo.TweetDBO;
import geotweetz.location.type.LocationCommonalityValue;
import geotweetz.location.type.ProbabilityAssignment;

/**
 * Implementation of a location estimation method using Dempster-Shafer (DS) Theory.
 * 
 * Please see:
 * <p>
 * "Evidential estimation of event locations in microblogs using the Dempster–Shafer theory"
 * <a href="http://dl.acm.org/citation.cfm?id=3009510">(http://dl.acm.org/ citation.cfm?id=3009510)</a>
 * <p>
 * and
 * <p>
 * "Evidential location estimation for events detected in Twitter"
 * <a href="http://dl.acm.org/citation.cfm?id=2533929">(http://dl.acm.org/ citation.cfm?id=2533929)</a>
 * 
 * <p>
 * The estimation method expects a clustered set of tweets that are presumably about an event and the set of locations
 * that define the propositional space of possible solutions (the frame of discernment). Using three spatial attributes
 * in tweets (lat-lon, tweet text, location in user profile) as three evidence sources, the estimation method assigns
 * basic probability values to sets of locations, combines them using combination rules in DS theory, calculates
 * commonality values for locations, and selects the location(s) with the highest commonality as the estimated event
 * location(s).
 * 
 * @author oozdikis
 *
 */
/*
 * Other components and extensions to the method, such as estimation at town level, using city-town association
 * (association of evidence), and location disambiguation are not included in this sample code. The terms "Location" and
 * "City" can be used interchangeably (since the code can also be used for location estimation using other types of
 * locations (e.g., town or POI). In this sample code, estimations are made at the city level, and the set of all cities
 * is denoted by Theta.
 * 
 */
public class LocationEstimator {
	private static final Logger logger = Logger.getLogger(LocationEstimator.class);

	/**
	 * All locations (cities) that define the propositional space of possible. solutions
	 */
	private List<CityDBO> allCitiesInFrameOfDiscernment = null;

	/**
	 * TweetLocationMapper Object that is used to map tweets to locations.
	 */
	private TweetLocationMapper tweetLocationMapper = null;

	/**
	 * Constructor
	 * 
	 * @param allCitiesInFrameOfDiscernment
	 *            All locations (cities) that define the propositional space of possible solutions.
	 */
	public LocationEstimator(List<CityDBO> allCitiesInFrameOfDiscernment) {
		this.allCitiesInFrameOfDiscernment = allCitiesInFrameOfDiscernment;
		this.tweetLocationMapper = new TweetLocationMapper(allCitiesInFrameOfDiscernment);
	}

	/**
	 * The method is used to estimate the location for an event represented by a collection of clustered tweets.
	 * Estimation using DS theory is carried out in 3 steps: 1) basic probability assignments for sets of locations are
	 * calculated using spatial attribute in tweets, 2) basic probability assignments are combined using combination
	 * rules, 3) commonality score for each location are found and the locations with the highest commonality score are
	 * selected as the event location.
	 * 
	 * @param tweetsInCluster
	 *            A collection of clustered tweets.
	 * @return LocationCommonalityValues for locations with the highest commonality score.
	 */
	public ArrayList<LocationCommonalityValue> estimateLocationForCluster(List<TweetDBO> tweetsInCluster) {
		logger.debug("Estimating location for cluster with " + tweetsInCluster.size() + " tweets");

		HashMap<TweetDBO, HashSet<Long>> tweetCityMappings = tweetLocationMapper
				.mapTweetsToCitiesUsingLatitudeLongitude(tweetsInCluster);
		HashMap<String, ProbabilityAssignment> basicProbabilityAssignmentsUsingTweetLatitudeLongitude = getBasicProbabilityAssignments(
				tweetsInCluster, tweetCityMappings);

		tweetCityMappings = tweetLocationMapper.mapTweetsToCitiesUsingContent(tweetsInCluster);
		HashMap<String, ProbabilityAssignment> basicProbabilityAssignmentsUsingTweetContent = getBasicProbabilityAssignments(
				tweetsInCluster, tweetCityMappings);

		tweetCityMappings = tweetLocationMapper.mapTweetsToCitiesUsingProfile(tweetsInCluster);
		HashMap<String, ProbabilityAssignment> basicProbabilityAssignmentsUsingUserProfileLocation = getBasicProbabilityAssignments(
				tweetsInCluster, tweetCityMappings);

		/*
		 * Disambiguation and city-town association can be executed at this point. They are excluded from this sample
		 * code for simplicity.
		 */

		HashMap<String, ProbabilityAssignment> combinedBPAsUsingGpsAndContent = executeCombineUsingDuboisAndPrade(
				basicProbabilityAssignmentsUsingTweetLatitudeLongitude.values(),
				basicProbabilityAssignmentsUsingTweetContent.values());
		HashMap<String, ProbabilityAssignment> combinedBPAsUsingAllThreeTweetFeatures = executeCombineUsingDuboisAndPrade(
				combinedBPAsUsingGpsAndContent.values(), basicProbabilityAssignmentsUsingUserProfileLocation.values());

		HashMap<Long, LocationCommonalityValue> cityCommonalityValuesMap = getLocationCommonalityValuesMap(
				combinedBPAsUsingAllThreeTweetFeatures);
		ArrayList<LocationCommonalityValue> highestCityCommonalityValues = getHighestLocationCommonalityValues(
				cityCommonalityValuesMap);
		return highestCityCommonalityValues;
	}

	/**
	 * 
	 * Calculates basic probability values for subsets of locations in the frame of discernment. A tweet that does not
	 * provide any evidence for a specific location supports Theta (to represent indifference). The method takes
	 * tweet-location mappings that are determined using one of the spatial features in tweets, and returns basic
	 * probability assignments (BPAs) for locations.
	 * 
	 * @param tweetsInCluster
	 *            Collection of tweets in a cluster (tweet cluster that represents an event)
	 * @param tweetLocationMappings
	 *            Mappings between tweets to sets of location ids that are identified using either tweet lat-lon, tweet
	 *            content or location in user profile.
	 * @return Basic probability assignments (BPAs) for locations.
	 */
	private HashMap<String, ProbabilityAssignment> getBasicProbabilityAssignments(List<TweetDBO> tweetsInCluster,
			HashMap<TweetDBO, HashSet<Long>> tweetLocationMappings) {
		HashMap<String, ProbabilityAssignment> probabilityAssignments = new HashMap<String, ProbabilityAssignment>();

		List<Long> allLocationIds = new LinkedList<Long>();
		for (CityDBO city : allCitiesInFrameOfDiscernment) {
			allLocationIds.add(city.getId());
		}
		String hashcodeForThetaSet = ProbabilityAssignment.generateHashcodeForSet(allLocationIds);

		int numberOfTweetsWithNoLocationMapping = 0;
		for (TweetDBO tweetInCluster : tweetsInCluster) {
			HashSet<Long> locationIdsFoundInTweet = tweetLocationMappings.get(tweetInCluster);
			if (locationIdsFoundInTweet != null && locationIdsFoundInTweet.size() > 0) {
				String hashcodeForLocationsSet = ProbabilityAssignment.generateHashcodeForSet(locationIdsFoundInTweet);
				ProbabilityAssignment probabilityAssignment = probabilityAssignments.get(hashcodeForLocationsSet);
				if (probabilityAssignment == null) {
					probabilityAssignment = new ProbabilityAssignment(locationIdsFoundInTweet);
					probabilityAssignments.put(hashcodeForLocationsSet, probabilityAssignment);
				}
				probabilityAssignment.setElementCount(probabilityAssignment.getElementCount() + 1);
				probabilityAssignment
						.setProbabilityValue(1.0 * probabilityAssignment.getElementCount() / tweetsInCluster.size());
			} else {
				numberOfTweetsWithNoLocationMapping++;
			}
		}
		if (numberOfTweetsWithNoLocationMapping > 0) {
			ProbabilityAssignment probabilityAssignment = probabilityAssignments.get(hashcodeForThetaSet);
			if (probabilityAssignment == null) {
				probabilityAssignment = new ProbabilityAssignment(new HashSet<Long>(allLocationIds));
				probabilityAssignments.put(hashcodeForThetaSet, probabilityAssignment);
			}
			probabilityAssignment
					.setElementCount(probabilityAssignment.getElementCount() + numberOfTweetsWithNoLocationMapping);
			probabilityAssignment
					.setProbabilityValue(1.0 * probabilityAssignment.getElementCount() / tweetsInCluster.size());
		}
		return probabilityAssignments;
	}

	/**
	 * Implementation of the combination rule "Dubois and Prade" according to the description given in the article:
	 * "Evidential estimation of event locations in microblogs using the Dempster–Shafer theory" . The method takes
	 * probability assignments using two different evidence sources and combines them.
	 * 
	 * @param bpas1
	 *            Basic probability assignments using evidence source #1
	 * @param bpas2
	 *            Basic probability assignments using evidence source #2
	 * @return Combined probability assignments
	 */
	private static HashMap<String, ProbabilityAssignment> executeCombineUsingDuboisAndPrade(
			Collection<ProbabilityAssignment> bpas1, Collection<ProbabilityAssignment> bpas2) {
		HashMap<String, ProbabilityAssignment> combinedBpasMap = new HashMap<String, ProbabilityAssignment>();
		HashMap<String, Double> remainingProbabilityValuesForUnions = new HashMap<String, Double>();
		HashMap<String, HashSet<Long>> unionHashcodeLocationIdsMap = new HashMap<String, HashSet<Long>>();
		for (ProbabilityAssignment bpa1 : bpas1) {
			for (ProbabilityAssignment bpa2 : bpas2) {
				double multiplication = bpa1.getProbabilityValue() * bpa2.getProbabilityValue();
				HashSet<Long> locationIdsSet1 = bpa1.getLocationIds();
				HashSet<Long> locationIdsSet2 = bpa2.getLocationIds();
				HashSet<Long> intersection = new HashSet<Long>(locationIdsSet1);
				intersection.retainAll(locationIdsSet2);
				if (intersection.size() == 0) { // conflicting evidence, assign
												// to union.
					HashSet<Long> union = new HashSet<Long>(locationIdsSet1);
					union.addAll(locationIdsSet2);
					String unionSetHashcode = ProbabilityAssignment.generateHashcodeForSet(new LinkedList<Long>(union));
					Double valueOfUnion = remainingProbabilityValuesForUnions.get(unionSetHashcode);
					if (valueOfUnion == null) {
						valueOfUnion = 0.0;
					}
					valueOfUnion += multiplication;
					remainingProbabilityValuesForUnions.put(unionSetHashcode, valueOfUnion);
					if (!unionHashcodeLocationIdsMap.containsKey(unionSetHashcode)) {
						unionHashcodeLocationIdsMap.put(unionSetHashcode, union);
					}
				} else { // non-conflicting evidence, assign to intersection
					String intersectionId = ProbabilityAssignment
							.generateHashcodeForSet(new LinkedList<Long>(intersection));
					ProbabilityAssignment combinedBpa = combinedBpasMap.get(intersectionId);
					if (combinedBpa == null) {
						combinedBpa = new ProbabilityAssignment(intersection);
						combinedBpasMap.put(intersectionId, combinedBpa);
					}
					combinedBpa.setProbabilityValue(multiplication + combinedBpa.getProbabilityValue());
				}
			}
		}

		Set<String> keySet = remainingProbabilityValuesForUnions.keySet();
		for (String key : keySet) { // distribute conflicting evidence
			double union = remainingProbabilityValuesForUnions.get(key);
			ProbabilityAssignment bpa = combinedBpasMap.get(key);
			if (bpa == null) {
				bpa = new ProbabilityAssignment(unionHashcodeLocationIdsMap.get(key));
				bpa.setProbabilityValue(union);
				combinedBpasMap.put(key, bpa);
			} else {
				bpa.setProbabilityValue(bpa.getProbabilityValue() + union);
			}
		}

		return combinedBpasMap;
	}

	/**
	 * Finds the commonality values for locations using the given probability assignments.
	 * 
	 * @param combinedProbabilities
	 *            Probability assignments for sets of locations.
	 * @return HashMap that maps a location id to the commonality value calculated for that location.
	 */
	private HashMap<Long, LocationCommonalityValue> getLocationCommonalityValuesMap(
			HashMap<String, ProbabilityAssignment> combinedProbabilities) {
		HashMap<Long, LocationCommonalityValue> commonalities = new HashMap<Long, LocationCommonalityValue>();
		for (ProbabilityAssignment combinedProbability : combinedProbabilities.values()) {
			HashSet<Long> locationIds = combinedProbability.getLocationIds();
			for (long locationId : locationIds) {
				LocationCommonalityValue commonality = commonalities.get(locationId);
				if (commonality == null) {
					commonality = new LocationCommonalityValue(locationId, 0);
					commonalities.put(locationId, commonality);
				}
				commonality.setCommonalityValue(
						commonality.getCommonalityValue() + combinedProbability.getProbabilityValue());
			}
		}
		return commonalities;
	}

	/**
	 * Sorts the values in the locationCommonalityValuesMap and returns the LocationCommonalityValue(s) with the highest
	 * commonality value.
	 * 
	 * @param locationCommonalityValuesMap
	 *            HashMap that maps location ids to their commonality values.
	 * @return LocationCommonalityValue(s) with the highest commonality value.
	 */
	private ArrayList<LocationCommonalityValue> getHighestLocationCommonalityValues(
			HashMap<Long, LocationCommonalityValue> locationCommonalityValuesMap) {
		ArrayList<LocationCommonalityValue> highestLocationCommonalityValues = new ArrayList<LocationCommonalityValue>();
		if (locationCommonalityValuesMap.size() > 0) {
			LinkedList<LocationCommonalityValue> locationCommonalityValuesList = new LinkedList<LocationCommonalityValue>(
					locationCommonalityValuesMap.values());
			Collections.sort(locationCommonalityValuesList);
			double highestCommonalityValue = locationCommonalityValuesList.get(0).getCommonalityValue();
			for (LocationCommonalityValue locationCommonalityValue : locationCommonalityValuesList) {
				if (locationCommonalityValue.getCommonalityValue() == highestCommonalityValue) {
					highestLocationCommonalityValues.add(locationCommonalityValue);
				}
			}
		}
		return highestLocationCommonalityValues;
	}
}
