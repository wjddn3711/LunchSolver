package com.app.lunchsolver.entity.restaurant;

import com.app.lunchsolver.dto.RestaurantDTO;
import com.app.lunchsolver.dto.RestaurantDTOInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface RestaurantsRepository extends JpaRepository<Restaurant, Long> {
    @Query(value = "select r.id from Restaurant r", nativeQuery = true)
    public List<Long> findAllreturnId();

    @Query(value = "SELECT R.ID, " +
            "R.BUSINESS_HOURS, R.BOOKING_REVIEW_SCORE," +
            "R.NAME,R.IMAGE_URL,R.CATEGORY," +
            "R.RESTAURANT_TYPE,R.SAVE_COUNT," +
            "R.VISITOR_REVIEW_SCORE," +
            "ST_Distance_Sphere(Point(:x,:y),POINT(R.X, R.Y)) AS diff_Distance," +
            "R.X," +
            "R.Y," +
            "R.ADDRESS " +
            "FROM RESTAURANT AS R HAVING diff_Distance <= 1000 order by diff_Distance", nativeQuery = true)
    public List<RestaurantDTOInterface> getRestaurantByLocation(@Param("x") Double x, @Param("y") Double y);
}
