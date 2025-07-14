package ru.bogatov.antiyoyo.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

@Slf4j
public class LeaveChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if (StompCommand.DISCONNECT == command) {
            final var requestTokenHeader = accessor.getFirstNativeHeader("Authorization");
            final var topic = accessor.getFirstNativeHeader(StompHeaders.DESTINATION);
            //todo check simpSession
            log.info("User leave : User Id : {} and Topic : {}", requestTokenHeader, topic);
        }

        return ChannelInterceptor.super.preSend(message, channel);
    }
}
