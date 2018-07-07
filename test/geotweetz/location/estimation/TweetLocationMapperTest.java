package geotweetz.location.estimation;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import geotweetz.location.dbo.CityDBO;
import geotweetz.location.dbo.TweetDBO;

/**
 * Test class to test TweetLocationMapper.
 * 
 * @author oozdikis
 *
 */
public class TweetLocationMapperTest {
	private static final int NUMBER_OF_CITIES_IN_TESTS = 10;
	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
	private List<CityDBO> testCitiesInCountry = null;

	@Before
	public void initialize() {
		configureLog4j();
		this.testCitiesInCountry = generateTestCitiesInCountry();
	}

	/**
	 * Tests tweet-location mapping using lat-lon (finds a matching location)
	 */
	@Test
	public void testMappingWithLatitudeLongitudeFoundMatch() {
		TweetLocationMapper tweetLocationMapper = new TweetLocationMapper(testCitiesInCountry);
		List<TweetDBO> tweetsInCluster = new ArrayList<TweetDBO>();
		TweetDBO tweet0 = generateTestTweet(0, "test0", 0.5, 0.5, "cityx");
		tweetsInCluster.add(tweet0);
		TweetDBO tweet1 = generateTestTweet(1, "test1", 1.5, 0.5, "cityx");
		tweetsInCluster.add(tweet1);
		TweetDBO tweet2 = generateTestTweet(2, "test1", 2.5, 0.5, "cityx");
		tweetsInCluster.add(tweet2);
		HashMap<TweetDBO, HashSet<Long>> tweetsMappedToCities = tweetLocationMapper
				.mapTweetsToCitiesUsingLatitudeLongitude(tweetsInCluster);
		Assert.assertEquals(tweetsMappedToCities.size(), 3);
		HashSet<Long> mappedCityIdsForTweet0 = tweetsMappedToCities.get(tweet0);
		Assert.assertNotNull(mappedCityIdsForTweet0);
		Assert.assertEquals(mappedCityIdsForTweet0.size(), 0);
		HashSet<Long> mappedCityIdsForTweet1 = tweetsMappedToCities.get(tweet1);
		Assert.assertNotNull(mappedCityIdsForTweet1);
		Assert.assertEquals(mappedCityIdsForTweet1.size(), 1);
		Assert.assertTrue(mappedCityIdsForTweet1.contains(1L));
		HashSet<Long> mappedCityIdsForTweet2 = tweetsMappedToCities.get(tweet2);
		Assert.assertNotNull(mappedCityIdsForTweet2);
		Assert.assertEquals(mappedCityIdsForTweet2.size(), 1);
		Assert.assertTrue(mappedCityIdsForTweet2.contains(2L));
	}

	/**
	 * Tests tweet-location mapping using lat-lon (can not find a matching location)
	 */
	@Test
	public void testMappingWithLatitudeLongitudeNotFoundMatch() {
		TweetLocationMapper tweetLocationMapper = new TweetLocationMapper(testCitiesInCountry);
		List<TweetDBO> tweetsInCluster = new ArrayList<TweetDBO>();
		TweetDBO tweet0 = generateTestTweet(0, "test0", 0.0, 0.0, "cityx");
		tweetsInCluster.add(tweet0);
		HashMap<TweetDBO, HashSet<Long>> tweetsMappedToCities = tweetLocationMapper
				.mapTweetsToCitiesUsingLatitudeLongitude(tweetsInCluster);
		Assert.assertEquals(tweetsMappedToCities.size(), 1);
		HashSet<Long> mappedCityIdsForTweet0 = tweetsMappedToCities.get(tweet0);
		Assert.assertNotNull(mappedCityIdsForTweet0);
		Assert.assertEquals(mappedCityIdsForTweet0.size(), 0);
	}

