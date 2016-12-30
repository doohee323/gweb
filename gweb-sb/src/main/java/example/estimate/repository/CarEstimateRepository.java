package example.estimate.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import example.estimate.domain.CarEstimate;

public interface CarEstimateRepository extends Repository<CarEstimate, Integer> {

	void save(CarEstimate rentcar);

	CarEstimate findById(String id);

	List<CarEstimate> findAll();

	void delete(CarEstimate carEstimate);

	void deleteAll();

}