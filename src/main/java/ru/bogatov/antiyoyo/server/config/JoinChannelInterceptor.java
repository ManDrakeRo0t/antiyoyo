package ru.bogatov.antiyoyo.server.config;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import ru.bogatov.antiyoyo.server.service.SimpSessionStorage;

@Slf4j
@AllArgsConstructor
public class JoinChannelInterceptor implements ChannelInterceptor {

    private final SimpSessionStorage simpSessionStorage;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if (StompCommand.SUBSCRIBE == command || StompCommand.SEND == command) {
            final var requestTokenHeader = accessor.getFirstNativeHeader("Authorization");
            final var topic = accessor.getFirstNativeHeader(StompHeaders.DESTINATION);
            log.info("User connected : User Id : {} and Topic : {}", requestTokenHeader, topic);
            simpSessionStorage.userConnected((String) message.getHeaders().get("simpSessionId"),requestTokenHeader, topic.substring(16, 52));
        }

        return message;
    }

}
