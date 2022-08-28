package com.app.lunchsolver.dto;


import lombok.*;

@Data
public class AddressDTO {
    double x;
    double y;

    @Builder
    public AddressDTO(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
