package com.app.lunchsolver.entity.menu;

import com.app.lunchsolver.entity.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, String> {
    List<Menu> findMenuByRestaurant(Restaurant restaurant);
}
