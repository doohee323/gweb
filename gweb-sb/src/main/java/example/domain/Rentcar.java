package example.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "rentcar")
public class Rentcar implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@Column(name = "version", nullable = false)
	public String version;

	@Column(name = "brand", nullable = false)
	public String brand;

	@Column(name = "model", nullable = false)
	public String model;

	@Column(name = "grade", nullable = false)
	public String grade;

	@Column(name = "returnCar", nullable = false)
	public Boolean returnCar;

	@Column(name = "duration", nullable = false)
	public Integer duration;

	@Column(name = "age", nullable = false)
	public Boolean age;

	@Column(name = "deposit", nullable = false)
	public Double deposit;

	@Column(name = "commission", nullable = false)
	public Double commission;

	@Column(name = "includeRepair", nullable = false)
	public Boolean includeRepair;

	@Column(name = "ownerType", nullable = false)
	public Integer ownerType;

}