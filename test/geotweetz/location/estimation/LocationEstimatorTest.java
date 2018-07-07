package geotweetz.location.estimation;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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
import geotweetz.location.type.LocationCommonalityValue;

/**
 * Test class to test LocationEstimator.
 * 
 * @author oozdikis
 *
 */
public class LocationEstimatorTest {
	private static final int NUMBER_OF_CITIES_IN_TESTS = 10;
	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
	private List<CityDBO> testCitiesInCountry = null;

	@Before
	public void initialize() {
		configureLog4j();
		this.testCitiesInCountry = generateTestCitiesInCountry();
	}

	/**
	 * Tests location estimation (single location has the highest commonality)
	 */
	@Test
	public void testEstimateLocationForClusterSingleResult() {
		LocationEstimator locationEstimator = new LocationEstimator(testCitiesInCountry);

		// Calculation of BPAs and Commonality values for the given test data below...
		// BPA GPS: {city1}=1/4, {city2}=1/4, Theta=2/4
		// BPA Content: {city1}=1/4, {city1, city2}=1/4, Theta=2/4
		// BPA Profile: {city1}=1/4, Theta=3/4
		// Combined GPS+Content: {city1}=6/16, {city2}=3/16, {city1, city2}=3/16, Theta=4/16
		// Combined GPS+Content+Profile: {city1}=31/64, {city2}=9/64, {city1, city2}=12/64, Theta=12/64
		// Commonality of City1=(31+12+12)/64=55/64=0.859375
		List<TweetDBO> tweetsInCluster = new ArrayList<TweetDBO>();
		TweetDBO tweet0 = generateTestTweet(0, "lorem ipsum city1 city2", 0.0, 0.0, "city1");
		tweetsInCluster.add(tweet0);
		TweetDBO tweet1 = generateTestTweet(1, "lorem ipsum", 1.5, 0.5, "lorem ipsum");
		tweetsInCluster.add(tweet1);
		TweetDBO tweet2 = generateTestTweet(2, "city1", 2.5, 0.5, "lorem ipsum");
		tweetsInCluster.add(tweet2);
		TweetDBO tweet3 = generateTestTweet(3, "", 0.0, 0.0, "");
		tweetsInCluster.add(tweet3);
		ArrayList<LocationCommonalityValue> highestCityCommonalityValues = locationEstimator
				.estimateLocationForCluster(tweetsInCluster);
		Assert.assertEquals(highestCityCommonalityValues.size(), 1);
		Assert.assertEquals(highestCityCommonalityValues.get(0).getLocationId(), 1L);
		Assert.assertEquals(highestCityCommonalityValues.get(0).getCommonalityValue(), 0.859375, 1e-15);
	}

	/**
	 * Tests location estimation (multiple locations have the highest commonality)
	 */
	@Test
	public void testEstimateLocationForClusterMultipleResults() {
		LocationEstimator locationEstimator = new LocationEstimator(testCitiesInCountry);

		// Calculation of BPAs and Commonality values for the given test data below...
		// BPA GPS: {city1}=1/4, {city2}=1/4, Theta=2/4
		// BPA Content: {city1}=1/4, {city2}=1/4, {city1, city2}=1/4, Theta=1/4
		// BPA Profile: {city1}=1/4, {city2}=1/4, Theta=2/4
		// Combined GPS+Content: {city1}=5/16, {city2}=5/16, {city1, city2}=4/16, Theta=2/16
		// Combined GPS+Content+Profile: {city1}=21/64, {city2}=21/64, {city1, city2}=18/64, Theta=4/64
		// Commonality of City1 and City2 =(21+18+4)/64=43/64=0.671875
		List<TweetDBO> tweetsInCluster = new ArrayList<TweetDBO>();
		TweetDBO tweet0 = generateTestTweet(0, "lorem ipsum city1 city2", 0.0, 0.0, "city1");
		tweetsInCluster.add(tweet0);
		TweetDBO tweet1 = generateTestTweet(1, "lorem ipsum City2", 1.5, 0.5, "city2 lorem ipsum");
		tweetsInCluster.add(tweet1);
		TweetDBO tweet2 = generateTestTweet(2, "city1", 2.5, 0.5, "lorem ipsum");
		tweetsInCluster.add(tweet2);
		TweetDBO tweet3 = generateTestTweet(3, "", 0.0, 0.0, "");
		tweetsInCluster.add(tweet3);
		ArrayList<LocationCommonalityValue> highestCityCommonalityValues = locationEstimator
				.estimateLocationForCluster(tweetsInCluster);
		Assert.assertEquals(highestCityCommonalityValues.size(), 2);

		LocationCommonalityValue commonalityForCity1 = highestCityCommonalityValues.get(0).getLocationId() == 1L
				? highestCityCommonalityValues.get(0) : highestCityCommonalityValues.get(1);
		LocationCommonalityValue commonalityForCity2 = highestCityCommonalityValues.get(0).getLocationId() == 2L
				? highestCityCommonalityValues.get(0) : highestCityCommonalityValues.get(1);
		Assert.assertEquals(commonalityForCity1.getLocationId(), 1L);
		Assert.assertEquals(commonalityForCity1.getCommonalityValue(), 0.671875, 1e-15);
		Assert.assertEquals(commonalityForCity2.getLocationId(), 2L);
		Assert.assertEquals(commonalityForCity2.getCommonalityValue(), 0.671875, 1e-15);
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
