package com.app.lunchsolver.dto;

import com.app.lunchsolver.enums.RestaurantType;

 public interface RestaurantDTOInterface {
     long getId();

    String getAddress();

    String getCategory();

    String getImage_Url();

    String getName();

    Double getDiff_Distance();

    String getBusiness_Hours();

    Double getVisitor_Review_Score();

    Long getSave_Count();

    Double getBooking_Review_Score();

    RestaurantType getRestaurant_Type();

    Double getX();

    Double getY();

}