	/**
	 * Tests tweet-location mapping using tweet content (finds a matching location)
	 */
	@Test
	public void testMappingWithTweetContentFoundMatch() {
		TweetLocationMapper tweetLocationMapper = new TweetLocationMapper(testCitiesInCountry);
		List<TweetDBO> tweetsInCluster = new ArrayList<TweetDBO>();
		TweetDBO tweet0 = generateTestTweet(0, "lorem ipsum city1 ...city2: city1 lorem ipsum", 0.0, 0.0, "cityx");
		tweetsInCluster.add(tweet0);
		TweetDBO tweet1 = generateTestTweet(1, "City3, lorem ipsum", 0.0, 0.0, "cityx");
		tweetsInCluster.add(tweet1);
		TweetDBO tweet2 = generateTestTweet(2, "CITY2", 0.0, 0.0, "cityx");
		tweetsInCluster.add(tweet2);
		HashMap<TweetDBO, HashSet<Long>> tweetsMappedToCities = tweetLocationMapper
				.mapTweetsToCitiesUsingContent(tweetsInCluster);
		Assert.assertEquals(tweetsMappedToCities.size(), 3);
		HashSet<Long> mappedCityIdsForTweet0 = tweetsMappedToCities.get(tweet0);
		Assert.assertNotNull(mappedCityIdsForTweet0);
		Assert.assertEquals(mappedCityIdsForTweet0.size(), 2);
		Assert.assertTrue(mappedCityIdsForTweet0.contains(1L));
		Assert.assertTrue(mappedCityIdsForTweet0.contains(2L));
		HashSet<Long> mappedCityIdsForTweet1 = tweetsMappedToCities.get(tweet1);
		Assert.assertNotNull(mappedCityIdsForTweet1);
		Assert.assertEquals(mappedCityIdsForTweet1.size(), 1);
		Assert.assertTrue(mappedCityIdsForTweet1.contains(3L));
		HashSet<Long> mappedCityIdsForTweet2 = tweetsMappedToCities.get(tweet2);
		Assert.assertNotNull(mappedCityIdsForTweet2);
		Assert.assertEquals(mappedCityIdsForTweet2.size(), 1);
		Assert.assertTrue(mappedCityIdsForTweet2.contains(2L));
	}

	/**
	 * Tests tweet-location mapping using tweet content (can not find a matching location)
	 */
	@Test
	public void testMappingWithTweetContentNotFoundMatch() {
		TweetLocationMapper tweetLocationMapper = new TweetLocationMapper(testCitiesInCountry);
		List<TweetDBO> tweetsInCluster = new ArrayList<TweetDBO>();
		TweetDBO tweet0 = generateTestTweet(0, "lorem ipsum", 0.0, 0.0, "cityx");
		tweetsInCluster.add(tweet0);
		HashMap<TweetDBO, HashSet<Long>> tweetsMappedToCities = tweetLocationMapper
				.mapTweetsToCitiesUsingContent(tweetsInCluster);
		Assert.assertEquals(tweetsMappedToCities.size(), 1);
		HashSet<Long> mappedCityIdsForTweet0 = tweetsMappedToCities.get(tweet0);
		Assert.assertNotNull(mappedCityIdsForTweet0);
		Assert.assertEquals(mappedCityIdsForTweet0.size(), 0);
	}

	/**
	 * Tests tweet-location mapping using user profile (finds a matching location)
	 */
	@Test
	public void testMappingWithUserProfileLocationFoundMatch() {
		TweetLocationMapper tweetLocationMapper = new TweetLocationMapper(testCitiesInCountry);
		List<TweetDBO> tweetsInCluster = new ArrayList<TweetDBO>();
		TweetDBO tweet0 = generateTestTweet(0, "tweet0", 0.0, 0.0, "lorem ipsum city1,city2,city1 lorem ipsum");
		tweetsInCluster.add(tweet0);
		TweetDBO tweet1 = generateTestTweet(1, "tweet1", 0.0, 0.0, "City3, lorem ipsum");
		tweetsInCluster.add(tweet1);
		TweetDBO tweet2 = generateTestTweet(2, "tweet2", 0.0, 0.0, "CITY2");
		tweetsInCluster.add(tweet2);
		HashMap<TweetDBO, HashSet<Long>> tweetsMappedToCities = tweetLocationMapper
				.mapTweetsToCitiesUsingProfile(tweetsInCluster);
		Assert.assertEquals(tweetsMappedToCities.size(), 3);
		HashSet<Long> mappedCityIdsForTweet0 = tweetsMappedToCities.get(tweet0);
		Assert.assertNotNull(mappedCityIdsForTweet0);
		Assert.assertEquals(mappedCityIdsForTweet0.size(), 2);
		Assert.assertTrue(mappedCityIdsForTweet0.contains(1L));
		Assert.assertTrue(mappedCityIdsForTweet0.contains(2L));
		HashSet<Long> mappedCityIdsForTweet1 = tweetsMappedToCities.get(tweet1);
		Assert.assertNotNull(mappedCityIdsForTweet1);
		Assert.assertEquals(mappedCityIdsForTweet1.size(), 1);
		Assert.assertTrue(mappedCityIdsForTweet1.contains(3L));
		HashSet<Long> mappedCityIdsForTweet2 = tweetsMappedToCities.get(tweet2);
		Assert.assertNotNull(mappedCityIdsForTweet2);
		Assert.assertEquals(mappedCityIdsForTweet2.size(), 1);
		Assert.assertTrue(mappedCityIdsForTweet2.contains(2L));
	}

