package com.caro.bizkit.domain.card.service;

import com.caro.bizkit.domain.card.dto.CardRequest;
import com.caro.bizkit.domain.card.dto.CardResponse;
import com.caro.bizkit.domain.card.dto.CardUpdateRequest;
import com.caro.bizkit.domain.card.entity.Card;
import com.caro.bizkit.domain.card.repository.CardRepository;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public List<CardResponse> getMyCards(UserPrincipal principal) {
        return cardRepository.findAllByUserId(principal.id()).stream()
                .map(CardResponse::from)
                .toList();
    }

    public CardResponse createMyCard(UserPrincipal principal, CardRequest request) {
        User user = userRepository.getReferenceById(principal.id());
        Card card = Card.create(
                user,
                Card.newUuid(),
                request.name(),
                request.email(),
                request.phone_number(),
                request.lined_number(),
                request.company(),
                request.position(),
                request.department(),
                request.start_date(),
                request.end_date(),
                request.qr_image_key(),
                request.ai_image_key()
        );
        Card saved = cardRepository.save(card);
        return CardResponse.from(saved);
    }

    @PreAuthorize("@cardSecurity.isOwner(#cardId, authentication)")
    public CardResponse updateMyCard(
            UserPrincipal principal,
            Integer cardId,
            CardUpdateRequest request
    ) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if (request.name() != null) {
            card.updateName(request.name());
        }
        if (request.email() != null) {
            card.updateEmail(request.email());
        }
        if (request.phone_number() != null) {
            card.updatePhoneNumber(request.phone_number());
        }
        if (request.lined_number() != null) {
            card.updateLinedNumber(request.lined_number());
        }
        if (request.company() != null) {
            card.updateCompany(request.company());
        }
        if (request.position() != null) {
            card.updatePosition(request.position());
        }
        if (request.department() != null) {
            card.updateDepartment(request.department());
        }
        if (request.start_date() != null) {
            card.updateStartDate(request.start_date());
        }
        if (request.is_progress() != null) {
            card.updateIsProgress(request.is_progress());
            if (request.is_progress()) {
                card.updateEndDate(null);
            }
        }
        if (request.end_date() != null) {
            card.updateEndDate(request.end_date());
            card.updateIsProgress(Boolean.FALSE);
        }
        if (request.qr_image_key() != null) {
            card.updateQrImageKey(request.qr_image_key());
        }
        if (request.ai_image_key() != null) {
            card.updateAiImageKey(request.ai_image_key());
        }
        return CardResponse.from(card);
    }
}
