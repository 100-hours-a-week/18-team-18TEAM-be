package com.caro.bizkit.domain.card.service;

import com.caro.bizkit.domain.card.dto.CardCollectRequest;
import com.caro.bizkit.domain.card.dto.CardResponse;
import com.caro.bizkit.domain.card.dto.CollectedCardsResult;
import com.caro.bizkit.domain.card.entity.Card;
import com.caro.bizkit.domain.card.entity.UserCard;
import com.caro.bizkit.domain.card.repository.CardRepository;
import com.caro.bizkit.domain.card.repository.UserCardRepository;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final CardRepository cardRepository;
    private final UserCardRepository userCardRepository;
    private final UserRepository userRepository;

    @Transactional()
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
    @Transactional(readOnly = true)
    public CollectedCardsResult getCollectedCards(
            UserPrincipal principal,
            Integer size,
            Integer cursorId,
            String keyword
    ) {
        int limit = normalizeSize(size);
        List<UserCard> userCards = findUserCards(principal.id(), cursorId, keyword, limit + 1);
        boolean hasNext = userCards.size() > limit;
        if (hasNext) {
            userCards = userCards.subList(0, limit);
        }
        Integer nextCursorId = userCards.isEmpty() ? null : userCards.getLast().getId();
        List<CardResponse> cards = userCards.stream()
                .map(UserCard::getCard)
                .map(CardResponse::from)
                .toList();
        return new CollectedCardsResult(cards, nextCursorId, hasNext);
    }

    @Transactional
    public void deleteCollectedCard(UserPrincipal principal, Integer cardId) {
        UserCard userCard = userCardRepository.findByUserIdAndCardId(principal.id(), cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collected card not found"));
        userCardRepository.delete(userCard);
    }

    private List<UserCard> findUserCards(Integer userId, Integer cursorId, String keyword, int limit) {
        if (StringUtils.hasText(keyword)) {
            return userCardRepository.searchCollectedCards(
                    userId,
                    cursorId,
                    keyword,
                    PageRequest.of(0, limit)
            );
        }
        if (cursorId == null) {
            return userCardRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId, PageRequest.of(0, limit));
        }
        return userCardRepository.findByUserIdAndIdLessThanOrderByCreatedAtDescIdDesc(
                userId,
                cursorId,
                PageRequest.of(0, limit)
        );
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 20;
        }
        return size;
    }


}
