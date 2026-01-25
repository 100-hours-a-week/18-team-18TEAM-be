package com.caro.bizkit.domain.card.service;

import com.caro.bizkit.domain.card.dto.CardRequest;
import com.caro.bizkit.domain.card.dto.CardResponse;
import com.caro.bizkit.domain.card.entity.Card;
import com.caro.bizkit.domain.card.repository.CardRepository;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        boolean isProgress = request.is_progress() != null
                ? request.is_progress()
                : request.end_date() == null;
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
                isProgress,
                request.qr_image_key(),
                request.ai_image_key()
        );
        Card saved = cardRepository.save(card);
        return CardResponse.from(saved);
    }
}
