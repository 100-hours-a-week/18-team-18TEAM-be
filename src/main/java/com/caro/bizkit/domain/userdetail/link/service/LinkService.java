package com.caro.bizkit.domain.userdetail.link.service;

import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.userdetail.link.dto.LinkRequest;
import com.caro.bizkit.domain.userdetail.link.dto.LinkResponse;
import com.caro.bizkit.domain.userdetail.link.dto.LinkUpdateRequest;
import com.caro.bizkit.domain.userdetail.link.entity.Link;
import com.caro.bizkit.domain.userdetail.link.repository.LinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public List<LinkResponse> getLinksByUserId(Integer userId) {
        return linkRepository.findAllByUserId(userId).stream()
                .map(LinkResponse::from)
                .toList();
    }

    public LinkResponse createMyLink(UserPrincipal principal, LinkRequest request) {
        User user = userRepository.getReferenceById(principal.id());
        Link linkEntity = Link.create(user, request.title(), request.link());
        Link saved = linkRepository.save(linkEntity);
        return LinkResponse.from(saved);
    }

    @PreAuthorize("@linkSecurity.isOwner(#linkId, authentication)")
    public LinkResponse updateMyLink(
            UserPrincipal principal,
            Integer linkId,
            LinkUpdateRequest request
    ) {
        Link linkEntity = linkRepository.findById(linkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found"));
        if (request.title() != null) {
            linkEntity.updateTitle(request.title());
        }
        if (request.link() != null) {
            linkEntity.updateLink(request.link());
        }
        return LinkResponse.from(linkEntity);
    }

    @PreAuthorize("@linkSecurity.isOwner(#linkId, authentication)")
    public void deleteMyLink(UserPrincipal principal, Integer linkId) {
        Link linkEntity = linkRepository.findById(linkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found"));
        linkRepository.delete(linkEntity);
    }
}
