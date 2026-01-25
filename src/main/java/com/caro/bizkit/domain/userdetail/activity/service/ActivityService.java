package com.caro.bizkit.domain.userdetail.activity.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.userdetail.activity.dto.ActivityResponse;
import com.caro.bizkit.domain.userdetail.activity.repository.ActivityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public List<ActivityResponse> getMyActivities(UserPrincipal principal) {
        return activityRepository.findAllByUserId(principal.id()).stream()
                .map(ActivityResponse::from)
                .toList();
    }
}
