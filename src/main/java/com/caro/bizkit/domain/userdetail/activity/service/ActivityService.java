package com.caro.bizkit.domain.userdetail.activity.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.userdetail.activity.dto.ActivityRequest;
import com.caro.bizkit.domain.userdetail.activity.dto.ActivityResponse;
import com.caro.bizkit.domain.userdetail.activity.dto.ActivityUpdateRequest;
import com.caro.bizkit.domain.userdetail.activity.entity.Activity;
import com.caro.bizkit.domain.userdetail.activity.repository.ActivityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public List<ActivityResponse> getActivitiesByUserId(Integer userId) {
        return activityRepository.findAllByUserId(userId).stream()
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

    @PreAuthorize("@activitySecurity.isOwner(#activityId, authentication)")
    public ActivityResponse updateMyActivity(
            UserPrincipal principal,
            Integer activityId,
            ActivityUpdateRequest request
    ) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (request.name() != null) {
            activity.updateName(request.name());
        }
        if (request.grade() != null) {
            activity.updateGrade(request.grade());
        }
        if (request.organization() != null) {
            activity.updateOrganization(request.organization());
        }
        if (request.content() != null) {
            activity.updateContent(request.content());
        }
        if (request.win_date() != null) {
            activity.updateWinDate(request.win_date());
        }
        return ActivityResponse.from(activity);
    }

    @PreAuthorize("@activitySecurity.isOwner(#activityId, authentication)")
    public void deleteMyActivity(UserPrincipal principal, Integer activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        activityRepository.delete(activity);
    }
}
