package com.parknow.service;

import com.parknow.dto.request.UpdateProfileRequest;
import com.parknow.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getUserProfile(Long userId);
    UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request);
}
