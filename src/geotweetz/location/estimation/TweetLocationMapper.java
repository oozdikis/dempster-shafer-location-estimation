package geotweetz.location.estimation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import geotweetz.location.dbo.CityDBO;
import geotweetz.location.dbo.TweetDBO;

/**
 * Implementation of location mapping methods for tweets. The class supports mappings using three spatial features in
 * tweets: 1) latitude-longitude information in geotagged tweets, 2) location names mentioned in tweet content, 3)
 * location names in user profile.
 * 
 * @author oozdikis
 *
 */
public class TweetLocationMapper {
	private static final Logger logger = Logger.getLogger(TweetLocationMapper.class);
	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

	/**
	 * All locations (cities) that define the propositional space of possible solutions.
	 */
	private List<CityDBO> allCitiesInFrameOfDiscernment = null;

	/**
	 * 
	 * @param allCitiesInFrameOfDiscernment
	 *            All locations (cities) that define the propositional space of possible solutions.
	 */
	public TweetLocationMapper(List<CityDBO> allCitiesInFrameOfDiscernment) {
		this.allCitiesInFrameOfDiscernment = allCitiesInFrameOfDiscernment;
	}

	/**
	 * The method that maps tweets to locations using their GPS coordinates (latitude-longitude). It maps a tweet t to
	 * the location that contains t's GPS position.
	 * 
	 * @param tweets
	 *            Tweets to be processed.
	 * @return HashMap that keeps mappings from tweets to location ids that are found using GPS coordinates. In
	 *         practice, the set of location ids for a tweet contains at most one element (a specific lat-lon can not be
	 *         in two different cities). If a tweet is not geotagged, it is mapped to an empty set.
	 */
	public HashMap<TweetDBO, HashSet<Long>> mapTweetsToCitiesUsingLatitudeLongitude(List<TweetDBO> tweets) {
		logger.debug("mapTweetsToCitiesUsingLatitudeLongitude() called for " + tweets.size() + " tweets.");
		HashMap<TweetDBO, HashSet<Long>> tweetCityMappings = new HashMap<TweetDBO, HashSet<Long>>();
		for (TweetDBO tweet : tweets) {
			HashSet<Long> locations = findIdsOfCitiesAtLatitudeLongitude(tweet.getLatitude(), tweet.getLongitude());
			tweetCityMappings.put(tweet, locations);
		}
		return tweetCityMappings;
	}

	/**
	 * The method that maps tweets to locations using their content (tweet text).
	 * 
	 * @param tweets
	 *            Tweets to be processed.
	 * @return HashMap that keeps mappings from tweets to location ids that are found using tweet text. A tweet can
	 *         include multiple location names. In this case, ids of these locations are returned for that tweet. If a
	 *         tweet does not include any reference to a location in its content, the tweet is mapped to an empty set.
	 */
	public HashMap<TweetDBO, HashSet<Long>> mapTweetsToCitiesUsingContent(List<TweetDBO> tweets) {
		logger.debug("mapTweetsToCitiesUsingContent() called for " + tweets.size() + " tweets.");
		HashMap<TweetDBO, HashSet<Long>> tweetCityMappings = new HashMap<TweetDBO, HashSet<Long>>();
		for (TweetDBO tweet : tweets) {
			HashSet<Long> locations = findIdsOfCitiesInText(tweet.getContent());
			tweetCityMappings.put(tweet, locations);
		}
		return tweetCityMappings;
	}

	/**
	 * The method that maps tweets to locations using the location attribute in the user profiles.
	 * 
	 * @param tweets
	 *            Tweets to be processed.
	 * @return HashMap that keeps mappings from tweets to location ids that are found using user profile. The location
	 *         attribute in the user profile can include multiple location names. In this case, ids of these locations
	 *         are included in the mapping for that tweet. If a user profile does not include any reference to a
	 *         location, the tweet posted by that user is mapped to an empty set.
	 */
	public HashMap<TweetDBO, HashSet<Long>> mapTweetsToCitiesUsingProfile(List<TweetDBO> tweets) {
		logger.debug("mapTweetsToCitiesUsingProfile() called for " + tweets.size() + " tweets.");
		HashMap<TweetDBO, HashSet<Long>> tweetCityMappings = new HashMap<TweetDBO, HashSet<Long>>();
		for (TweetDBO tweet : tweets) {
			HashSet<Long> locations = findIdsOfCitiesInText(tweet.getUserLocation());
			tweetCityMappings.put(tweet, locations);
		}
		return tweetCityMappings;
	}

	/**
	 * The method that finds the location id at the given latitude-longitude.
	 * 
	 * @param latitude
	 *            latitude
	 * @param longitude
	 *            longitude
	 * @return id of the location at the given latitude-longitude. Id is returned in a HashSet to have the same
	 *         representation with other types of tweet-location mappings
	 */
	private HashSet<Long> findIdsOfCitiesAtLatitudeLongitude(double latitude, double longitude) {
		HashSet<Long> foundLocations = new HashSet<Long>();
		Coordinate coordinate = new Coordinate(latitude, longitude);
		Point point = GEOMETRY_FACTORY.createPoint(coordinate);
		for (CityDBO location : allCitiesInFrameOfDiscernment) {
			if (point.within(location.getBoundaryCoordinates())) {
				foundLocations.add(location.getId());
			}
		}
		return foundLocations;
	}

	/**
	 * The method that finds the location names in a given text and returns the ids of these locations.
	 * 
	 * @param textToSearchForLocationNames
	 *            Text to search for location names.
	 * @return Ids of locations found that are found in the given text.
	 */
	private HashSet<Long> findIdsOfCitiesInText(String textToSearchForLocationNames) {
		HashSet<Long> foundLocationIds = new HashSet<Long>();
		if (textToSearchForLocationNames != null && !textToSearchForLocationNames.trim().equals("")) {
			String[] termsInText = StringUtils.split(textToSearchForLocationNames, " ,.\n\t()!?:;\"“'@#\\/-&");
			for (String termInText : termsInText) {
				for (CityDBO location : allCitiesInFrameOfDiscernment) {
					if (termInText.equalsIgnoreCase(location.getName())) {
						foundLocationIds.add(location.getId());
					}
				}
			}
		}
		return foundLocationIds;
	}

}
