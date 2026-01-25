package com.caro.bizkit.domain.card.service;

import com.caro.bizkit.domain.card.dto.CardResponse;
import com.caro.bizkit.domain.card.repository.CardRepository;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    public List<CardResponse> getMyCards(UserPrincipal principal) {
        return cardRepository.findAllByUserId(principal.id()).stream()
                .map(CardResponse::from)
                .toList();
    }
}
