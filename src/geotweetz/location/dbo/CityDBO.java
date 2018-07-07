package geotweetz.location.dbo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Polygon;

/**
 * City Database Object that represents a city with its id, name and boundary coordinates.
 * 
 * @author oozdikis
 *
 */

@Entity
@Table(name = "city")
public class CityDBO implements Serializable {

	private static final long serialVersionUID = 5869292429646723732L;

	/**
	 * 
	 * Unique city id
	 */
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/**
	 * 
	 * Name of the city
	 */
	@Column(name = "name")
	private String name;

	/**
	 * 
	 * Polygon that represents the boundary coordinates (region) of the city in terms of latitude-longitude.
	 */
	@Type(type = "org.hibernatespatial.GeometryUserType")
	@Column(name = "boundary")
	private Polygon boundaryCoordinates;

	/**
	 * 
	 * @return city id
	 */
	public long getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 *            unique id of the city to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * 
	 * @return name of the city
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 *            name of the city to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return boundary coordinates of the city in terms of lat-lon Polygon
	 */
	public Polygon getBoundaryCoordinates() {
		return boundaryCoordinates;
	}

	/**
	 * 
	 * @param polygon
	 *            boundary coordinates of the city to set
	 */
	public void setBoundaryCoordinates(Polygon polygon) {
		this.boundaryCoordinates = polygon;
	}

	public String toString() {
		return "(city: " + id + ", " + name + ")";
	}
}
