package example.estimate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import example.estimate.domain.CarEstimate;
import example.estimate.repository.CarEstimateRepository;

@Service
public class CarEstimateService {
	@Autowired
	private CarEstimateRepository carEstimateRepository;

	public List<CarEstimate> findAll() {
		return carEstimateRepository.findAll();
	}

	public CarEstimate findByCarEstimateId(String id) {
		return carEstimateRepository.findById(id);
	}

	public void save(CarEstimate carEstimate) {
		carEstimateRepository.save(carEstimate);
	}

	public void delete(CarEstimate carEstimate) {
		carEstimateRepository.delete(carEstimate);
	}

	public void deleteAll() {
		carEstimateRepository.deleteAll();
	}
}