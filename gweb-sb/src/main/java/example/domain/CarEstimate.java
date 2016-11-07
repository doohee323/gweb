package example.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
   
@Entity
@Table(name = "carEstimate")
public class CarEstimate implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", nullable = false)
	public String id;

	@Column(name = "version", nullable = false)
	public String version; // version

	@Column(name = "monthlyPay", nullable = false)
	public Long monthlyPay; // 월납부금

	@Column(name = "rate", nullable = false)
	public Double rate; // 금리

	@Column(name = "restValue", nullable = false)
	public Double restValue; // 잔존가치

	@Column(name = "deliveryCostFirst", nullable = false)
	public Integer deliveryCostFirst; // 1차탁송료

	@Column(name = "deliveryCostSecond", nullable = false)
	public Integer deliveryCostSecond; // 2차탁송료

	@Column(name = "additionalCost", nullable = false)
	public Integer additionalCost; // 부대비용

	@Column(name = "insuranceCost", nullable = false)
	public Integer insuranceCost; // 보험료

	@Column(name = "insuranceCostMyCar", nullable = false)
	public Integer insuranceCostMyCar; // 자차보험료
}
