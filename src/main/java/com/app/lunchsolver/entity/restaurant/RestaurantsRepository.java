package com.app.lunchsolver.entity.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestaurantsRepository extends JpaRepository<Restaurant, Long> {
    @Query(value = "select r.id from Restaurant r", nativeQuery = true)
    public List<Long> findAllreturnId();

}
