package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.AddressDTO;
import com.app.lunchsolver.dto.SessionUser;
import com.app.lunchsolver.entity.user.User;

public interface UserService {
    AddressDTO getXY(String query);
    String getAddress(double x, double y);

    User saveOrUpdateXY(SessionUser sessionUser);
}
