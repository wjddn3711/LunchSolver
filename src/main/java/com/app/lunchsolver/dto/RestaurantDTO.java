package com.app.lunchsolver.dto;

import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.entity.user.User;
import com.app.lunchsolver.enums.RestaurantType;
import com.app.lunchsolver.enums.Role;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class RestaurantDTO implements RestaurantDTOInterface {

    public long id;

    public String address;

    public String category;

    public String image_Url;

    public String name;

    public Double diff_Distance;

    public String business_Hours;

    public Double visitor_Review_Score;

    public Long save_Count;

    public Double booking_Review_Score;

    public RestaurantType restaurant_Type;

    public Double x;

    public Double y;

    @Builder
    public RestaurantDTO(long id, String address, String category, String image_Url, String name, Double diff_Distance, String business_Hours, Double visitor_Review_Score, Long save_Count, Double booking_Review_Score, RestaurantType restaurant_Type, Double x, Double y) {
        this.id = id;
        this.address = address;
        this.category = category;
        this.image_Url = image_Url;
        this.name = name;
        this.diff_Distance = diff_Distance;
        this.business_Hours = business_Hours;
        this.visitor_Review_Score = visitor_Review_Score;
        this.save_Count = save_Count;
        this.booking_Review_Score = booking_Review_Score;
        this.restaurant_Type = restaurant_Type;
        this.x = x;
        this.y = y;
    }

    public static List<RestaurantDTO> interfaceToDto(List<RestaurantDTOInterface> dtoInterfaces){
        List<RestaurantDTO> dtos = new ArrayList<>();
        for (RestaurantDTOInterface dtoInterface : dtoInterfaces) {
            dtos.add(RestaurantDTO.builder()
                    .x(dtoInterface.getX())
                    .y(dtoInterface.getY())
                    .name(dtoInterface.getName())
                    .id(dtoInterface.getId())
                    .address(dtoInterface.getAddress())
                    .category(dtoInterface.getCategory())
                    .image_Url(dtoInterface.getImage_Url())
                    .diff_Distance(dtoInterface.getDiff_Distance())
                    .business_Hours(dtoInterface.getBusiness_Hours())
                    .visitor_Review_Score(dtoInterface.getVisitor_Review_Score())
                    .save_Count(dtoInterface.getSave_Count())
                    .booking_Review_Score(dtoInterface.getBooking_Review_Score())
                    .restaurant_Type(dtoInterface.getRestaurant_Type())
                    .build());
        }
        return dtos;
    }
}
