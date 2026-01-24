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
    public static UserResponse fromPrincipal(UserPrincipal principal, String profileImageUrl) {
        if (principal == null) {
            return null;
        }
        return new UserResponse(
                principal.id(),
                principal.name(),
                principal.email(),
                principal.phone_number(),
                principal.lined_number(),
                principal.company(),
                principal.department(),
                principal.position(),
                profileImageUrl,
                principal.description()
        );
    }
}
