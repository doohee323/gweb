package example.estimate.domain;

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
	public Long monthlyPay; // monthly_payment

	@Column(name = "rate", nullable = false)
	public Double rate; // rate

	@Column(name = "restValue", nullable = false)
	public Double restValue; // residual_value

	@Column(name = "deliveryCostFirst", nullable = false)
	public Integer deliveryCostFirst; // 1consignment

	@Column(name = "deliveryCostSecond", nullable = false)
	public Integer deliveryCostSecond; // 2consignment

	@Column(name = "additionalCost", nullable = false)
	public Integer additionalCost; // incidental_expenses

	@Column(name = "insuranceCost", nullable = false)
	public Integer insuranceCost; // insurance_bill

	@Column(name = "insuranceCostMyCar", nullable = false)
	public Integer insuranceCostMyCar; // self_insurance_bill
}
