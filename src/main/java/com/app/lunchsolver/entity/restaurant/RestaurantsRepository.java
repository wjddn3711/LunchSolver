package com.app.lunchsolver.entity.restaurant;

import com.app.lunchsolver.dto.RestaurantDTO;
import com.app.lunchsolver.dto.RestaurantDTOInterface;
import com.app.lunchsolver.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface RestaurantsRepository extends JpaRepository<Restaurant, Long> {
    @Query(value = "select r.id from Restaurant r", nativeQuery = true)
    List<Long> findAllreturnId();

    @Query(value = "SELECT R.ID, " +
            "R.BUSINESS_HOURS, R.BOOKING_REVIEW_SCORE," +
            "R.NAME,R.IMAGE_URL,R.CATEGORY," +
            "R.RESTAURANT_TYPE,R.SAVE_COUNT," +
            "R.VISITOR_REVIEW_SCORE," +
            "ST_Distance_Sphere(Point(:x,:y),POINT(R.X, R.Y)) AS diff_Distance," +
            "R.X," +
            "R.Y," +
            "R.ADDRESS " +
            "FROM RESTAURANT R HAVING diff_Distance <= 1000 ORDER BY diff_Distance"
            ,countQuery = "SELECT COUNT(ST_Distance_Sphere(Point(:x,:y),POINT(R.X, R.Y))) AS diff_Distance FROM RESTAURANT AS R HAVING diff_Distance <= 1000"
            ,nativeQuery = true)
    Page<RestaurantDTOInterface> getRestaurantByLocation(@Param("x") Double x, @Param("y") Double y, Pageable pageable);

    Optional<Restaurant> findRestaurantById(long id);


}
