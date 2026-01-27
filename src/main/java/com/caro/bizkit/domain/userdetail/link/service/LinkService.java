package com.caro.bizkit.domain.userdetail.link.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.userdetail.link.dto.LinkResponse;
import com.caro.bizkit.domain.userdetail.link.repository.LinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;

    public List<LinkResponse> getMyLinks(UserPrincipal principal) {
        return linkRepository.findAllByUserId(principal.id()).stream()
                .map(LinkResponse::from)
                .toList();
    }
}
