package com.app.lunchsolver.dto;

import com.app.lunchsolver.enums.RestaurantType;
import lombok.*;

import javax.persistence.*;

@Data
@AllArgsConstructor
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

}
