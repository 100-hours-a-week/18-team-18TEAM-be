package com.caro.bizkit.domain.chat.controller;

import com.caro.bizkit.domain.chat.dto.ChatMessageRequest;
import com.caro.bizkit.domain.chat.dto.ChatMessageResponse;
import com.caro.bizkit.domain.chat.dto.RedisChatMessage;
import com.caro.bizkit.domain.chat.service.ChatMessageService;
import com.caro.bizkit.domain.chat.service.ChatRedisPublisher;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService chatMessageService;
    private final ChatRedisPublisher chatRedisPublisher;
    private final UserRepository userRepository;

    @MessageMapping("/chat/messages")
    public void sendMessage(ChatMessageRequest request, Principal principal) {
        Integer userId = Integer.valueOf(principal.getName());

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLinedNumber(),
                user.getCompany(),
                user.getDepartment(),
                user.getPosition(),
                user.getProfileImageKey(),
                user.getDescription()
        );

        // DB 저장
        ChatMessageResponse response = chatMessageService.sendMessage(userPrincipal, request);

        // Redis publish (모든 인스턴스에 브로드캐스트)
        chatRedisPublisher.publish(RedisChatMessage.from(response));
    }
}
