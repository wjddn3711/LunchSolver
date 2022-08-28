package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.AddressDTO;

public interface UserService {
    AddressDTO getXY(String query);
    String getAddress(double x, double y);
}
