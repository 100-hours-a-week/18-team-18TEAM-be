package com.caro.bizkit.domain.userdetail.link.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.userdetail.link.dto.LinkRequest;
import com.caro.bizkit.domain.userdetail.link.dto.LinkResponse;
import com.caro.bizkit.domain.userdetail.link.entity.Link;
import com.caro.bizkit.domain.userdetail.link.repository.LinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    public List<LinkResponse> getMyLinks(UserPrincipal principal) {
        return linkRepository.findAllByUserId(principal.id()).stream()
                .map(LinkResponse::from)
                .toList();
    }

    public LinkResponse createMyLink(UserPrincipal principal, LinkRequest request) {
        User user = userRepository.getReferenceById(principal.id());
        Link linkEntity = Link.create(user, request.title(), request.link());
        Link saved = linkRepository.save(linkEntity);
        return LinkResponse.from(saved);
    }
}
