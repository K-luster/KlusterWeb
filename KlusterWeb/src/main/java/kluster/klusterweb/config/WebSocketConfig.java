package kluster.klusterweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig {

    public void configureMessageBroker(MessageBrokerRegistry config) { // 메시지 브로커를 설정
        config.enableSimpleBroker("/topic","/queue"); // 내장 메시지 브로커를 사용하기 위한 메소드
        config.setApplicationDestinationPrefixes("/app");
    }

    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/login").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/chat").setAllowedOriginPatterns("*").withSockJS();
    }

}
