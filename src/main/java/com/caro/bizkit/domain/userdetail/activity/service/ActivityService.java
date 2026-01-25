package com.caro.bizkit.domain.userdetail.activity.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.userdetail.activity.dto.ActivityRequest;
import com.caro.bizkit.domain.userdetail.activity.dto.ActivityResponse;
import com.caro.bizkit.domain.userdetail.activity.entity.Activity;
import com.caro.bizkit.domain.userdetail.activity.repository.ActivityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public List<ActivityResponse> getMyActivities(UserPrincipal principal) {
        return activityRepository.findAllByUserId(principal.id()).stream()
                .map(ActivityResponse::from)
                .toList();
    }

    public ActivityResponse createMyActivity(UserPrincipal principal, ActivityRequest request) {
        User user = userRepository.getReferenceById(principal.id());
        Activity activity = Activity.create(
                user,
                request.name(),
                request.grade(),
                request.organization(),
                request.content(),
                request.win_date()
        );
        Activity saved = activityRepository.save(activity);
        return ActivityResponse.from(saved);
    }
}
