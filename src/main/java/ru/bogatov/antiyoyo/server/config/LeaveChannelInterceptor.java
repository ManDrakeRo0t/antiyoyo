package ru.bogatov.antiyoyo.server.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import ru.bogatov.antiyoyo.server.service.SimpSessionStorage;

@Slf4j
@AllArgsConstructor
public class LeaveChannelInterceptor implements ChannelInterceptor {

    private final SimpSessionStorage simpSessionStorage;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if (StompCommand.DISCONNECT == command) {
            simpSessionStorage.userDisconnected((String) message.getHeaders().get("simpSessionId"));
        }

        return message;
    }
}