	/**
	 * Tests tweet-location mapping using user profile (can not find a matching location)
	 */
	@Test
	public void testMappingWithUserProfileLocationNotFoundMatch() {
		TweetLocationMapper tweetLocationMapper = new TweetLocationMapper(testCitiesInCountry);
		List<TweetDBO> tweetsInCluster = new ArrayList<TweetDBO>();
		TweetDBO tweet0 = generateTestTweet(0, "tweet0", 0.0, 0.0, "lorem ipsum");
		tweetsInCluster.add(tweet0);
		TweetDBO tweet1 = generateTestTweet(0, "tweet1", 0.0, 0.0, "");
		tweetsInCluster.add(tweet1);
		HashMap<TweetDBO, HashSet<Long>> tweetsMappedToCities = tweetLocationMapper
				.mapTweetsToCitiesUsingProfile(tweetsInCluster);
		Assert.assertEquals(tweetsMappedToCities.size(), 2);
		HashSet<Long> mappedCityIdsForTweet0 = tweetsMappedToCities.get(tweet0);
		Assert.assertNotNull(mappedCityIdsForTweet0);
		Assert.assertEquals(mappedCityIdsForTweet0.size(), 0);
		HashSet<Long> mappedCityIdsForTweet1 = tweetsMappedToCities.get(tweet1);
		Assert.assertNotNull(mappedCityIdsForTweet1);
		Assert.assertEquals(mappedCityIdsForTweet1.size(), 0);
	}

	private TweetDBO generateTestTweet(long id, String content, double lat, double lon, String userLocation) {
		TweetDBO tweet = new TweetDBO();
		tweet.setId(id);
		tweet.setContent(content);
		tweet.setLatitude(lat);
		tweet.setLongitude(lon);
		tweet.setUserLocation(userLocation);
		return tweet;
	}

	private static List<CityDBO> generateTestCitiesInCountry() {
		List<CityDBO> testCities = new ArrayList<CityDBO>();
		for (int i = 1; i <= NUMBER_OF_CITIES_IN_TESTS; i++) {
			CityDBO testCity = generateTestCity(i, "city" + i, new double[][] { { 0.0 + i, 0.0 }, { 1.0 + i, 0.0 },
					{ 1.0 + i, 1.0 }, { 0.0 + i, 1.0 }, { 0.0 + i, 0.0 } });
			testCities.add(testCity);
		}
		return testCities;
	}

	private static CityDBO generateTestCity(long cityId, String cityTitle, double[][] boundaryCoordinatesOfCity) {
		CityDBO city = new CityDBO();
		city.setId(cityId);
		city.setName(cityTitle);
		Coordinate[] coordinatesArray = new Coordinate[boundaryCoordinatesOfCity.length];
		for (int i = 0; i < boundaryCoordinatesOfCity.length; i++) {
			coordinatesArray[i] = new Coordinate(boundaryCoordinatesOfCity[i][0], boundaryCoordinatesOfCity[i][1]);
		}
		CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinatesArray);
		LinearRing linearRingOfCityBoundaryCoordinates = new LinearRing(coordinateSequence, GEOMETRY_FACTORY);
		Polygon polygon = new Polygon(linearRingOfCityBoundaryCoordinates, null, GEOMETRY_FACTORY);
		city.setBoundaryCoordinates(polygon);
		return city;
	}

	private void configureLog4j() {
		try {
			String log4jPath = "config/log4j.properties";
			URL log4jURL = getResource(log4jPath);
			PropertyConfigurator.configure(log4jURL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private URL getResource(String configFileName) throws Exception {
		File file = new File(configFileName);
		if (file.exists()) {
			URI uri = file.toURI();
			if (uri != null) {
				return uri.toURL();
			}
		}
		throw new Exception("No config file exists at " + configFileName);
	}

}
