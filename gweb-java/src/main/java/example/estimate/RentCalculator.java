package example.estimate;

import example.estimate.domain.CarEstimate;

public interface RentCalculator {

	public CarEstimate getResult(String version, // version
			String model,
			boolean returnCar,
			int duration,
			boolean age,
			double deposit,
			double commission,
			boolean includeRepair,
			int ownerType
	);
}
