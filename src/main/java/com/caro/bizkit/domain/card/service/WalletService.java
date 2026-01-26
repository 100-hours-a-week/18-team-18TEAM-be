package com.caro.bizkit.domain.card.service;

import com.caro.bizkit.domain.card.dto.CardCollectRequest;
import com.caro.bizkit.domain.card.dto.CardResponse;
import com.caro.bizkit.domain.card.entity.Card;
import com.caro.bizkit.domain.card.entity.UserCard;
import com.caro.bizkit.domain.card.repository.CardRepository;
import com.caro.bizkit.domain.card.repository.UserCardRepository;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final CardRepository cardRepository;
    private final UserCardRepository userCardRepository;
    private final UserRepository userRepository;

    public CardResponse collectCard(UserPrincipal principal, CardCollectRequest request) {
        Card card = cardRepository.findByUuid(request.uuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if (card.getUser() != null && card.getUser().getId().equals(principal.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot collect your own card");
        }
        if (userCardRepository.existsByUserIdAndCardId(principal.id(), card.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already collected");
        }
        User user = userRepository.getReferenceById(principal.id());
        UserCard userCard = UserCard.create(user, card);
        userCardRepository.save(userCard);
        return CardResponse.from(card);
    }
}
