package geotweetz.location.dbo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

/**
 * Tweet Database Object with attributes that are used in event localization.
 * 
 * @author oozdikis
 *
 */
/*
 * This data model for tweets contains only the necessary attributes for location estimation. Other tweet attributes
 * (e.g., posting time, user id, retweet info) are not included in this sample code.
 * 
 */
@Entity
@Table(name = "tweet")
public class TweetDBO implements Serializable {

	private static final long serialVersionUID = 5869292429646723732L;

	/**
	 * tweet id received from Twitter
	 */
	@Id
	@Column(name = "id")
	private long id;

	/**
	 * tweet content (tweet text)
	 */
	@Column(name = "content")
	private String content;

	/**
	 * latitude of tweet
	 */
	@Column(name = "latitude")
	@Index(name = "latitude")
	double latitude;

	/**
	 * longitude of tweet
	 */
	@Column(name = "longitude")
	@Index(name = "longitude")
	double longitude;

	/**
	 * location attribute in the profile of the user who posted the tweet
	 */
	@Column(name = "userlocation")
	private String userLocation;

	/**
	 * 
	 * @return tweet id
	 */
	public long getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 *            unique tweet id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * 
	 * @return tweet content (tweet text)
	 */
	public String getContent() {
		return content;
	}

	/**
	 * 
	 * @param content
	 *            tweet content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 
	 * @return latitude of tweet
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * 
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * 
	 * @return longitude of tweet
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * 
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * 
	 * @return location attribute in the user profile
	 */
	public String getUserLocation() {
		return userLocation;
	}

	/**
	 * 
	 * @param userLocation
	 *            user location to set
	 */
	public void setUserLocation(String userLocation) {
		this.userLocation = userLocation;
	}

	public String toString() {
		return "(tweet: " + id + ", " + content + ")";
	}
}
