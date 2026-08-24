package com.example.green.security;

import com.example.green.domain.entity.User;
import com.example.green.domain.repository.UserRepository;
import com.example.green.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor
        implements ChannelInterceptor {

    private static final String ECO_CONTAINERS_TOPIC =
            "/topic/eco-containers";

    private static final String ADMIN_ALERTS_TOPIC =
            "/topic/admin/alerts";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT) {
            authenticate(accessor);
        }

        if (command == StompCommand.SUBSCRIBE) {
            authorizeSubscription(accessor);
        }

        /*
         * Клиенты этого проекта только подписываются на события.
         * Отправлять сообщения в broker им не разрешается.
         */
        if (command == StompCommand.SEND) {
            throw new AccessDeniedException(
                    "Client WebSocket messages are not allowed"
            );
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorizationHeader =
                accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("WebSocket access token is missing");
        }

        String token = authorizationHeader.substring(7);

        try {
            Claims claims = jwtService.parse(token);

            Long userId = Long.parseLong(
                    claims.getSubject()
            );

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new BadCredentialsException("WebSocket user not found")
                    );

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            user.getId(),
                            null,
                            List.of(
                                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                            )
                    );

            /*
             * Spring сохранит пользователя STOMP-сессии.
             * Он будет доступен в последующих SUBSCRIBE-сообщениях.
             */
            accessor.setUser(authentication);

        } catch (BadCredentialsException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BadCredentialsException("Invalid or expired WebSocket access token", exception);
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        Authentication authentication = requireAuthentication(accessor.getUser());
        String destination = accessor.getDestination();

        if (destination == null) {
            throw new AccessDeniedException("WebSocket destination is missing");
        }

        /*
         * Заполненность контейнеров может видеть любой
         * авторизованный пользователь.
         */
        if (ECO_CONTAINERS_TOPIC.equals(destination)) {
            return;
        }

        /*
         * Административные уведомления доступны только ADMIN.
         */
        if (ADMIN_ALERTS_TOPIC.equals(destination)) {
            boolean isAdmin = authentication
                    .getAuthorities()
                    .stream()
                    .anyMatch(authority ->
                            "ROLE_ADMIN".equals(
                                    authority.getAuthority()
                            )
                    );

            if (!isAdmin) {
                throw new AccessDeniedException("Only administrators can subscribe to admin alerts");
            }

            return;
        }

        /*
         * Запрещаем неизвестные подписки по умолчанию.
         * Это предотвращает случайное открытие нового topic.
         */
        throw new AccessDeniedException("Subscription is not allowed: " + destination);
    }

    private Authentication requireAuthentication(Principal principal) {
        if (!(principal instanceof Authentication authentication) || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("WebSocket authentication is required");
        }

        return authentication;
    }
}