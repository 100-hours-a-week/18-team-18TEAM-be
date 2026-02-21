package com.caro.bizkit.domain.chat.config;

import com.caro.bizkit.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        Object tokenAttr = accessor.getSessionAttributes() != null
                ? accessor.getSessionAttributes().get("accessToken")
                : null;

        if (tokenAttr == null) {
            log.warn("STOMP CONNECT: accessToken not found in session");
            throw new IllegalArgumentException("인증 토큰이 없습니다.");
        }

        String token = tokenAttr.toString();
        if (!jwtTokenProvider.isValid(token)) {
            log.warn("STOMP CONNECT: invalid token");
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        Claims claims = jwtTokenProvider.parseClaims(token);
        String userId = claims.getSubject();
        accessor.setUser(new StompPrincipal(userId));
        log.debug("STOMP CONNECT: authenticated userId={}", userId);

        return message;
    }
}
