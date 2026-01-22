package com.caro.bizkit.domain.user.dto;

public record UserResponse(
        Integer id,
        String name,
        String email,
        String phone_number,
        String lined_number,
        String company,
        String department,
        String position,
        String profile_image_url,
        String description
) {
}
